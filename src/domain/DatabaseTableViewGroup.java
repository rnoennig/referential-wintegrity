package domain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ui.ClipboardUtil;
import ui.DatabaseTableView;
import ui.DependentDatabaseTableRowsQuery;
import ui.DependentRowsTab;
import ui.MainWindow;

/**
 * Groups together {@link DatabaseTableView}s that belong to a result of a {@link DependentDatabaseTableRowsQuery}
 *
 */
public class DatabaseTableViewGroup implements ActionListener {
	private List<DatabaseTableView> databaseTableViews = new ArrayList<>();
	private DependentRowsTab tab;
	
	public DatabaseTableViewGroup(DependentRowsTab tab) {
		this.tab = tab;
	}

	public void addAll(List<DatabaseTableView> databaseTableViews) {
		this.databaseTableViews.addAll(databaseTableViews);
		this.tab.addAllContentComponents(databaseTableViews);
	}
	
	public void clear() {
		this.tab.clear();
		this.databaseTableViews = new ArrayList<>();
	}

	/**
	 * @see DependentRowsTab for defining all commands
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(DependentRowsTab.COMMAND_EXPORT_INSERT)) {
			exportTablesAsInsertStatements();
		}
		if (e.getActionCommand().equals(DependentRowsTab.COMMAND_EXPORT_DELETE)) {
			exportTablesAsDeleteStatements();
		}
		if (e.getActionCommand().equals(DependentRowsTab.COMMAND_REFRESH)) {
			this.tab.refresh();
		}
	}

	private void exportTablesAsInsertStatements() {
		List<DatabaseTable> tables = databaseTableViews.stream()
				.map(dbtv -> dbtv.getTable())
				.collect(Collectors.toList());
		System.out.println("Export this group as insert statements:");
		List<String> statements = tables.stream()
				.distinct()
				.flatMap(t -> MainWindow.getInstance().getJdbcProvider().toInsertStatements(t).stream())
				.collect(Collectors.toList());
		String statementsAsText = statements.stream().collect(Collectors.joining("\n"));
		ClipboardUtil.copyText(statementsAsText);
		for (String stmt : statements) {
			System.out.println(stmt);
		}
	}

	private void exportTablesAsDeleteStatements() {
		List<DatabaseTable> tables = databaseTableViews.stream()
				.map(dbtv -> dbtv.getTable())
				.collect(Collectors.toList());
		// delete in reverse hierarchical order to preserve referential integrity
		Collections.reverse(tables);
		System.out.println("Export this group as delete statements:");
		List<String> statements = tables.stream()
				.distinct()
				.flatMap(t -> MainWindow.getInstance().getJdbcProvider().toDeleteStatements(t).stream())
				.collect(Collectors.toList());
		String statementsAsText = statements.stream().collect(Collectors.joining("\n"));
		ClipboardUtil.copyText(statementsAsText);
		for (String stmt : statements) {
			System.out.println(stmt);
		}
	}
	
}
