package de.moonflower.jfritz.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.callerlist.CallerListListener;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.phonebook.PhoneBookListener;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.struct.Port;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.Encryption;

/**
 * This class is responsible for seting up a connection to a
 * JFritz server. All communication between server and client
 * are asynchronus in nature.
 *
 * All communication from client to server
 * uses either ClientDataRequest, ClientActionRequest or String objects,
 * whereas the String objects are intended only to pass messages to the
 * server (like client closing the connection).
 *
 * All communication from server to client uses either DataChange or
 * String objects, where the String objects are also used to pass messages.
 *
 *  @see de.moonflower.jfritz.network.ClientConnectionListener
 *
 * @author brian
 *
 */
public class ServerConnectionThread extends Thread implements CallerListListener,
		PhoneBookListener {
	private final static Logger log = Logger.getLogger(ServerConnectionThread.class);

	private static boolean isConnected = false;

	private static boolean connect = false;

	private Socket socket;

	private ObjectInputStream objectIn;

	private ObjectOutputStream objectOut;

	private Cipher inCipher;

	private Cipher outCipher;

	private ClientDataRequest<Call> callListRequest;

	private ClientDataRequest<Person> phoneBookRequest;

	private ClientActionRequest actionRequest;

	private boolean quit = false;

	private boolean callsAdded = false, callsRemoved=false, callUpdated=false,
		contactsAdded=false, contactsRemoved=false, contactUpdated=false;

	//needed for direct dialing
	private String[] availablePorts = null;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	/**
	 * Returns the current state of this thread
	 *
	 * @return the state of the connection to the server
	 */
	public static boolean isConnected(){
		return isConnected;
	}

	/**
	 * Starts the thread and attempts to build a connection to the
	 * user specified server
	 *
	 */
	public synchronized void connectToServer(){
		connect = true;
		notify();
	}

	/**
	 * This method is used to cleanly kill a connection and put the current
	 * thread into sleep mode
	 *
	 */
	public synchronized void disconnectFromServer(){

		if(!isConnected)
			return;

		try{
			log.info("NETWORKING: Writing disconnect message to the server");
			SealedObject sealed_object = new SealedObject("JFRITZ CLOSE", outCipher);

			objectOut.writeObject(sealed_object);
			objectOut.flush();
			objectOut.close();
			objectIn.close();
			connect = false;
		}catch(IOException e){
			log.error("Error writing disconnect message to server");
			log.error(e.toString());
			e.printStackTrace();
		}catch(IllegalBlockSizeException e){
			log.error("Problems with the block size");
			log.error(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * This is where the connection is initiated and the client is
	 * synchronized with the server
	 *
	 */
	public void run(){
		while(!quit){
			if(!connect){
				try{
					synchronized(this){
						wait();
					}
				}catch(InterruptedException e){
					log.error("SeverConnection Thread was interrupted!");
		        	Thread.currentThread().interrupt();
				}
			}else{

				String server, user, password;
				int port;

				server = properties.getProperty("server.name");
				port = Integer.parseInt(properties.getProperty("server.port"));
				user = properties.getProperty("server.login");
				password = Encryption.decrypt(properties.getProperty("server.password"));

				log.info("NETWORKING: Attempting to connect to server");
				log.info("NETWORKING: Server: "+ server);
				log.info("NETWORKING: Port: "+port);
				log.info("NETWORKING: User: "+user);
				log.info("NETWORKING: Pass: "+password);

				try{
					socket = new Socket(server, port);
					log.info("NETWORKING: successfully connected to server, authenticating");

					//set timeout in case server thread is not functioning properly
					socket.setSoTimeout(20000);
					objectOut = new ObjectOutputStream(socket.getOutputStream());
					objectIn = new ObjectInputStream(socket.getInputStream());

					if(authenticateWithServer(user, password)){
						log.info("NETWORKING: Successfully authenticated with server");
						isConnected = true;
						NetworkStateMonitor.clientStateChanged();

						//reset the keep alive settings to more reasonable level
						socket.setSoTimeout(105000);

						callListRequest = new ClientDataRequest<Call>();
						callListRequest.destination = ClientDataRequest.Destination.CALLLIST;

						phoneBookRequest = new ClientDataRequest<Person>();
						phoneBookRequest.destination = ClientDataRequest.Destination.PHONEBOOK;

						actionRequest = new ClientActionRequest();

						JFritz.getCallerList().addListener(this);
						JFritz.getPhonebook().addListener(this);

						synchronizeWithServer();
						listenToServer();

						JFritz.getCallerList().removeListener(this);
						JFritz.getPhonebook().removeListener(this);
						log.info("NETWORKING: Connection to server closed");

					}else{
						log.error("NETWORKING: Authentication failed!");
						Debug.errDlg(messages.getMessage("authentification_failed"));
						connect = false;

					}

					objectOut.close();
					objectIn.close();

				}catch(ConnectException e){
					String message = messages.getMessage("connection_server_refused");
					log.error(message, e);
					Debug.errDlg(message);
					connect = false;

				}catch(IOException e){
					String message = messages.getMessage("connection_server_refused");
					log.error(message, e);
					Debug.errDlg(message);
				}

				isConnected = false;
				NetworkStateMonitor.clientStateChanged();

				//if a connection is still wished, the socket was closed for some
				//reason, wait a short delay before retrying
				if(connect){

					synchronized(this){

						try{
							log.info("NETWORKING: Waiting 15 secs for retry attempt");
							wait(15000);
						}catch(InterruptedException e){
							log.error("ServerConnectionThread interrupted waiting to reconnect!");
							log.error(e.toString());
							e.printStackTrace();
				        	Thread.currentThread().interrupt();
						}
					}
				}

			}

			//TODO: Cleanup code here!
		}

		log.info("NETWORKING: Server Connection thread has ended cleanly");
	}

	/**
	 * function attempts to login to the user specified server
	 *
	 * @param user username of the account on the server
	 * @param password password of the account on the server
	 * @return whether the client successfully connected to the server or not
	 */
	private boolean authenticateWithServer(String user, String password){
		Object o;
		String response;
		byte[] dataKey;

		try{

			o = objectIn.readObject();
			if(o instanceof String){

				//write out the username to the server and close the stream to free all resources
				response = (String) o;
				log.info("NETWORKING: Connected to JFritz Server: "+response);
				if(!response.equals("JFRITZ SERVER 1.1")){
					log.info("NETWORKING: Unkown Server version, newer JFritz protocoll version?");
					log.info("NETWORKING: Canceling login attempt!");
				}
				objectOut.writeObject(user);
				objectOut.flush();

				// compute the password md5 hash to get our authentication key
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.update(password.getBytes());

				// create our first private key, the auth key for authentication with the client
				DESKeySpec desKeySpec = new DESKeySpec(md.digest());
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
				SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

				// create the first cipher
				Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
				desCipher.init(Cipher.DECRYPT_MODE, secretKey);

				// read in our data key, encoded with the authentication key
				// and then close all resources associated with it
				SealedObject sealedObject = (SealedObject)objectIn.readObject();
				o = sealedObject.getObject(desCipher);
				if(o instanceof byte[]){
					dataKey = (byte[]) o;

					//create the second private key,  the data key
					desKeySpec = new DESKeySpec(dataKey);
					secretKey = keyFactory.generateSecret(desKeySpec);

					//prepare the two data key ciphers
					inCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
					outCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
					inCipher.init(Cipher.DECRYPT_MODE, secretKey);
					outCipher.init(Cipher.ENCRYPT_MODE, secretKey);

					//write the server our OK encoded with our new data key
					SealedObject sealed_ok = new SealedObject("OK", outCipher);
					objectOut.writeObject(sealed_ok);

					//read "OK" response from server
					SealedObject sealed_response = (SealedObject)objectIn.readObject();
					o = sealed_response.getObject(inCipher);
					if(o instanceof String){
						if(o.equals("OK")){		//server unstands us and we understand it
							return true;
						}else{
							log.info("NETWORKING: Server sent wrong string as response to authentication challenge!");
						}
					}else{
						log.info("NETWORKING: Server sent wrong object as response to authentication challenge!");
					}


				}else {
					log.info("NETWORKING: Server sent wrong type for data key!");
				}
			}

		}catch(ClassNotFoundException e){
			log.error("Server authentication response invalid!");
			log.error(e.toString());
			e.printStackTrace();

		}catch(NoSuchAlgorithmException e){
			log.info("NETWORKING: MD5 Algorithm not present in this JVM!");
			log.error(e.toString());
			e.printStackTrace();

		}catch(InvalidKeySpecException e){
			log.info("NETWORKING: Error generating cipher, problems with key spec?");
			log.error(e.toString());
			e.printStackTrace();

		}catch(InvalidKeyException e){
			log.info("NETWORKING: Error genertating cipher, problems with key?");
			log.error(e.toString());
			e.printStackTrace();

		}catch(NoSuchPaddingException e){
			log.info("NETWORKING: Error generating cipher, problems with padding?");
			log.error(e.toString());
			e.printStackTrace();

		}catch(EOFException e){
			log.error("Server closed Stream unexpectedly!");
			log.error(e.toString());
			e.printStackTrace();

		}catch(SocketTimeoutException e){
			log.error("Read timeout while authenticating with server!");
			log.error(e.toString());
			e.printStackTrace();

		}catch(IOException e){
			log.error("Error reading response during authentication!");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			log.error("Bad padding exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * function gets all calls newer than the newest call in the call list
	 * and gets all contacts from the server.
	 *
	 */
	private synchronized void synchronizeWithServer(){

		log.info("NETWORKING: Requesting updates from server");
		try{
			callListRequest.operation = ClientDataRequest.Operation.GET;
			callListRequest.timestamp = JFritz.getCallerList().getLastCallDate();
			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset(); //reset the streams object cache!

			phoneBookRequest.operation = ClientDataRequest.Operation.GET;
			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

			//request the available ports
			actionRequest.action = ClientActionRequest.ActionType.doCall;
			actionRequest.port = null;
			actionRequest.number = null;
			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing synchronizing request to server!");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * function listens to commands issued by the server, can only
	 * be exited by closing the object streams or receiving a
	 * close request from the server
	 *
	 */
	@SuppressWarnings("unchecked")
	private void listenToServer(){
		Vector<Call> vCalls;
		Vector<Person> vPersons;
		DataChange<?> change;
		Object o;
		String message;

		log.info("NETWORKING: Listening for commands from server");
		while(true){
			try{
				SealedObject sealed_object = (SealedObject)objectIn.readObject();
				o = sealed_object.getObject(inCipher);
				if(o instanceof DataChange){

					change = (DataChange<?>) o;
						if(change.destination == DataChange.Destination.CALLLIST){
							if(change.operation == DataChange.Operation.ADD){

								vCalls = (Vector<Call>) change.data;
								log.info("NETWORKING: Received request to add "+vCalls.size()+" calls");

								//lock the call list so the new entries don't ping pong back and forth
								synchronized(JFritz.getCallerList()){
									callsAdded = true;
									JFritz.getCallerList().addEntries(vCalls);
									callsAdded = true;
								}

							}else if(change.operation == DataChange.Operation.REMOVE){

								vCalls = (Vector<Call>) change.data;
								log.info("NETWORKING: Received request to remove "+vCalls.size()+" calls");

								synchronized(JFritz.getCallerList()){
									callsRemoved = true;
									JFritz.getCallerList().removeEntries(vCalls);
									callsRemoved = false;
								}

							}else if(change.operation == DataChange.Operation.UPDATE){

								log.info("NETWORKING: Received request to upate a call");
								synchronized(JFritz.getCallerList()){
									callUpdated=true;
									JFritz.getCallerList().updateEntry((Call) change.original, (Call) change.updated);
									callUpdated=false;
								}
							}

						}else if(change.destination == DataChange.Destination.PHONEBOOK){
							if(change.operation == DataChange.Operation.ADD){

								vPersons = (Vector<Person>) change.data;
								log.info("NETWORKING: Received request to add "+vPersons.size()+" contacts");

								synchronized(JFritz.getCallerList()){
									contactsAdded = true;
									JFritz.getPhonebook().addEntries(vPersons);
									contactsAdded = false;
								}

							}else if(change.operation == DataChange.Operation.REMOVE){

								vPersons = (Vector<Person>) change.data;
								log.info("NETWORKING: Received request to remove "+vPersons.size()+" contacts");

								synchronized(JFritz.getPhonebook()){
									contactsRemoved = true;
									JFritz.getPhonebook().removeEntries(vPersons);
									contactsRemoved = false;
								}

							}else if(change.operation == DataChange.Operation.UPDATE){

								log.info("NETWORKING: Recieved request to update a contact");

								synchronized(JFritz.getPhonebook()){
									contactUpdated = true;
									JFritz.getPhonebook().updateEntry((Person) change.original, (Person) change.updated);
									contactUpdated = false;
								}
							}

							//Call monitor event from the server
						}else if(change.destination == DataChange.Destination.CALLMONITOR
								&& JFritz.getJframe() != null && JFritz.getJframe().isCallMonitorStarted()
								&& properties.getProperty("option.callMonitorType").equals("6")){

							log.info("NETWORKING: Call monitor event received from server");
							//call in or disconnect event received
							String[] ignoredMSNs = properties.getProperty("option.callmonitor.ignoreMSN").trim().split(";");
							boolean ignoreIt = false;

							if(change.original != null){

								Call c = (Call) change.original;

								// see if we need to ignore this call
								for (int i = 0; i < ignoredMSNs.length; i++) {
						            log.info("NETWORKING: " + ignoredMSNs[i]);
						            if (!ignoredMSNs[i].equals(""))
						                if (c.getRoute()
						                        .equals(ignoredMSNs[i])) {

						                    ignoreIt = true;
						                    break;
						                }
						        }

								if(ignoreIt)
									continue;

								//Pending call in event
								if(change.operation == DataChange.Operation.ADD &&
										Boolean.parseBoolean(properties.getProperty(
						                        "option.callmonitor.monitorTableIncomingCalls"))){

									JFritz.getCallMonitorList().invokeIncomingCall(c);

									//Established call in event
								} else if(change.operation == DataChange.Operation.UPDATE &&
										Boolean.parseBoolean(properties.getProperty(
						                        "option.callmonitor.monitorTableIncomingCalls"))){

									JFritz.getCallMonitorList().invokeIncomingCallEstablished(c);

									//Disconnect call event
								} else if(change.operation == DataChange.Operation.REMOVE){
									JFritz.getCallMonitorList().invokeDisconnectCall(c);
								}

								// call out event received
							} else if( change.updated != null && Boolean.parseBoolean(properties.getProperty(
										"option.callmonitor.monitorOutgoingCalls"))){

								Call c = (Call) change.updated;

								//see if we need to ingnore the call
								for (int i = 0; i < ignoredMSNs.length; i++) {
						            log.debug(ignoredMSNs[i]);
						            if (!ignoredMSNs[i].equals(""))
						                if (c.getRoute()
						                        .equals(ignoredMSNs[i])) {

						                    ignoreIt = true;
						                    break;
						                }
						        }

								if(ignoreIt)
									continue;

								// call out pending event received
								if(change.operation == DataChange.Operation.ADD){
									JFritz.getCallMonitorList().invokeOutgoingCall(c);

								// call out established event received
								}else if(change.operation == DataChange.Operation.UPDATE){
									JFritz.getCallMonitorList().invokeOutgoingCallEstablished(c);
								}

							}

						}else{
							log.info("NETWORKING: destination not chosen for incoming data, ignoring!");
						}
						//we received the ports list from the server
				}else if(o instanceof String[]){
					log.info("received available ports from server");
					availablePorts = (String[]) o;
				}else if(o instanceof String){ //message received from the server

					message = (String) o;

					if(message.equals("JFRITZ CLOSE")){
						log.info("NETWORKING: Closing connection with server!");
						disconnect();
						connect = false;
						return;
					}else if(message.equals("Party on, Wayne!")){
						log.info("NETWORKING: Received keep alive message from server");
						replyToKeepAlive();
					}else{
						log.info("NETWORKING: Received message from server: "+message);
					}

					//TODO: Add other messages here if necessary

				}else {
					log.info("NETWORKING: " + o.toString());
					log.info("NETWORKING: received unexpected object, ignoring!");
				}

			}catch(ClassNotFoundException e){
				log.error("Response from server contained unkown object!");
				log.error(e.toString());
				e.printStackTrace();
			}catch(SocketException e){
				if(e.getMessage().equals("Socket closed")){
					log.info("NETWORKING: Socket closed");	//we closed the socket as requested by the user
				}else{
					log.error(e.toString());
					e.printStackTrace();
				}
				return;
			}catch(EOFException e ){
				log.error("Server closed stream unexpectedly!");
				log.error(e.toString());
				e.printStackTrace();
				return;
			}catch(IOException e){
				log.error(e.toString());
				e.printStackTrace();
				return;
			} catch (IllegalBlockSizeException e) {
				log.error("Illegal block size exception!");
				log.error(e.toString());
				e.printStackTrace();
			} catch (BadPaddingException e) {
				log.error("Bad padding exception!");
				log.error(e.toString());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when the server send a close request. This code makes sure that
	 * we aren't writing a request to the server as the streams are closed
	 *
	 */
	private synchronized void disconnect(){
		try{
			objectOut.close();
			objectIn.close();
		}catch(IOException e){
			log.error("Error disconnecting from server");
			log.error(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Function used to quit this thread, should be called on application exit
	 *
	 */
	public synchronized void quitThread(){
		quit = true;
		notify();
	}


	public synchronized void requestLookup(){
		actionRequest.action = ClientActionRequest.ActionType.doLookup;
		try{

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing lookup request to server");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * This function request a specific reverse lookup for the number using the site
	 *
	 * @param number
	 * @param siteName
	 */
	public synchronized void requestSpecificLookup(PhoneNumberOld number, String siteName){

		actionRequest.action = ClientActionRequest.ActionType.doLookup;
		actionRequest.number = number;
		actionRequest.siteName = siteName;

		try{

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing lookup request to server");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		actionRequest.number = null;
		actionRequest.siteName = null;
	}

	public synchronized void requestGetCallList(){
		actionRequest.action = ClientActionRequest.ActionType.getCallList;

		try{

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing do get list request");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

	}

	public synchronized void requestDeleteList(){
		log.info("Requesting server to delete the list from the box");
		actionRequest.action = ClientActionRequest.ActionType.deleteListFromBox;

		try{

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing writing delete list from box request");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}
	}

	public synchronized void requestDoCall(PhoneNumberOld number, Port port){
		log.info("NETWORKING: Requesting the server to dial "+number.getIntNumber()+" using "+port);
		actionRequest.action = ClientActionRequest.ActionType.doCall;
		actionRequest.number = number;
		actionRequest.port = port;

		try{

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing writing doCall request");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		actionRequest.number = null;
		actionRequest.port = null;
	}

	public synchronized void requestHangup(Port port){
		log.info("NETWORKING: Requesting the server to hangup");
		actionRequest.action = ClientActionRequest.ActionType.hangup;
		actionRequest.port = port;

		try{

			SealedObject sealedActionRequest = new SealedObject(actionRequest, outCipher);
			objectOut.writeObject(sealedActionRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing writing hangup request");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}
	}

	public synchronized void callsAdded(Vector<Call> newCalls){

		//this thread added the new calls, so we don't need to write them back
		if(callsAdded)
			return;

		log.info("NETWORKING: Notifying the server of added calls, size: "+newCalls.size());
		callListRequest.data = newCalls;
		callListRequest.operation = ClientDataRequest.Operation.ADD;

		try{
			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing new calls to the server");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		//remove reference to the data
		callListRequest.data = null;
	}

	public synchronized void callsRemoved(Vector<Call> removedCalls){

		//this thread removed the calls, no need to write them back
		if(callsRemoved)
			return;

		log.info("NETWORKING: Notifying the server of removed calls, size: "+removedCalls.size());
		callListRequest.data = removedCalls;
		callListRequest.operation = ClientDataRequest.Operation.REMOVE;

		try{
			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset();
		}catch(IOException e){
			log.error("Error writing removed calls to the server");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		//remove reference to the data
		callListRequest.data = null;
	}

	public synchronized void callsUpdated(Call original, Call updated){

		if(callUpdated)
			return;

		log.info("NETWORKING: Notifying server of updated call");
		callListRequest.operation = ClientDataRequest.Operation.UPDATE;
		callListRequest.original = original;
		callListRequest.updated = updated;

		try{

			SealedObject sealedCallListRequest = new SealedObject(callListRequest, outCipher);
			objectOut.writeObject(sealedCallListRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing updated call to server!");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		//wipe out uneeded contents for later
		callListRequest.original = null;
		callListRequest.updated = null;

	}

	public synchronized void contactsAdded(Vector<Person> newContacts){

		//This thread added the contacts, no need to write them back
		if(contactsAdded)
			return;

		log.info("NETWORKING: Notifying the server of added contacts, size: "+newContacts.size());
		phoneBookRequest.data = newContacts;
		phoneBookRequest.operation = ClientDataRequest.Operation.ADD;

		try{
			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();
		}catch(IOException e){
			log.error("Error writing new contacts to server!");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.data = null;
	}

	public synchronized void contactsRemoved(Vector<Person> removedContacts){

		//This thread removed the contacts, no need to write them back
		if(contactsRemoved)
			return;

		log.info("NETWORKING: Notifying the server of removed contacts, size: "+removedContacts.size());
		phoneBookRequest.data = removedContacts;
		phoneBookRequest.operation = ClientDataRequest.Operation.REMOVE;

		try{

			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing removed contacts to server!");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.data = null;
	}

	public synchronized void contactUpdated(Person original, Person updated){

		if(contactUpdated)
			return;

		phoneBookRequest.operation = ClientDataRequest.Operation.UPDATE;
		phoneBookRequest.original = original;
		phoneBookRequest.updated = updated;

		try{

			SealedObject sealedPhoneBookRequest = new SealedObject(phoneBookRequest, outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing updated contact to server");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}

		phoneBookRequest.original = null;
		phoneBookRequest.updated = null;
	}

	/**
	 * This function replies to a keep alive message sent form
	 * the server
	 *
	 */
	public  synchronized void replyToKeepAlive(){
		try{

			log.info("NETWORKING: Replying to servers keep alive message");
			SealedObject sealedPhoneBookRequest = new SealedObject("Party on, Garth!", outCipher);
			objectOut.writeObject(sealedPhoneBookRequest);
			objectOut.flush();
			objectOut.reset();

		}catch(IOException e){
			log.error("Error writing updated contact to server");
			log.error(e.toString());
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			log.error("Illegal block size exception!");
			log.error(e.toString());
			e.printStackTrace();
		}
	}

	public synchronized boolean hasAvailablePorts(){
		if(availablePorts != null)
			return true;

		return false;
	}

	public synchronized String[] getAvailablePorts(){
		return availablePorts;
	}

}