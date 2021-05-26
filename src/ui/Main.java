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
import domain.DatabaseTable;
import domain.DatabaseTableRow;
import domain.Table;
import domain.TableCell;
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
//				// DEMO
//				try {
//					get();
//				} catch (InterruptedException | ExecutionException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				DatabaseTableRow row = jdbcProvider
//						.selectRows("greatgrandparent", Arrays.asList("id"),Arrays.asList(Integer.valueOf(1))).getTableRows()
//						.get(0);
//				JPanel panel = addTab(tabPane, "TEST");
//				addDependentRowsTableViews(panel, row);
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
		SwingWorker<Table, Void> swingWorker = new SwingWorker<Table, Void>() {
			@Override
			protected Table doInBackground() throws Exception {
				return Main.this.jdbcProvider.selectAllTableNames();
			}

			@Override
			protected void done() {
				try {
					TableView allTablesView = new TableView(get(), true);
					allTablesView.addClicklistener(new ClickAdapter() {
						@Override
						public void cellSelected(TableRow row, TableCell cell) {
							String tableName = cell.getValue().toString();
							JPanel panel = addTab(tabPane, tableName);
							createRowSelectionView(tabPane, tableName, panel);
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
					TableView tableView = new DatabaseTableView(get());
					tableView.addClicklistener(new ClickAdapter() {
						@Override
						public void cellSelected(TableRow row, TableCell cell) {
							// FIXME check unique constraints too, not only primary keys
							Optional<PrimaryKey> primaryKey = ((DatabaseTable)row.getTable()).getTableDefinition().getPrimaryKey();
							if (primaryKey.isEmpty()) {
								// TODO show some kind of error message, that no PK is defined?
								System.err.println("Cannot open dependend[ent|ing] rows because no primary key was found");
								return;
							}
							
							String tabTitle = row.getTableName() + "#" + primaryKey.get().getColumnDefinitions() + "="
									+ row.getColumnValues(primaryKey.get().getColumnDefinitions());
							JPanel tabPanel = addTab(tabPane, tabTitle);
							addDependentRowsTableViews(tabPanel, (DatabaseTableRow)row);
							tabPane.setSelectedIndex(tabPane.getTabCount()-1);
						}
					});
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

	/**
	 * 
	 * @param panel
	 * @param row
	 * @return a panel with all table and all dependant rows for the given row
	 */
	protected void addDependentRowsTableViews(JComponent panel, DatabaseTableRow row) {
		SwingWorker<List<DatabaseTable>, Void> swingWorker = new SwingWorker<List<DatabaseTable>, Void>() {
			@Override
			protected List<DatabaseTable> doInBackground() throws Exception {
				return Main.this.jdbcProvider.getDependentRows(row);
			}

			@Override
			protected void done() {
				try {
					for (DatabaseTable dependentTables : get()) {
						panel.add(new DatabaseTableView(dependentTables));
						panel.revalidate();
					}
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		swingWorker.execute();
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
