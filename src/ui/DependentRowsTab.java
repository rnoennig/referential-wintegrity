package ui;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import domain.DatabaseTable;
import domain.DatabaseTableViewGroup;

/**
 * Tab component that can send out actions for exporting data as SQL inserts and
 * deletes
 * 
 *
 */
public class DependentRowsTab extends QueryResultTab<List<DatabaseTable>> {

	public static final String COMMAND_EXPORT_INSERT = "export_insert";
	public static final String COMMAND_EXPORT_DELETE = "export_delete";
	private JMenuItem exportInsertMenuItem;
	private JMenuItem exportDeleteMenuItem;

	public DependentRowsTab(JTabbedPane tabPane, String title) {
		super(tabPane, title);

		exportInsertMenuItem = new JMenuItem("Export as INSERT statements");
		exportInsertMenuItem.setActionCommand(COMMAND_EXPORT_INSERT);
		menu.add(exportInsertMenuItem);

		exportDeleteMenuItem = new JMenuItem("Export as DELETE statements");
		exportDeleteMenuItem.setActionCommand(COMMAND_EXPORT_DELETE);
		menu.add(exportDeleteMenuItem);
	}

	/**
	 * @see DatabaseTableViewGroup which handles these commands
	 */
	@Override
	public void addActionListener(ActionListener listener) {
		super.addActionListener(listener);
		exportInsertMenuItem.addActionListener(listener);
		exportDeleteMenuItem.addActionListener(listener);
	}
}
