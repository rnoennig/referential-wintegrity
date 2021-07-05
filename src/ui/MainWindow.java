package ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
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

public class MainWindow {
	
	private static MainWindow main;

	private JdbcService jdbcProvider;

	private QueryResultTabbedPane queryResultTabPane;

	private JSplitPane eastWestPanel;

	public JdbcService getJdbcProvider() {
		return jdbcProvider;
	}

	public void setJdbcProvider(JdbcService jdbcProvider) {
		this.jdbcProvider = jdbcProvider;
	}
	
	MainWindow() {
		main = this;
	}
	
	public static MainWindow getInstance() {
		return main;
	}

	public void run() {
		FlatDarkLaf.setup();
		UIManager.put("Table.showHorizontalLines", true);
		UIManager.put("Table.showVerticalLines", true);
		UIManager.put("Table.alternateRowColor", new Color(78, 83, 84));
		JFrame frame = new JFrame("Referential Wintegrity");

		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		eastWestPanel = new JSplitPane();
		eastWestPanel.setDividerSize(3);
		frame.add(eastWestPanel);

		queryResultTabPane = new QueryResultTabbedPane();

		createTableSelectionView();

		// setting left component to null prevents a button from being displayed
		eastWestPanel.setLeftComponent(null);
		eastWestPanel.setRightComponent(queryResultTabPane);
		
		SwingWorker<Schema, Void> swingWorker = new SwingWorker<Schema, Void>() {

			@Override
			protected Schema doInBackground() throws Exception {
				return MainWindow.this.jdbcProvider.readSchemaGraph();
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
	private void createTableSelectionView() {
		SwingWorker<Table<TableRow, TableCell>, Void> swingWorker = new SwingWorker<>() {
			@Override
			protected Table<TableRow, TableCell> doInBackground() throws Exception {
				return MainWindow.this.jdbcProvider.selectAllTableNames();
			}

			@Override
			protected void done() {
				try {
					Table<TableRow, TableCell> table = get();
					TableView<TableRow, TableCell> allTablesView = new TableView<>(table, true, false, false);
					allTablesView.setAutoHeight(true);
					allTablesView.addClicklistener(createOnTableClickAdapter(queryResultTabPane));
					eastWestPanel.setLeftComponent(allTablesView);
					
					int maxWidthNeeded = allTablesView.getMaxColumnSize(table.getColumnNames().get(0));
					
					eastWestPanel.setDividerLocation(maxWidthNeeded + 4);
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
	private DatabaseTableQuery<DatabaseTable> createQueryForFetchingAllsTableRows(QueryResultTabbedPane tabPane, String tableName,
			QueryResultTab<DatabaseTable> tab) {
		return new DatabaseTableQuery<>() {
			
			@Override
			protected DatabaseTable doInBackground() throws Exception {
				return MainWindow.this.jdbcProvider.getTableRows(tableName);
			}
			
			@Override
			protected void done(DatabaseTable result) {
				tab.clear();
				DatabaseTableView tableView = new DatabaseTableView(result);
				tableView.setAutoHeight(true);
				tableView.addClicklistener(createDependentRowClickListener());
				tab.addAllContentComponents(Arrays.asList(tableView));
				tab.setTabTooltip(new Date().toString());
			}
		};
	}
	
	private TableViewClickAdapter<DatabaseTableRow, DatabaseTableCell> createDependentRowClickListener() {
		return new TableViewClickAdapter<DatabaseTableRow, DatabaseTableCell>() {
			@Override
			public void cellSelected(DatabaseTableRow row, DatabaseTableCell cell) {
				if (row.hasNoRelations()) {
					// TODO show some kind of error message, that no PK is defined?
					System.err.println("Cannot open dependend[ent|ing] rows because neither unique keys nor foreign keys were found");
					return;
				}
				String tabTitle = row.getUniqueDescription();
				DependentRowsTab dependentRowsTab = new DependentRowsTab(queryResultTabPane, tabTitle);
				
				DatabaseTableViewGroup databaseTableGroup = new DatabaseTableViewGroup(dependentRowsTab);
				DependentDatabaseTableRowsQuery dependentRowsQuery = createQueryForDependentTableRows(dependentRowsTab, row, databaseTableGroup);

				dependentRowsTab.addActionListener(databaseTableGroup);
				dependentRowsTab.setQuery(dependentRowsQuery);
				
				dependentRowsQuery.execute();
				dependentRowsTab.select();
			}
		};
	}
	
	private DependentDatabaseTableRowsQuery createQueryForDependentTableRows(Tab tab, DatabaseTableRow row,
			DatabaseTableViewGroup databaseTableGroup) {
		return new DependentDatabaseTableRowsQuery(row) {
			@Override
			protected void done(List<DatabaseTable> result) {
				List<DatabaseTableView> dependentDatabaseTableViews = new ArrayList<>();
				for (DatabaseTable dependentTables : result) {
					DatabaseTableView databaseTableView = new DatabaseTableView(dependentTables);
					databaseTableView.addClicklistener(createDependentRowClickListener());
					dependentDatabaseTableViews.add(databaseTableView);
				}
				databaseTableGroup.clear();
				databaseTableGroup.addAll(dependentDatabaseTableViews);
				tab.setTabTooltip(new Date().toString());
			}
		};
	}

	private TableViewClickAdapter<TableRow, TableCell> createOnTableClickAdapter(QueryResultTabbedPane tabPane) {
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
