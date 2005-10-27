/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.dialogs.quickdial.QuickDials;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.firmware.FritzBoxFirmware;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.QuickDial;
import de.moonflower.jfritz.utils.HTMLUtil;
import java.net.URLEncoder;

/**
 * Static class for data retrieval from the fritz box
 *
 * TODO: This class needs to be abstracted, so that subclasses for each boxtype
 * can be implemented.
 *
 * Notiz: Der Webserver auf der Fritzbox unterstützt leider kein UTF-8 als
 * URL-Codierung, deshalb habe ich ISO-8859-1 genommen
 *
 * @author akw
 *
 */
public class JFritzUtils {

    final static String POSTDATA_LIST = "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=foncalls&login%3Acommand%2Fpassword=";

    final static String POSTDATA_QUICKDIAL = "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=kurzwahlen&login%3Acommand%2Fpassword=";

    final static String POSTDATA_SIPPROVIDER = "&var%3Alang=de&var%3Amenu=fon&var%3Apagename=siplist&login%3Acommand%2Fpassword=";

    final static String POSTDATA_CLEAR = "&var%3Alang=de&var%3Apagename=foncalls&var%3Amenu=fon&telcfg%3Asettings/ClearJournal=1";

    final static String POSTDATA_CALL = "&login:command/password=$PASSWORT&telcfg:settings/UseClickToDial=1&telcfg:command/Dial=$NUMMER&telcfg:settings/DialPort=$NEBENSTELLE";

    final static String PATTERN_LIST_OLD = "<tr class=\"Dialoglist\">"
            + "\\s*<td class=\"c1\"><script type=\"text/javascript\">document.write\\(uiCallSymbol\\(\"(\\d)\"\\)\\);</script></td>"
            + "\\s*<td class=\"c3\">(\\d\\d\\.\\d\\d\\.\\d\\d \\d\\d:\\d\\d)</td>"
            + "\\s*<td class=\"c4\"><script type=\"text/javascript\">document.write\\(uiRufnummerDisplay\\(\"([^\"\\)\\);]*)\"\\)\\);</script></td>"
            + "\\s*<td class=\"c5\"><script type=\"text/javascript\">document.write\\(uiPortDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
            + "()"
            + "\\s*<td class=\"c6\"><script type=\"text/javascript\">document.write\\(uiDauerDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
            + "\\s*</tr>";

    final static String PATTERN_LIST_42 = "<tr class=\"Dialoglist\">"
            + "\\s*<td class=\"c1\"><script type=\"text/javascript\">document.write\\(uiCallSymbol\\(\"(\\d)\"\\)\\);</script></td>"
            + "\\s*<td class=\"c3\">(\\d\\d\\.\\d\\d\\.\\d\\d \\d\\d:\\d\\d)</td>"
            + "\\s*<td class=\"c4\"><script type=\"text/javascript\">document.write\\(uiRufnummerDisplay\\(\"([^\"\\)\\);]*)\"\\)\\);</script></td>"
            + "\\s*<td class=\"c5\"><script type=\"text/javascript\">document.write\\(uiPortDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
            + "\\s*<td class=\"c7\"><script type=\"text/javascript\">document.write\\(uiRouteDisplay\\(\"(\\w*)\"\\)\\);</script></td>"
            + "\\s*<td class=\"c6\"><script type=\"text/javascript\">document.write\\(uiDauerDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
            + "\\s*</tr>";

    final static String PATTERN_LIST_85 = "<tr class=\"Dialoglist\">"
        + "\\s*<td class=\"c1\"><script type=\"text/javascript\">document.write\\(uiCallSymbol\\(\"(\\d)\"\\)\\);</script></td>"
        + "\\s*<td class=\"c3\">(\\d\\d\\.\\d\\d\\.\\d\\d \\d\\d:\\d\\d)</td>"
        + "\\s*<td class=\"c4\"><script type=\"text/javascript\">document.write\\(uiRufnummerDisplay\\(\"([^\"\\)\\);]*)\"\\)\\);</script></td>"
        + "\\s*<td class=\"c5\"><script type=\"text/javascript\">document.write\\(uiPortDisplay\\(\"(\\d*)\"\\)\\);</script></td>"
        + "\\s*<td class=\"c7\"><script type=\"text/javascript\">document.write\\(uiRouteDisplay\\(([^\\)\\)]*)\\)\\);</script></td>"
        + "\\s*<td class=\"c6\">([^<]*)</td>" + "\\s*</tr>";

    final static String PATTERN_LIST_87 = "<tr class=\"Dialoglist\">"
        + "\\s*<td class=\"c1\"><nobr><script type=\"text/javascript\">document.write\\(uiCallSymbol\\(\"(\\d)\"\\)\\);</script></nobr></td>"
        + "\\s*<td class=\"c3\"><nobr>(\\d\\d\\.\\d\\d\\.\\d\\d \\d\\d:\\d\\d)</nobr></td>"
        + "\\s*<td class=\"c4\"><nobr><script type=\"text/javascript\">document.write\\(uiRufnummerDisplay\\(\"([^\"\\)\\);]*)\"\\)\\);</script></nobr></td>"
        + "\\s*<td class=\"c5\"><nobr><script type=\"text/javascript\">document.write\\(uiPortDisplay\\(\"(\\d*)\"\\)\\);</script></nobr></td>"
        + "\\s*<td class=\"c7\"><nobr><script type=\"text/javascript\">document.write\\(uiRouteDisplay\\(([^\\)\\)]*)\\)\\);</script></nobr></td>"
        + "\\s*<td class=\"c6\"><nobr>([^<]*)</nobr></td>" + "\\s*</tr>";

    final static String PATTERN_QUICKDIAL = "<tr class=\"Dialoglist\">"
            + "\\s*<td style=\"text-align: center;\">(\\d*)</td>"
            + "\\s*<td>(\\w*)</td>"
            + "\\s*<td>([^<]*)</td>"
            + "\\s*<td style=\"text-align: right;\"><button [^>]*>\\s*<img [^>]*></button></td>"
            + "\\s*<td style=\"text-align: right;\"><button [^>]*>\\s*<img [^>]*></button></td>"
            + "\\s*</tr>";

    final static String PATTERN_SIPPROVIDER = "<!-- \"(\\d)\" / \"(\\w*)\" -->"
            + "\\s*<td class=\"c1\">\\s*<input type=checkbox id=\"uiViewActivsip\\d\""
            + "\\s*onclick=\"uiOnChangeActivated\\('uiViewActivsip\\d','uiPostActivsip\\d'\\); return true;\">"
            + "\\s*</td>"
            //			+ "\\s*<td
            // class=\"c2\">([\\(]*[\\w*\\s*]*[\\)]*[\\w*\\s*]*)</td>"
            + "\\s*<td class=\"c2\">([^<]*)</td>"
            + "\\s*<td class=\"c3\"><script type=\"text/javascript\">document.write\\(ProviderDisplay\\(\"([^\"]*)\"\\)\\);</script></td>"
            + "\\s*<td class=\"c6\"><script type=\"text/javascript\">document.write\\(AuswahlDisplay\\(\"([^\"]*)\"\\)\\);</script></td>";

    final static String PATTERN_SIPPROVIDER_ACTIVE = "<input type=\"hidden\" name=\"sip:settings/sip(\\d)/activated\" value=\"(\\d)\" id=\"uiPostActivsip";

    /**
     * Detects type of fritz box by detecting the firmware version
     *
     * @param box_address
     * @return boxtype
     * @throws WrongPasswordException
     * @throws IOException
     */
    public static FritzBoxFirmware detectBoxType(String firmware,
            String box_address, String box_password)
            throws WrongPasswordException, IOException {
        FritzBoxFirmware fw;
        try {
            fw = new FritzBoxFirmware(firmware);
            // FIXME: Debug
            //fw = new FritzBoxFirmware("14.03.87");
            Debug.msg("Using Firmware: " + fw + " (" + fw.getBoxName() + ")");
        } catch (InvalidFirmwareException e) {
            fw = FritzBoxFirmware.detectFirmwareVersion(box_address,
                    box_password);
            Debug.msg("Found Firmware: " + fw + " (" + fw.getBoxName() + ")");

        }
        return fw;
    }

    /**
     * retrieves vector of caller data from the fritz box
     *
     * @param box_address
     * @param password
     * @param countryPrefix
     * @param areaPrefix
     * @param areaCode
     * @return Vector of caller data
     * @throws WrongPasswordException
     * @throws IOException
     */
    public static Vector retrieveCallersFromFritzBox(String box_address,
            String password, String countryPrefix, String countryCode,
            String areaPrefix, String areaCode, FritzBoxFirmware firmware,
            JFritz jfritz) throws WrongPasswordException, IOException {

        String data = "";
        String postdata = firmware.getAccessMethod() + POSTDATA_LIST
                + URLEncoder.encode(password, "ISO-8859-1");
        String urlstr = "http://" + box_address + "/cgi-bin/webcm";
        data = fetchDataFromURL(urlstr, postdata);

        // DEBUG: Test other versions
        if (false) {
            String filename = "../Firmware 87/Anrufliste.html";
            Debug.msg("Debug mode: Loading " + filename);
            try {
                data = "";
                String thisLine;
                BufferedReader in = new BufferedReader(new FileReader(filename));
                while ((thisLine = in.readLine()) != null) {
                    data += thisLine;
                }
                in.close();
            } catch (IOException e) {
                Debug.err("File not found: " + filename);
            }
        }
        // END OF DEBUG SECTION

        Vector list = parseCallerData(data, firmware, countryPrefix,
                countryCode, areaPrefix, areaCode, jfritz);
        return list;
    }

    /**
     *
     * @param firmware
     * @return Vector of QuickDial objects
     * @throws WrongPasswordException
     * @throws IOException
     */
    public static Vector retrieveQuickDialsFromFritzBox(QuickDials model,
            FritzBoxFirmware firmware) throws WrongPasswordException,
            IOException {
        String postdata = firmware.getAccessMethod()
                + POSTDATA_QUICKDIAL
                + URLEncoder.encode(Encryption.decrypt(JFritz
                        .getProperty("box.password")), "ISO-8859-1");
        String urlstr = "http://"
                + JFritz.getProperty("box.address", "fritz.box")
                + "/cgi-bin/webcm";
        String data = fetchDataFromURL(urlstr, postdata);
        return parseQuickDialData(model, data, firmware);
    }

    /**
     * retrieves vector of SipProviders stored in the FritzBox
     *
     * @param box_address
     * @param box_password
     * @param firmware
     * @return Vector of SipProvider
     * @throws WrongPasswordException
     * @throws IOException
     *             author robotniko
     * @throws InvalidFirmwareException
     */
    public static Vector retrieveSipProvider(String box_address,
            String box_password, FritzBoxFirmware firmware)
            throws WrongPasswordException, IOException,
            InvalidFirmwareException {
        if (firmware == null)
            throw new InvalidFirmwareException("No valid firmware");
        String postdata = firmware.getAccessMethod() + POSTDATA_SIPPROVIDER
                + URLEncoder.encode(box_password, "ISO-8859-1");
        String urlstr = "http://" + box_address + "/cgi-bin/webcm";
        String data = fetchDataFromURL(urlstr, postdata);

        // DEBUG: Test other versions
        if (false) {
            String filename = "sipProvider 86.html";
            Debug.msg("Debug mode: Loading " + filename);
            try {
                data = "";
                String thisLine;
                BufferedReader in = new BufferedReader(new FileReader(filename));
                while ((thisLine = in.readLine()) != null) {
                    data += thisLine;
                }
                in.close();
            } catch (IOException e) {
                Debug.err("File not found: " + filename);
            }
        }

        Vector list = parseSipProvider(data);
        return list;
    }

    /**
     * parses html data from the fritz!box's web interface and retrieves SIP
     * information.
     *
     * @param data
     *            html data
     * @return list of SipProvider objects author robotniko, akw
     */
    public static Vector parseSipProvider(String data) {
        Vector list = new Vector();
        data = removeDuplicateWhitespace(data);
        Pattern p;
        p = Pattern.compile(PATTERN_SIPPROVIDER);
        Matcher m = p.matcher(data);
        while (m.find()) {
            if (!(m.group(4).equals(""))) {
                list.add(new SipProvider(Integer.parseInt(m.group(5)), m
                        .group(3), m.group(4)));
                Debug.msg("SIP-Provider: " + list.lastElement());
            }
        }
        p = Pattern.compile(PATTERN_SIPPROVIDER_ACTIVE);
        m = p.matcher(data);
        while (m.find()) {
            Enumeration en = list.elements();
            while (en.hasMoreElements()) {
                SipProvider sipProvider = (SipProvider) en.nextElement();
                if (sipProvider.getProviderID() == Integer.parseInt(m.group(1))) {
                    if (Integer.parseInt(m.group(2)) == 1) {
                        sipProvider.setActive(true);
                    } else {
                        sipProvider.setActive(false);
                    }
                }
            }
        }

        return list;
    }

    /**
     * fetches html data from url using POST requests
     *
     * @param urlstr
     * @param postdata
     * @return html data
     * @throws WrongPasswordException
     * @throws IOException
     */
    public static String fetchDataFromURL(String urlstr, String postdata)
            throws WrongPasswordException, IOException {
        URL url = null;
        URLConnection urlConn;
        DataOutputStream printout;
        String data = "";
        boolean wrong_pass = false;

        try {
            url = new URL(urlstr);
        } catch (MalformedURLException e) {
            Debug.err("URL invalid: " + urlstr);
            throw new WrongPasswordException("URL invalid: " + urlstr);
        }

        if (url != null) {
            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Sending postdata
            if (postdata != null) {
                urlConn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                printout = new DataOutputStream(urlConn.getOutputStream());
                printout.writeBytes(postdata);
                printout.flush();
                printout.close();
            }

            BufferedReader d;

            try {
                // Get response data
                d = new BufferedReader(new InputStreamReader(urlConn
                        .getInputStream()));
                int i = 0;
                String str;
                while (null != ((str = HTMLUtil.stripEntities(d.readLine())))) {
                    // Password seems to be wrong
                    // if (str.indexOf("FEHLER: Das angegebene Kennwort ") > 0)
                    if (str.indexOf("FRITZ!Box Anmeldung") > 0)
                        wrong_pass = true;
                    // Skip a few lines
                    //if (i > 778)
                    data += str;
                    i++;
                }
                d.close();
            } catch (IOException e1) {
                throw new IOException("Network unavailable");
            }

            if (wrong_pass)
                throw new WrongPasswordException("Password invalid");
        }
        return data;
    }

    /**
     * clears the caller list on the fritz box
     *
     * @param box_address
     * @param password
     * @throws IOException
     * @throws WrongPasswordException
     */
    public static void clearListOnFritzBox(String box_address, String password,
            FritzBoxFirmware firmware) throws WrongPasswordException,
            IOException {
        Debug.msg("Clearing List");
        String urlstr = "http://" + box_address + "/cgi-bin/webcm";
        String postdata = firmware.getAccessMethod() + POSTDATA_CLEAR;
        fetchDataFromURL(urlstr, postdata);
    }

    /**
     * removes all duplicate whitespaces from inputStr
     *
     * @param inputStr
     * @return outputStr
     */
    public static String removeDuplicateWhitespace(String inputStr) {
        Pattern p = Pattern.compile("\\s+");
        Matcher matcher = p.matcher(inputStr);
        String outputStr = matcher.replaceAll(" ");
        outputStr.replaceAll(">\\s+<", "><");
        return outputStr;
    }

    /**
     * creates a list of QuickDial objects
     *
     * @param data
     * @param firmware
     * @return list of QuickDial objects
     */
    public static Vector parseQuickDialData(QuickDials model, String data,
            FritzBoxFirmware firmware) {
        Vector list = new Vector();
        data = removeDuplicateWhitespace(data);
        Pattern p = Pattern.compile(PATTERN_QUICKDIAL);
        Matcher m = p.matcher(data);

        while (m.find()) {
            String description = model.getDescriptionFromNumber(m.group(3));
            list.add(new QuickDial(m.group(1), m.group(2), m.group(3),
                    description));
        }
        return list;
    }

    /**
     * Parses html data from the fritz!box's web interface.
     *
     * @param data
     */
    public static Vector parseCallerData(String data,
            FritzBoxFirmware firmware, String countryPrefix,
            String countryCode, String areaPrefix, String areaCode,
            JFritz jfritz) {
        Vector list = new Vector();
        data = removeDuplicateWhitespace(data);
        Debug.msg(3, "Parsing data: " + data);
        Pattern p;
        Debug.msg(2, "FirmwareMinorVersion: "
                + firmware.getMinorFirmwareVersion());
        if (firmware.getMinorFirmwareVersion() < 42)
            p = Pattern.compile(PATTERN_LIST_OLD);
        else if ((firmware.getMinorFirmwareVersion() >= 42)
                && (firmware.getMinorFirmwareVersion() < 85))
            p = Pattern.compile(PATTERN_LIST_42);
        else if ((firmware.getMinorFirmwareVersion() >= 85)
                && (firmware.getMinorFirmwareVersion() < 87))
            p = Pattern.compile(PATTERN_LIST_85);
        else
            p = Pattern.compile(PATTERN_LIST_87);

        Matcher m = p.matcher(data);

        while (m.find()) {
            try {
                CallType symbol = new CallType(Byte.parseByte(m.group(1)));
                String port = m.group(4);
                PhoneNumber number = createAreaNumber(m.group(3),
                        countryPrefix, countryCode, areaPrefix, areaCode,
                        jfritz);
                Date date = new SimpleDateFormat("dd.MM.yy HH:mm").parse(m
                        .group(2));
                String route = "";
                int duration = 0;
                if (firmware.getMinorFirmwareVersion() < 85) {
                    route = m.group(5);
                    duration = Integer.parseInt(m.group(6));
                } else {
                    String[] routeStrings = m.group(5).split(",");
                    routeStrings[0] = routeStrings[0].replaceAll("\\\"", ""); // Alle " entfernen
                    routeStrings[1] = routeStrings[1].replaceAll("\\\"", ""); // Alle " entfernen
                    if (routeStrings[1].equals("0")) {
                        route = routeStrings[0]; // Festnetznummer
                    } else if (routeStrings[1].equals("1")) {
                        route = "SIP" + routeStrings[0]; // VoIP-Nummer
                    } else
                        Debug.err("Fehler in parseCallerData: Konnte Route nicht auflösen: "
                                        + m.group(5));
                    String[] durationStrings = m.group(6).split(":");
                    duration = Integer.parseInt(durationStrings[0]) * 3600
                            + Integer.parseInt(durationStrings[1]) * 60;
                }
                list.add(new Call(jfritz, symbol, date, number, port, route,
                        duration));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * creates number with area code prefix
     *
     * @param number
     * @param countryPrefix
     * @param areaPrefix
     * @param areaCode
     * @return number with area code prefix
     */
    public static PhoneNumber createAreaNumber(String number,
            String countryPrefix, String countryCode, String areaPrefix,
            String areaCode, JFritz jfritz) {
        PhoneNumber phoneNumber;
        if (!number.equals("")) {
            if (number.length() < 3) { // Short Number (maybe internal)
                phoneNumber = new PhoneNumber(number);
                return phoneNumber;
            }
            if (number.startsWith("**7")) // QuickDial
            {
                int count = 5;
                Debug.msg("Kurzwahl entdeckt: " + number +"! Ersetze Kurzwahl mit zugehöriger Nummer.");
                while (count>0) {
                    count --;
                    // replace QuickDial with
                    // QuickDial-Entry
                    String quickDialNumber = number.substring(3);
                    if (jfritz.getJframe().getQuickDialPanel().getDataModel()
                            .getQuickDials().size() == 0) {
                        // get QuickDials from FritzBox
                        jfritz.getJframe().getQuickDialPanel().getDataModel()
                                .getQuickDialDataFromFritzBox();
                    }
                    Enumeration en = jfritz.getJframe().getQuickDialPanel()
                            .getDataModel().getQuickDials().elements();
                    while (en.hasMoreElements()) {
                        QuickDial quickDial = (QuickDial) en.nextElement();
                        if (quickDialNumber.equals(quickDial.getQuickdial())) {
                            number = null;
                            phoneNumber = new PhoneNumber(quickDial.getNumber());
                            Debug.msg("Kurzwahl gefunden. Nummer: "+phoneNumber.getFullNumber());
                            return phoneNumber;
                        }
                    }
                    Debug.msg("Kurzwahl NICHT gefunden. Aktualisiere Kurzwahl-Liste");
                    ((QuickDials) jfritz.getJframe().getQuickDialPanel()
                            .getDataModel()).getQuickDialDataFromFritzBox();
                }
            }

            if (number.startsWith(countryCode) && number.length() > 9) {
                // International numbers without countryPrefix
                // (some VOIP numbers)
                number = countryPrefix + number;
            }
            if (number.startsWith(countryPrefix)) { // International call

                if (number.startsWith(countryPrefix + countryCode)) {
                    // if own country, remove countrycode
                    number = areaPrefix
                            + number.substring(countryPrefix.length()
                                    + countryCode.length());
                }
            } else if (!number.startsWith(areaPrefix)) {
                number = areaPrefix + areaCode + number;
            }
            phoneNumber = new PhoneNumber(number);
            return phoneNumber;
        }
        return null;
    }

    /**
     * creates a String with version and date of CVS Id-Tag
     *
     * @param tag
     * @return String with version and date of CVS Id-Tag
     */
    public static String getVersionFromCVSTag(String tag) {
        String[] parts = tag.split(" ");
        return "CVS v" + parts[2] + " (" + parts[3] + ")";
    }

    public static boolean parseBoolean(String input) {
        if (input != null && input.equalsIgnoreCase("true"))
            return true;
        else
            return false;
    }

    public static String lookupAreaCode(String number) {
        Debug.msg("Looking up " + number + "...");
        // FIXME: Does not work (Cookies)
        String urlstr = "http://www.vorwahl.de/national.php";
        String postdata = "search=1&vorwahl=" + number;
        String data = "";
        try {
            data = fetchDataFromURL(urlstr, postdata);
        } catch (Exception e) {
        }
        System.out.println("DATA: " + data.trim());
        return "";
    }

    public static String replaceSpecialChars(String input) {
        // XML Sonderzeichen durch ASCII Codierung ersetzen
        String out = input;
        out = out.replaceAll("&", "&#38;");
        out = out.replaceAll("'", "&#39;");
        out = out.replaceAll("<", "&#60;");
        out = out.replaceAll(">", "&#62;");
        out = out.replaceAll("\"", "&#34;");
        return out;
    }

    public static void doCall(String number, String port,
            FritzBoxFirmware firmware) {
        String data = "";
        try {
            String passwort = Encryption.decrypt(JFritz.getProperty(
                    "box.password", Encryption.encrypt("")));
            number = number.replaceAll("\\+", "00");

            String portStr = "";
            if (port.equals("Fon 1")) {
                portStr = "1";
            } else if (port.equals("Fon 2")) {
                portStr = "2";
            } else if (port.equals("Fon 3")) {
                portStr = "3";
            } else if (port.equals("ISDN Alle")) {
                portStr = "50";
            } else if (port.equals("ISDN 1")) {
                portStr = "51";
            } else if (port.equals("ISDN 2")) {
                portStr = "52";
            } else if (port.equals("ISDN 3")) {
                portStr = "53";
            } else if (port.equals("ISDN 4")) {
                portStr = "54";
            } else if (port.equals("ISDN 5")) {
                portStr = "55";
            } else if (port.equals("ISDN 6")) {
                portStr = "56";
            } else if (port.equals("ISDN 7")) {
                portStr = "57";
            } else if (port.equals("ISDN 8")) {
                portStr = "58";
            } else if (port.equals("ISDN 9")) {
                portStr = "59";
            }

            String postdata = POSTDATA_CALL.replaceAll("\\$PASSWORT",
                    URLEncoder.encode(passwort, "ISO-8859-1"));
            postdata = postdata.replaceAll("\\$NUMMER", number);
            postdata = postdata.replaceAll("\\$NEBENSTELLE", portStr);

            postdata = firmware.getAccessMethod() + postdata;

            String urlstr = "http://"
                    + JFritz.getProperty("box.address", "fritz.box")
                    + "/cgi-bin/webcm";
            data = fetchDataFromURL(urlstr, postdata);
            System.out.println("PASSWORT: " + passwort);
            System.out.println("Nummer: " + number);
            System.out.println("Port: " + portStr);

            System.out.println(urlstr + "?" + postdata);
            System.out.println(data);
        } catch (UnsupportedEncodingException uee) {
        } catch (WrongPasswordException wpe) {
        } catch (IOException ioe) {
        }

    }

}