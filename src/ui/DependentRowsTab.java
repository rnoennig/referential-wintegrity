package ui;

import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

public class DependentRowsTab extends Tab {

	public static final String COMMAND_EXPORT_INSERT = "export_insert";
	public static final String COMMAND_EXPORT_DELETE = "export_delete";
	private JMenuItem exportInsertMenuItem;
	private JMenuItem exportDeleteMenuItem;

	public DependentRowsTab(JTabbedPane tabPane, String title) {
		super(tabPane, title);
		
		JPopupMenu menu = new JPopupMenu();
		exportInsertMenuItem = new JMenuItem("Export as INSERT statements");
		exportInsertMenuItem.setActionCommand(COMMAND_EXPORT_INSERT);
		exportDeleteMenuItem = new JMenuItem("Export as DELETE statements");
		exportDeleteMenuItem.setActionCommand(COMMAND_EXPORT_DELETE);
		
		menu.add(exportInsertMenuItem);
		menu.add(exportDeleteMenuItem);
		this.tabTitlePanel.setComponentPopupMenu(menu);
		this.tabTitlePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tabPane.setSelectedIndex(tabPane.indexOfTab(title));
			}
		});
	}

	@Override
	public void addActionListener(ActionListener listener) {
		super.addActionListener(listener);
		exportInsertMenuItem.addActionListener(listener);
		exportDeleteMenuItem.addActionListener(listener);
	}
}
