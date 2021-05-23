package ui;
import java.util.List;

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
        
        TableView allTablesView = createTableSelectionView(tabPane);
        eastWestPanel.setLeftComponent(allTablesView);
        
		
        eastWestPanel.setRightComponent(tabPane);
        
        SwingWorker<Schema, Void> swingWorker = new SwingWorker<Schema, Void>() {

			@Override
			protected Schema doInBackground() throws Exception {
				return Main.this.jdbcProvider.readSchemaGraph();
			}
			
			@Override
			protected void done() {
				// DEMO
		        //TableRow row = jdbcProvider.selectRows("child", "select * from child where id = 16").getTableRows().get(0);
		        TableRow row = jdbcProvider.selectRows("greatgrandparent", "select * from greatgrandparent where id = 1").getTableRows().get(0);
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
	 * @return a view with all tables
	 */
	protected TableView createTableSelectionView(JTabbedPane tabPane) {
		TableView allTablesView = new TableView(this.jdbcProvider.getTables());
        allTablesView.addClicklistener(new ClickAdapter() {
        	@Override
        	public void cellSelected(TableRow row, String cellValue) {
        		String tableName = cellValue;
        		JPanel panel = addTab(tabPane, tableName);
        		panel.add(createRowSelectionView(tabPane, tableName));
        	}
        });
		return allTablesView;
	}
	
	/**
	 * 
	 * @param tabPane
	 * @param tableName
	 * @return a view with all rows of the given table
	 */
	protected TableView createRowSelectionView(JTabbedPane tabPane, String tableName) {
		TableView tableView = new TableView(this.jdbcProvider.getTableRows(tableName));
		tableView.addClicklistener(new ClickAdapter() {
        	@Override
        	public void cellSelected(TableRow row, String cellValue) {
        		String tabTitle = tableName + "#" + row.getTable().getPrimaryKeys()[0] + "=" + row.getColumnValue(row.getTable().getPrimaryKeys()[0]);
        		JPanel panel = addTab(tabPane, tabTitle);
				addDependentRowsTableViews(panel, row);
        	}
        });
		return tableView;
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
			panel.add(new TableView(dependentTables));
		}
	}

	/**
	 * adds a new tab to the tab pane
	 * @param tabPane
	 * @param tabTitle
	 * @param component
	 * @return 
	 */
	protected JPanel addTab(JTabbedPane tabPane, String tabTitle) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JScrollPane scrollpane = new JScrollPane(panel);
		tabPane.addTab(tabTitle, scrollpane);
		
//		panel.validate();
		return panel;
	}

}
