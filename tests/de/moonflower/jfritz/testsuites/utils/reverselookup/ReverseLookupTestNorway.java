package de.moonflower.jfritz.testsuites.utils.reverselookup;

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.reverselookup.ReverseLookup;

public class ReverseLookupTestNorway extends ReverseLookupTestBase {
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
	public void testFlorentin() {
		checkNum = new PhoneNumberOld("+4773505023", false);
		entry = new CheckEntry(checkNum, "Florentin", "Moser", "Schiötzvei 5", "7020", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}

	@Test
	public void testUlrike() {
		checkNum = new PhoneNumberOld("+4773945687", false);
		entry = new CheckEntry(checkNum, "Ulrike", "Griep", "Loholtbakken 7", "7049", "Trondheim");
		ReverseLookup.lookup(checkNum, entry, true);
		checkEntry(entry);
	}
}

