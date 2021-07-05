package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

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

	public QueryResultTab(QueryResultTabbedPane tabPane, String title) {
		super(tabPane, title);
		
		refreshMenuItem = new JMenuItem("Refresh");
		refreshMenuItem.setActionCommand(COMMAND_REFRESH);
		menu.add(refreshMenuItem);
	}

	public void setQuery(DatabaseTableQuery<T> query) {
		this.query = query;
		this.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
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

	public void refresh() {
		query.execute();
	}
	
}
