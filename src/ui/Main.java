package ui;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import dao.JdbcService;
import domain.DatabaseTable;
import domain.DatabaseTableCell;
import domain.DatabaseTableRow;
import domain.DatabaseTableViewGroup;
import domain.Table;
import domain.TableCell;
import domain.TableRow;
import domain.ri.Schema;
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
		FlatDarkLaf.setup();
		UIManager.put("Table.showHorizontalLines", true);
		UIManager.put("Table.showVerticalLines", true);
		UIManager.put("Table.alternateRowColor", new Color(78, 83, 84));
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
				createDependentRowsTableGroup(dependentRowsTab, (DatabaseTableRow)row);
				dependentRowsTab.select();
//				// DEMO
			}
		};
		swingWorker.execute();

		frame.setVisible(true);
	}

	/**
	 * Fetches all available tables from database
	 * @param tabPane
	 * @param eastWestPanel
	 * @return a view with all tables
	 */
	protected void createTableSelectionView(JTabbedPane tabPane, JSplitPane eastWestPanel) {
		SwingWorker<Table<TableRow, TableCell>, Void> swingWorker = new SwingWorker<>() {
			@Override
			protected Table<TableRow, TableCell> doInBackground() throws Exception {
				return Main.this.jdbcProvider.selectAllTableNames();
			}

			@Override
			protected void done() {
				try {
					TableView<TableRow, TableCell> allTablesView = new TableView<>(get(), true, false, false);
					allTablesView.addClicklistener(createOnTableClickAdapter(tabPane));
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
	 * Creates a query that fetches all rows of a table and populating a tab with the result
	 * @param tabPane
	 * @param tableName
	 * @param tab
	 * @return
	 */
	protected DatabaseTableQuery<DatabaseTable> createQueryForFetchingAllsTableRows(JTabbedPane tabPane, String tableName,
			QueryResultTab<DatabaseTable> tab) {
		return new DatabaseTableQuery<>() {
			
			@Override
			protected DatabaseTable doInBackground() throws Exception {
				return Main.this.jdbcProvider.getTableRows(tableName);
			}
			
			@Override
			protected void done(DatabaseTable result) {
				tab.clear();
				DatabaseTableView tableView = new DatabaseTableView(result);
				tableView.setAutoHeight(true);
				tableView.addClicklistener(createDependentRowClickListener(tabPane));
				tab.addContentComponent(tableView);
			}
		};
	}
	
	protected TableViewClickAdapter<DatabaseTableRow, DatabaseTableCell> createDependentRowClickListener(JTabbedPane tabPane) {
		return new TableViewClickAdapter<DatabaseTableRow, DatabaseTableCell>() {
			@Override
			public void cellSelected(DatabaseTableRow row, DatabaseTableCell cell) {
				if (row.hasNoRelations()) {
					// TODO show some kind of error message, that no PK is defined?
					System.err.println("Cannot open dependend[ent|ing] rows because neither unique keys nor foreign keys were found");
					return;
				}
				String tabTitle = row.getUniqueDescription();
				DependentRowsTab dependentRowsTab = new DependentRowsTab(tabPane, tabTitle);
				DatabaseTableViewGroup databaseTableGroup = createDependentRowsTableGroup(dependentRowsTab, row);
				databaseTableGroup.executeQuery();
				dependentRowsTab.select();
			}
		};
	}

	/**
	 * 
	 * @param tab
	 * @param row
	 */
	protected DatabaseTableViewGroup createDependentRowsTableGroup(Tab tab, DatabaseTableRow row) {
		DatabaseTableViewGroup databaseTableGroup = new DatabaseTableViewGroup(tab);
		DependentDatabaseTableRowsQuery dependentRowsQuery = createQueryForDependentTableRows(tab, row, databaseTableGroup);
		databaseTableGroup.setDependentRowsQuery(dependentRowsQuery);
		tab.addActionListener(databaseTableGroup);
		return databaseTableGroup;
	}

	private DependentDatabaseTableRowsQuery createQueryForDependentTableRows(Tab tab, DatabaseTableRow row,
			DatabaseTableViewGroup databaseTableGroup) {
		return new DependentDatabaseTableRowsQuery(row) {
			@Override
			protected void done(List<DatabaseTable> result) {
				for (DatabaseTable dependentTables : result) {
					DatabaseTableView databaseTableView = new DatabaseTableView(dependentTables);
					databaseTableView.addClicklistener(createDependentRowClickListener(tab.getTabPane()));
					databaseTableGroup.add(databaseTableView);
					// scroll parent when child cannot scroll further but parent could
					databaseTableView.getScrollPane().addMouseWheelListener(new MouseWheelScrollListener(databaseTableView.getScrollPane()));
				}
			}
		};
	}

	private TableViewClickAdapter<TableRow, TableCell> createOnTableClickAdapter(JTabbedPane tabPane) {
		return new TableViewClickAdapter<TableRow, TableCell>() {
			@Override
			public void cellSelected(TableRow row, TableCell cell) {
				String tableName = cell.getValue().toString();
				QueryResultTab<DatabaseTable> tab = new QueryResultTab<>(tabPane, tableName);
				
				DatabaseTableQuery<DatabaseTable> query = createQueryForFetchingAllsTableRows(tabPane, tableName, tab);
				query.execute();
				
				tab.setQuery(query);
				tab.select();
			}
		};
	}

}
