package ui;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import domain.DatabaseTableViewGroup;

public class DependentRowsTab extends Tab {

	public static final String COMMAND_EXPORT_INSERT = "export_insert";
	public static final String COMMAND_EXPORT_DELETE = "export_delete";
	public static final String COMMAND_REFRESH = "refresh";
	private JMenuItem exportInsertMenuItem;
	private JMenuItem exportDeleteMenuItem;
	private JMenuItem refreshMenuItem;

	public DependentRowsTab(JTabbedPane tabPane, String title) {
		super(tabPane, title);
		
		JPopupMenu menu = new JPopupMenu();
		exportInsertMenuItem = new JMenuItem("Export as INSERT statements");
		exportInsertMenuItem.setActionCommand(COMMAND_EXPORT_INSERT);
		menu.add(exportInsertMenuItem);

		exportDeleteMenuItem = new JMenuItem("Export as DELETE statements");
		exportDeleteMenuItem.setActionCommand(COMMAND_EXPORT_DELETE);
		menu.add(exportDeleteMenuItem);
		
		refreshMenuItem = new JMenuItem("Refresh");
		refreshMenuItem.setActionCommand(COMMAND_REFRESH);
		menu.add(refreshMenuItem);
		
		this.tabTitlePanel.setComponentPopupMenu(menu);
	}

	/**
	 * @see DatabaseTableViewGroup which handles all commands
	 */
	@Override
	public void addActionListener(ActionListener listener) {
		super.addActionListener(listener);
		exportInsertMenuItem.addActionListener(listener);
		exportDeleteMenuItem.addActionListener(listener);
		refreshMenuItem.addActionListener(listener);
	}
}
