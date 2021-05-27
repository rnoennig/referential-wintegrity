package domain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ui.DatabaseTableView;
import ui.DependentRowsTab;
import ui.Main;

/**
 * Groups together {@link DatabaseTableView}s that belong to a set of data
 *
 */
public class DatabaseTableViewGroup implements ActionListener {
	List<DatabaseTableView> databaseTableViews = new ArrayList<>();

	public void add(DatabaseTableView databaseTableView) {
		this.databaseTableViews.add(databaseTableView);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(DependentRowsTab.COMMAND_EXPORT_INSERT)) {
			exportTablesAsInsertStatements();
		}
		if (e.getActionCommand().equals(DependentRowsTab.COMMAND_EXPORT_DELETE)) {
			exportTablesAsDeleteStatements();
		}
	}

	private void exportTablesAsInsertStatements() {
		List<DatabaseTable> tables = databaseTableViews.stream()
				.map(dbtv -> dbtv.getTable())
				.collect(Collectors.toList());
		System.out.println("Export this group as insert statements:");
		List<String> statements = tables.stream().flatMap(t -> Main.getInstance().getJdbcProvider().toInsertStatements(t).stream()).collect(Collectors.toList());
		for (String stmt : statements) {
			System.out.println(stmt);
		}
	}

	private void exportTablesAsDeleteStatements() {
		List<DatabaseTable> tables = databaseTableViews.stream()
				.map(dbtv -> dbtv.getTable())
				.collect(Collectors.toList());
		Collections.reverse(tables);
		System.out.println("Export this group as delete statements:");
		List<String> statements = tables.stream().flatMap(t -> Main.getInstance().getJdbcProvider().toDeleteStatements(t).stream()).collect(Collectors.toList());
		for (String stmt : statements) {
			System.out.println(stmt);
		}
	}
	
}
