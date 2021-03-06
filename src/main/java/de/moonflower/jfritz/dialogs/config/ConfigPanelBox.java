package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;

public class ConfigPanelBox extends JPanel implements ActionListener, ConfigPanel
{
	private static final long serialVersionUID = -9082897750502622409L;
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigPanelBox()
	{
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel topPane = new JPanel();
		JLabel title = new JLabel();
		title.setText("Configured Boxes"); // @todo: messages.getMessage
		topPane.add(title);
		this.add(topPane, BorderLayout.NORTH);

		// Content panel contains table and modification buttons to the right
		JPanel contentPane = new JPanel();

		contentPane.setLayout(new BorderLayout());

		JPanel tablePane = new JPanel();

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));

		JButton addButton = new JButton(messages.getMessage("add"));
		buttonPane.add(addButton);

		JButton editButton = new JButton(messages.getMessage("edit"));
		buttonPane.add(editButton);

		JButton removeButton = new JButton(messages.getMessage("remove"));
		buttonPane.add(removeButton);

		JButton moveUpButton = new JButton(messages.getMessage("move_up"));
		buttonPane.add(moveUpButton);

		JButton moveDownButton = new JButton(messages.getMessage("move_down"));
		buttonPane.add(moveDownButton);

		contentPane.add(tablePane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.EAST);

		this.add(new JScrollPane(contentPane), BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public void cancel() {
		// TODO Auto-generated method stub

	}

	public String getHelpUrl() {
		return "No help available";
	}

	public JPanel getPanel() {
		return this;
	}

	public String getPath() {
		return "Boxes---";
	}

	public void loadSettings() {
		// TODO Auto-generated method stub

	}

	public void saveSettings() throws WrongPasswordException,
			InvalidFirmwareException, IOException {
		// TODO Auto-generated method stub

	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}

}
