package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestAustria extends ReverseLookupTestBase {
	private CheckEntry entry;
	private PhoneNumberOld checkNum;

	@BeforeClass
	public static void init() {
		Debug.on();
    	Debug.setVerbose(true);
    	Debug.setDebugLevel(Debug.LS_DEBUG);
		Main.loadProperties(false);
		Main.loadMessages(new Locale("de_DE"));
		JFritz.loadNumberSettings();
	}

	@Test
	public void testTrivadis() {
		checkNum = new PhoneNumberOld("+4313323531", false);
		entry = new CheckEntry(checkNum, "Delphi GmbH", "Trivadis", "Handelskai 94-96", "1200", "Wien");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testKarin() {
		checkNum = new PhoneNumberOld("+4353365227", false);
		entry = new CheckEntry(checkNum, "Böglerhof GmbH u Co KG", "Romantikhotel", "Alpbach , 166", "6236", "Alpbach (T)");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

