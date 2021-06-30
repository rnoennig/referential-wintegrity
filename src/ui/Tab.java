package ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * 
 * Contains a component for the tab title as well as a component for the tab content 
 *
 */
public class Tab {

	protected String title;
	protected JTabbedPane tabPane;
	protected JLabel tabTitleLabel;
	protected JPanel panel;
	protected JScrollPane scrollPane;
	
	protected JPopupMenu menu;
	private JMenuItem closeMenuItem;
	
	public static final String COMMAND_CLOSE = "close";
	
	public Tab(JTabbedPane tabPane, String title) {
		this.tabPane = tabPane;
		this.title = title;
		
		this.panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane(panel);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(4);
		scrollPane.getVerticalScrollBar().setUnitIncrement(4);

		this.tabPane.addTab(this.title, scrollPane);
		this.tabPane.setTabComponentAt(tabPane.indexOfComponent(scrollPane), this.tabTitleLabel);
		this.tabTitleLabel = new JLabel(this.title);
		this.tabTitleLabel.setBackground(new Color(0,0,0,0));
		this.tabTitleLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tabPane.setSelectedIndex(tabPane.indexOfComponent(scrollPane));
			}
		});

		menu = new JPopupMenu();
		
		closeMenuItem = new JMenuItem("Close");
		closeMenuItem.setActionCommand(COMMAND_CLOSE);
		closeMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onClose();
			}
		}); 
		menu.add(closeMenuItem);
		
		this.tabTitleLabel.setComponentPopupMenu(menu);
	}
	
	protected void onClose() {
		int indexOfTabComponent = this.tabPane.indexOfComponent(this.scrollPane);
		this.tabPane.removeTabAt(indexOfTabComponent);
	}

	/**
	 * subclasses may implement more actions
	 * @param listener
	 */
	public void addActionListener(ActionListener listener) {
		// intentionally empty
	}

	public void addContentComponent(JComponent component) {
		panel.add(component);
		panel.revalidate();
	}

	public void clear() {
		panel.removeAll();
		panel.revalidate();
	}

	public boolean isActive() {
		return tabPane.getSelectedComponent() == this.scrollPane;
	}

	public JTabbedPane getTabPane() {
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
}
