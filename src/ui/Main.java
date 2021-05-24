package ui;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import dao.JdbcService;
import domain.Table;
import domain.TableRow;
import domain.ri.PrimaryKey;
import domain.ri.Schema;

public class Main {

	public static void main(String[] args) {
		Main main = new Main();
		main.setJdbcProvider(new JdbcService());
		main.run();
	}

	private JdbcService jdbcProvider;

	public JdbcService getJdbcProvider() {
		return jdbcProvider;
	}

	public void setJdbcProvider(JdbcService jdbcProvider) {
		this.jdbcProvider = jdbcProvider;
	}

	private void run() {
		JFrame frame = new JFrame("Referential Wintegrity");

		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane eastWestPanel = new JSplitPane();
		eastWestPanel.setDividerSize(3);
		frame.add(eastWestPanel);

		JTabbedPane tabPane = new JTabbedPane();

		createTableSelectionView(tabPane, eastWestPanel);

		eastWestPanel.setRightComponent(tabPane);

		SwingWorker<Schema, Void> swingWorker = new SwingWorker<Schema, Void>() {

			@Override
			protected Schema doInBackground() throws Exception {
				return Main.this.jdbcProvider.readSchemaGraph();
			}

			@Override
			protected void done() {
				// DEMO
				// TableRow row = jdbcProvider.selectRows("child", "select * from child where id
				// = 16").getTableRows().get(0);
				try {
					get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TableRow row = jdbcProvider
						.selectRows("greatgrandparent", "select * from greatgrandparent where id = 1").getTableRows()
						.get(0);
				JPanel panel = addTab(tabPane, "TEST");
				addDependentRowsTableViews(panel, row);
				// DEMO
			}
		};
		swingWorker.execute();

		frame.setVisible(true);
	}

	/**
	 * 
	 * @param tabPane
	 * @param eastWestPanel
	 * @return a view with all tables
	 */
	protected void createTableSelectionView(JTabbedPane tabPane, JSplitPane eastWestPanel) {
		SwingWorker<Table, Void> swingWorker = new SwingWorker<Table, Void>() {
			@Override
			protected Table doInBackground() throws Exception {
				return Main.this.jdbcProvider.getTables();
			}

			@Override
			protected void done() {
				try {
					TableView allTablesView;
					allTablesView = new TableView(get());
					allTablesView.addClicklistener(new ClickAdapter() {
						@Override
						public void cellSelected(TableRow row, String cellValue) {
							String tableName = cellValue;
							JPanel panel = addTab(tabPane, tableName);
							createRowSelectionView(tabPane, tableName, panel);
						}
					});
					eastWestPanel.setLeftComponent(allTablesView);
					eastWestPanel.setDividerLocation(150);
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		swingWorker.execute();
	}

	/**
	 * 
	 * @param tabPane
	 * @param tableName
	 * @param panel
	 * @return a view with all rows of the given table
	 */
	protected void createRowSelectionView(JTabbedPane tabPane, String tableName, JPanel panel) {
		SwingWorker<Table, Void> swingWorker = new SwingWorker<Table, Void>() {
			@Override
			protected Table doInBackground() throws Exception {
				return Main.this.jdbcProvider.getTableRows(tableName);
			}

			@Override
			protected void done() {
				try {
					TableView tableView;
					tableView = new FormattedDatabaseTableView(get());
					tableView.addClicklistener(new ClickAdapter() {
						@Override
						public void cellSelected(TableRow row, String cellValue) {
							Optional<PrimaryKey> primaryKey = row.getTable().getTableDefinition().getPrimaryKey();
							if (primaryKey.isEmpty()) {
								//TODO show some kind of error message, that no PK is defined?
								return;
							}
							String tabTitle = tableName + "#" + primaryKey.get().getColumnDefinitions() + "="
									+ row.getColumnValues(primaryKey.get().getColumnDefinitions());
							JPanel panel = addTab(tabPane, tabTitle);
							addDependentRowsTableViews(panel, row);
						}
					});
					panel.add(tableView);
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		swingWorker.execute();
	}

	/**
	 * 
	 * @param panel
	 * @param row
	 * @return a panel with all table and all dependant rows for the given row
	 */
	protected void addDependentRowsTableViews(JComponent panel, TableRow row) {
		List<Table> dependentRows = this.jdbcProvider.getDependentRows(row);

		for (Table dependentTables : dependentRows) {
			panel.add(new FormattedDatabaseTableView(dependentTables));
		}
	}

	/**
	 * adds a new tab to the tab pane
	 * 
	 * @param tabPane
	 * @param tabTitle
	 * @param component
	 * @return a panel to add components to which will make up the scrollable tab
	 *         pane's content
	 */
	protected JPanel addTab(JTabbedPane tabPane, String tabTitle) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JScrollPane scrollpane = new JScrollPane(panel);
		tabPane.addTab(tabTitle, scrollpane);
		return panel;
	}

}
