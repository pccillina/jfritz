/*
 *
 * Created on 08.05.2005
 *
 */
package de.moonflower.jfritz.callerlist;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.cellrenderer.CallByCallCellRenderer;
import de.moonflower.jfritz.cellrenderer.CallTypeCellRenderer;
import de.moonflower.jfritz.cellrenderer.DateCellRenderer;
import de.moonflower.jfritz.cellrenderer.DurationCellRenderer;
import de.moonflower.jfritz.cellrenderer.NumberCellRenderer;
import de.moonflower.jfritz.cellrenderer.PersonCellRenderer;
import de.moonflower.jfritz.cellrenderer.PortCellRenderer;
import de.moonflower.jfritz.cellrenderer.RouteCellRenderer;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;

/**
 * Creates table of callers
 *
 */
//FIXME Breiten der Spalten gehen verloren
public class CallerTable extends JTable {
	private static final long serialVersionUID = 1;

	private static final String TRUE = "true";

	private static final String FALSE = "false";

	private static final int MAXCOLUMNCOUNT = 9;

	private TableColumn callByCallColumn = null;

	private TableColumn commentColumn = null;

	private TableColumn portColumn = null;

	final CallerTable table = this;

	private JFrame parentFrame;

	private WindowAdapter wl;

	/**
	 * Constructs CallerTable
	 *
	 */
	public CallerTable(JFrame parentFrame, CallerList list) {
		super(list);
		this.parentFrame = parentFrame;
		wl = new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				Debug
						.msg("parent window is closing. writing table width for CallerTable");
				writeColumnStatus();
			}
		};
		this.parentFrame.addWindowListener(wl);
		setTableProperties();
		createColumns();
	}

	/**
	 * sets some properties of the CallerTable
	 */
	private void setTableProperties() {
		setRowHeight(24);
		setAutoCreateColumnsFromModel(false);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setFocusable(true);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		getTableHeader().setReorderingAllowed(true);
		getTableHeader().setResizingAllowed(true);
		getTableHeader().addMouseListener(new ColumnHeaderListener(getModel()));

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// clear selection
					table.clearSelection();
					JFritz.getJframe().setStatus();
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					// Delete selected entries
					((CallerList) getModel()).removeEntries(getSelectedRows());
				}
			}
		});

		addKeyListener(keyListener);

		SelectionListener listener = new SelectionListener(this);
		getSelectionModel().addListSelectionListener(listener);
	}

	/**
	 * Creates the columns of the CallerTable
	 *
	 * @param messages
	 */
	private void createColumns() {
		ColumnHeaderToolTips headerTips = new ColumnHeaderToolTips();

		TableColumn col = getColumnModel().getColumn(0);
		col.setIdentifier("type"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("type")); //$NON-NLS-1$
		col.setCellRenderer(new CallTypeCellRenderer());
		headerTips.setToolTip(col, Main.getMessage("type_desc")); //$NON-NLS-1$
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.type.width", "32"))); //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(1);
		col.setIdentifier("date"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("date")); //$NON-NLS-1$
		col.setCellRenderer(new DateCellRenderer());
		headerTips.setToolTip(col, Main.getMessage("date_desc")); //$NON-NLS-1$
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.date.width", "80"))); //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(2);
		col.setIdentifier("callbycall"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("callbycall")); //$NON-NLS-1$
		headerTips.setToolTip(col, Main.getMessage("callbycall_desc")); //$NON-NLS-1$
		col.setCellRenderer(new CallByCallCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.callbycall.width", "40"))); //$NON-NLS-1$,  //$NON-NLS-2$
		callByCallColumn = col;

		col = getColumnModel().getColumn(3);
		col.setIdentifier("number"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("number")); //$NON-NLS-1$
		col.setCellRenderer(new NumberCellRenderer());
		headerTips.setToolTip(col, Main.getMessage("number_desc")); //$NON-NLS-1$
		col.setCellEditor(new CallCellEditor());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.number.width", "100"))); //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(4);
		col.setIdentifier("participant"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("participant")); //$NON-NLS-1$
		headerTips.setToolTip(col, Main.getMessage("participant_desc")); //$NON-NLS-1$
		//col.setCellEditor(new TextFieldCellEditor());
		col.setCellEditor(new PersonCellEditor((CallerList) getModel()));
		col.setCellRenderer(new PersonCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.participant.width", "100"))); //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(5);
		col.setIdentifier("port"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("port")); //$NON-NLS-1$
		headerTips.setToolTip(col, Main.getMessage("port_desc")); //$NON-NLS-1$
		col.setCellRenderer(new PortCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.port.width", "60"))); //$NON-NLS-1$,  //$NON-NLS-2$
		portColumn = col;

		col = getColumnModel().getColumn(6);
		col.setIdentifier("route"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("route")); //$NON-NLS-1$
		headerTips.setToolTip(col, Main.getMessage("route_desc")); //$NON-NLS-1$
		col.setCellRenderer(new RouteCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.route.width", "120"))); //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(7);
		col.setIdentifier("duration"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("duration")); //$NON-NLS-1$
		headerTips.setToolTip(col, Main.getMessage("duration_desc")); //$NON-NLS-1$
		col.setCellRenderer(new DurationCellRenderer());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.duration.width", "60"))); //$NON-NLS-1$,  //$NON-NLS-2$

		col = getColumnModel().getColumn(8);
		col.setIdentifier("comment"); //$NON-NLS-1$
		col.setHeaderValue(Main.getMessage("comment")); //$NON-NLS-1$
		headerTips.setToolTip(col, Main.getMessage("comment_desc")); //$NON-NLS-1$
		col.setCellEditor(new CommentCellEditor());
		col.setMinWidth(10);
		col.setMaxWidth(1600);
		col.setPreferredWidth(Integer.parseInt(Main.getProperty(
				"column.comment.width", "60"))); //$NON-NLS-1$,  //$NON-NLS-2$
		commentColumn = col;

		TableColumnModel colModel = getColumnModel();
		if (!JFritzUtils.parseBoolean(Main.getProperty(
				"option.showCallByCallColumn", "true"))) { //$NON-NLS-1$, //$NON-NLS-2$
			try {
				// Try to remove Call-By-Call Column
				colModel.removeColumn(colModel.getColumn(colModel
						.getColumnIndex("callbycall"))); //$NON-NLS-1$
				Debug.msg("Hiding call-by-call column"); //$NON-NLS-1$
			} catch (IllegalArgumentException iae) { // No Call-By-Call
				// column found.
			}
		}

		if (!JFritzUtils.parseBoolean(Main.getProperty(
				"option.showCommentColumn", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			try {
				// Try to remove comment column
				colModel.removeColumn(colModel.getColumn(colModel
						.getColumnIndex("comment"))); //$NON-NLS-1$
				Debug.msg("Hiding comment column"); //$NON-NLS-1$
			} catch (IllegalArgumentException iae) { // No comment
				// column found.
			}
		}

		if (!JFritzUtils.parseBoolean(Main.getProperty(
				"option.showPortColumn", "true"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			try {
				// Try to remove port column
				colModel.removeColumn(colModel.getColumn(colModel
						.getColumnIndex("port"))); //$NON-NLS-1$
				Debug.msg("Hiding port column"); //$NON-NLS-1$
			} catch (IllegalArgumentException iae) { // No port
				// column found.
			}
		}

		for (int i = 0; i < getColumnCount(); i++) {
			String columnName = Main.getProperty("column" + i + ".name", ""); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
			//Debug.msg("column"+i+".name: "+Main.getProperty("column"+i+".name",""));
			if (!columnName.equals("")) { //$NON-NLS-1$
				if (getColumnIndex(columnName) != -1) {
					moveColumn(getColumnIndex(columnName), i);
				}
			}
		}

		getTableHeader().addMouseMotionListener(headerTips);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if ((rowIndex % 2 == 0) && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(new Color(255, 255, 200));
		} else if (!isCellSelected(rowIndex, vColIndex)) {
			// If not shaded, match the table's background
			c.setBackground(getBackground());
		} else {
			c.setBackground(new Color(204, 204, 255));
		}
		return c;
	}

	private void writeColumnStatus() {
		TableColumnModel colModel = getColumnModel();
		for (int i = 0; i < colModel.getColumnCount(); i++) {
			TableColumn col = getColumnModel().getColumn(i);
			Main.setProperty("" + col.getIdentifier(), ""
					+ col.getPreferredWidth());
		}
		try {
			colModel.getColumnIndex("callbycall");
			Main.setProperty("option.showCallByCallColumn", "" + TRUE);
		} catch (IllegalArgumentException iae) {
			Main.setProperty("option.showCallByCallColumn", "" + FALSE);
		}
		try {
			colModel.getColumnIndex("callbycall");
			Main.setProperty("option.showCommentColumn", "" + TRUE);
		} catch (IllegalArgumentException iae) {
			Main.setProperty("option.showCommentColumn", "" + FALSE);
		}
		try {
			colModel.getColumnIndex("callbycall");
			Main.setProperty("option.showPortColumn", "" + TRUE);
		} catch (IllegalArgumentException iae) {
			Main.setProperty("option.showPortColumn", "" + FALSE);
		}

		for (int i = 0; i < MAXCOLUMNCOUNT; i++) {
			try {
				Main.setProperty("" + "column" + i + ".name", ""
						+ colModel.getColumn(i).getIdentifier());
				//Debug.msg(""+"column"+i+".name"+""+colModel.getColumn(i).getIdentifier());
			} catch (IllegalArgumentException iae) {
				Main.setProperty("column" + i + ".name", "");
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				Main.setProperty("column" + i + ".name", "");
			}
		}/*
		 String columnName = Main.getProperty("column"+i+".name",""); //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
		 if (!columnName.equals("")) { //$NON-NLS-1$
		 if (getColumnIndex(columnName) != -1) {
		 moveColumn(getColumnIndex(columnName), i);
		 }
		 }

		 */

	}

	/**
	 * @return Returns the callByCall column
	 */
	public TableColumn getCallByCallColumn() {
		return callByCallColumn;
	}

	/**
	 * @return Returns the comment column
	 */
	public TableColumn getCommentColumn() {
		return commentColumn;
	}

	/**
	 * @return Returns the port column
	 */
	public TableColumn getPortColumn() {
		return portColumn;
	}

	/**
	 *
	 * Bestimmt die Spaltennummer zu einer bestimmten SpaltenID
	 * SpaltenID = type, duration, port, participant etc.
	 *
	 */
	public int getColumnIndex(String columnIdentifier) {
		for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
			TableColumn currentColumn = getColumnModel().getColumn(i);
			if (currentColumn.getIdentifier().toString().equals(
					columnIdentifier)) {
				return i;
			}
		}
		return -1;
	}
}
