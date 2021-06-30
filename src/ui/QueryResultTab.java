package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import domain.DatabaseTableViewGroup;

/**
 * 
 * Contains a component for the tab title as well as a component for the tab content
 * Is used to display the result of a query
 *
 */
public class QueryResultTab<T> extends Tab {

	public static final String COMMAND_REFRESH = "refresh";

	private JMenuItem refreshMenuItem;
	
	protected DatabaseTableQuery<T> query;

	public QueryResultTab(JTabbedPane tabPane, String title) {
		super(tabPane, title);
		
		refreshMenuItem = new JMenuItem("Refresh");
		refreshMenuItem.setActionCommand(COMMAND_REFRESH);
		menu.add(refreshMenuItem);
		
		GridBagLayout gridbag = new GridBagLayout();
		panel.setLayout(gridbag);
	}

	public void setQuery(DatabaseTableQuery<T> query) {
		this.query = query;
		this.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				query.execute();
			}
		});
	}

	/**
	 * @see DatabaseTableViewGroup which handles all commands
	 */
	@Override
	public void addActionListener(ActionListener listener) {
		super.addActionListener(listener);
		refreshMenuItem.addActionListener(listener);
	}
	
	@Override
	public void addContentComponent(JComponent component) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		this.panel.add(component, c);
		panel.revalidate();
	}
	
}
