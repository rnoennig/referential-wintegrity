package ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

/**
 * 
 * Contains a component for the tab title as well as a component for the tab content 
 *
 */
public class Tab {

	protected String title;
	protected QueryResultTabbedPane tabPane;
	protected JLabel tabTitleLabel;
	protected JPanel panel;
	protected JScrollPane scrollPane;
	
	protected JPopupMenu menu;
	private JMenuItem closeMenuItem;
	
	public static final String COMMAND_CLOSE = "close";
	
	public Tab(QueryResultTabbedPane tabPane, String title) {
		this.tabPane = tabPane;
		this.title = title;
		
		this.panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		scrollPane = new JScrollPane(panel);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(4);
		scrollPane.getVerticalScrollBar().setUnitIncrement(4);

		this.tabPane.addTab(this.title, scrollPane);
		this.tabTitleLabel = new JLabel(this.title);
		this.tabTitleLabel.setBackground(new Color(0,0,0,0));
		this.tabTitleLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tabPane.setSelectedIndex(tabPane.indexOfComponent(scrollPane));
			}
		});
		this.tabPane.setTabComponentAt(tabPane.indexOfComponent(scrollPane), this.tabTitleLabel);
		tabPane.addTab(scrollPane, this);

		menu = new JPopupMenu();
		
		closeMenuItem = new JMenuItem("Close");
		closeMenuItem.setActionCommand(COMMAND_CLOSE);
		Action closeCommand = new AbstractAction("Close") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		};
		closeMenuItem.addActionListener(closeCommand);
		menu.add(closeMenuItem);
		
		this.tabTitleLabel.setComponentPopupMenu(menu);
	}
	
	protected void onClose() {
		close();
	}

	/**
	 * subclasses may implement more actions
	 * @param listener
	 */
	public void addActionListener(ActionListener listener) {
		// intentionally empty
	}

	public void addAllContentComponents(List<? extends JComponent> components) {
		for (JComponent component : components) {
			panel.add(component);
		}
		panel.revalidate();
	}

	public void clear() {
		panel.removeAll();
		panel.revalidate();
	}

	public boolean isActive() {
		return tabPane.getSelectedComponent() == this.scrollPane;
	}

	public QueryResultTabbedPane getTabPane() {
		return tabPane;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	/**
	 * make this tab the selected tab
	 */
	public void select() {
		this.tabPane.setSelectedComponent(this.scrollPane);
	}

	public void close() {
		int indexOfTabComponent = this.tabPane.indexOfComponent(this.scrollPane);
		this.tabPane.removeTabAt(indexOfTabComponent);
	}
}
