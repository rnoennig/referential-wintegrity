package ui;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import dao.JdbcService;
import domain.DatabaseTable;
import domain.DatabaseTableRow;
import domain.DatabaseTableViewGroup;
import domain.Table;
import domain.TableCell;
import domain.TableRow;
import domain.ri.Constraint;
import domain.ri.ForeignKey;
import domain.ri.Schema;
import domain.ri.TableDefinition;
import domain.ri.UniqueConstraint;
import service.JvmArgumentConfig;

public class Main {
	
	private static final Main main = new Main();

	public static void main(String[] args) {
		main.setJdbcProvider(new JdbcService(new JvmArgumentConfig()));
		main.run();
	}

	private JdbcService jdbcProvider;

	public JdbcService getJdbcProvider() {
		return jdbcProvider;
	}

	public void setJdbcProvider(JdbcService jdbcProvider) {
		this.jdbcProvider = jdbcProvider;
	}
	
	public static Main getInstance() {
		return main;
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

		// setting left component to null prevents a button from being displayed
		eastWestPanel.setLeftComponent(null);
		eastWestPanel.setRightComponent(tabPane);

		SwingWorker<Schema, Void> swingWorker = new SwingWorker<Schema, Void>() {

			@Override
			protected Schema doInBackground() throws Exception {
				return Main.this.jdbcProvider.readSchemaGraph();
			}

			@Override
			protected void done() {
//				// DEMO
				try {
					get();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DatabaseTableRow row = jdbcProvider
						.selectRows("greatgrandparent", Arrays.asList("id"),Arrays.asList(Integer.valueOf(1))).getTableRows()
						.get(0);
				String tabTitle = "TEST";
				DependentRowsTab dependentRowsTab = new DependentRowsTab(tabPane, tabTitle);
				addDependentRowsTableViews(dependentRowsTab, (DatabaseTableRow)row);
				int tabIndex = tabPane.getTabCount() - 1;
				tabPane.setSelectedIndex(tabIndex);
//				// DEMO
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
		SwingWorker<Table<TableRow>, Void> swingWorker = new SwingWorker<Table<TableRow>, Void>() {
			@Override
			protected Table<TableRow> doInBackground() throws Exception {
				return Main.this.jdbcProvider.selectAllTableNames();
			}

			@Override
			protected void done() {
				try {
					TableView<TableRow> allTablesView = new TableView<>(get(), true);
					allTablesView.addClicklistener(new ClickAdapter() {
						@Override
						public void cellSelected(TableRow row, TableCell cell) {
							String tableName = cell.getValue().toString();
							Tab tab = new Tab(tabPane, tableName);
							createRowSelectionView(tabPane, tableName, tab.getContentComponent());
							tabPane.setSelectedIndex(tabPane.getTabCount()-1);
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
		SwingWorker<DatabaseTable, Void> swingWorker = new SwingWorker<DatabaseTable, Void>() {
			@Override
			protected DatabaseTable doInBackground() throws Exception {
				return Main.this.jdbcProvider.getTableRows(tableName);
			}

			@Override
			protected void done() {
				try {
					DatabaseTableView tableView = new DatabaseTableView(get());
					tableView.setAutoHeight(true);
					tableView.addClicklistener(createDependentRowClickListener(tabPane));
					panel.add(tableView);
					panel.revalidate();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		swingWorker.execute();
	}
	
	protected ClickAdapter createDependentRowClickListener(JTabbedPane tabPane) {
		return new ClickAdapter() {
			@Override
			public void cellSelected(TableRow row, TableCell cell) {
				Constraint constraint;
				
				TableDefinition tableDefinition = ((DatabaseTable)row.getTable()).getTableDefinition();
				Optional<UniqueConstraint> primaryUniqueConstraints = tableDefinition.getPrimaryUniqueConstraint();
				List<ForeignKey> foreignKeys = tableDefinition.getForeignKeys();
				if (primaryUniqueConstraints.isEmpty() && foreignKeys.isEmpty()) {
					// TODO show some kind of error message, that no PK is defined?
					System.err.println("Cannot open dependend[ent|ing] rows because neither unique keys nor foreign keys were found");
					return;
				}
				if (primaryUniqueConstraints.isPresent()) {
					constraint = primaryUniqueConstraints.get();
				} else {
					constraint = foreignKeys.get(0);
				}
				
				String tabTitle = row.getTableName() + "#" + constraint.getColumnDefinitions() + "="
						+ row.getColumnValues(constraint.getColumnDefinitions());
				DependentRowsTab dependentRowsTab = new DependentRowsTab(tabPane, tabTitle);
				addDependentRowsTableViews(dependentRowsTab, (DatabaseTableRow)row);
				int tabIndex = tabPane.getTabCount() - 1;
				tabPane.setSelectedIndex(tabIndex);
			}
		};
	}

	/**
	 * 
	 * @param tab
	 * @param row
	 * @return a panel with all table and all dependent rows for the given row
	 */
	protected void addDependentRowsTableViews(Tab tab, DatabaseTableRow row) {
		DatabaseTableViewGroup databaseTableGroup = new DatabaseTableViewGroup(tab);
		DependentDatabaseTableRowsQuery dependentRowsQuery = new DependentDatabaseTableRowsQuery(row) {

			@Override
			protected void done(List<DatabaseTable> result) {
				for (DatabaseTable dependentTables : result) {
					DatabaseTableView databaseTableView = new DatabaseTableView(dependentTables);
					databaseTableView.addClicklistener(createDependentRowClickListener(tab.getTabPane()));
					databaseTableGroup.add(databaseTableView);
					// fix scroll issue
					databaseTableView.getScrollPane().addMouseWheelListener(new MouseWheelScrollListener(databaseTableView.getScrollPane()));
				}
			}
		};
		databaseTableGroup.setDependentRowsQuery(dependentRowsQuery);
		tab.addActionListener(databaseTableGroup);
		databaseTableGroup.executeQuery();
	}

}
