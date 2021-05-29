package ui;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	protected JPanel tabTitlePanel;
	protected JPanel panel;
	private JScrollPane scrollpane;
	
	public Tab(JTabbedPane tabPane, String title) {
		this.tabPane = tabPane;
		this.title = title;
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		scrollpane = new JScrollPane(panel);
		this.tabPane.addTab(this.title, scrollpane);
		this.tabTitlePanel = new JPanel();
		this.tabTitlePanel.setBackground(new Color(0,0,0,0));
		JLabel tabLabel = new JLabel(this.title);
		this.tabTitlePanel.add(tabLabel);
		this.panel = panel;
		this.tabPane.setTabComponentAt(tabPane.indexOfTab(title), this.tabTitlePanel);
		this.tabTitlePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tabPane.setSelectedIndex(tabPane.indexOfTab(title));
			}
		});
	}
	
	public void addActionListener(ActionListener listener) {
		// subclasses may implement more actions
	}

	public JPanel getContentComponent() {
		return panel;
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
		return tabPane.getSelectedComponent() == this.scrollpane;
	}

	public JTabbedPane getTabPane() {
		return tabPane;
	}

}
