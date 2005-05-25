/*
 *
 * Created on 10.04.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.Component;
import java.awt.Toolkit;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * This is the renderer for the call type cell of the table, which shows a small
 * icon.
 *
 * @author Arno Willig
 */
public class NumberCellRenderer extends DefaultTableCellRenderer {
	JFritzProperties properties;

	ResourceBundle messages;

	final ImageIcon imagePhone, imageHandy, imageHome, imageWorld, imageFreeCall;

	final ImageIcon imageD1, imageD2, imageO2, imageEplus, imageSipgate;

	final static boolean showHandyLogos = true;

	/**
	 * renders the number field in the CallerTable
	 */
	public NumberCellRenderer(JFritzProperties properties, ResourceBundle messages) {
		super();
		this.properties = properties;
		this.messages = messages;

		imagePhone = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/phone.png")));
		imageHandy = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/handy.png")));
		imageHome = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/home.png")));
		imageWorld = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/world.png")));
		imageD1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass()
						.getResource("/de/moonflower/jfritz/resources/images/d1.png")));
		imageD2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass()
						.getResource("/de/moonflower/jfritz/resources/images/d2.png")));
		imageO2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass()
						.getResource("/de/moonflower/jfritz/resources/images/o2.png")));
		imageEplus = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/eplus.png")));
		imageSipgate = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/sipgate.png")));
		imageFreeCall = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/freecall.png")));

	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		JLabel label = (JLabel) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (value != null) {
			String number = (String) value;
// FIXME
			number = JFritzUtils.createAreaNumber(number,
					properties.getProperty("country.prefix"),
					properties.getProperty("country.code"),
					properties.getProperty("area.prefix"),
					properties.getProperty("area.code"));
			setToolTipText(number);
			if (number.length() > 4) { // if valid number present, draw icon
				if (ReverseLookup.numberIsMobile(number)) {
					String provider = ReverseLookup.getMobileProvider(number);
					if (provider.equals("")) provider="unknown";

					setToolTipText(messages.getString("cellphone_network")
							+ ": " + provider);
					if (showHandyLogos) {
						if (provider.equals("D1")) {
							label.setIcon(imageD1);
						} else if (provider.equals("D2")) {
							label.setIcon(imageD2);
						} else if (provider.equals("O2")) {
							label.setIcon(imageO2);
						} else if (provider.equals("E+")) {
							label.setIcon(imageEplus);
						} else {
							label.setIcon(imageHandy);
						}
					} else {
						label.setIcon(imageHandy);
					}
				} else if ((number.startsWith(properties
						.getProperty("area.prefix")
						+ properties.getProperty("area.code")+"1988"))||
						(number.startsWith("01801777"))) {
					label.setIcon(imageSipgate);
					setToolTipText(messages.getString("voip_call"));
				} else if (number.startsWith(properties
						.getProperty("area.prefix")
						+ properties.getProperty("area.code"))) {
					label.setIcon(imageHome);
					setToolTipText(messages.getString("local_call"));
				} else if (number.startsWith(properties
						.getProperty("country.prefix"))) {
					label.setIcon(imageWorld);
					setToolTipText(messages.getString("int_call"));
				} else if (number.startsWith("0800")){
					label.setIcon(imageFreeCall);
					setToolTipText(messages.getString("freecall"));
				} else {
					label.setIcon(imagePhone);
					setToolTipText(messages.getString("fixed_network"));
				}
			} else {
				label.setIcon(null);
			}
		}
		return label;
	}
}