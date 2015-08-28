/*
 * Created on 20.07.2005
 */
package de.moonflower.jfritz;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.utils.Debug;

/**
 * @author rob
 */
public class ShutdownThread extends Thread {
	private final static Logger log = Logger.getLogger(ShutdownThread.class);
	private Main jfritzMain;

	public ShutdownThread(Main main) {
		super("ShutdownThread");
		this.setDaemon(true);
		jfritzMain = main;
	}

	public void run() {
		Debug.info(log, "Starting shutdown thread.."); //$NON-NLS-1$
		jfritzMain.prepareShutdown(true, false);
		Debug.info(log, "Shutdown thread done."); //$NON-NLS-1$
	}
}
