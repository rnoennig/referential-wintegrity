package domain;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import ui.Main;

public abstract class DependentDatabaseTableRowsQuery {

	private DatabaseTableRow[] rows;
	
	public DependentDatabaseTableRowsQuery(DatabaseTableRow... rows) {
		this.rows = rows;
	}
	
	protected List<DatabaseTable> doInBackground() throws Exception {
		// TODO support more than one row as query input
		return Main.getInstance().getJdbcProvider().getDependentRows(rows[0]);
	}
	
	public void execute() {
		SwingWorker<List<DatabaseTable>, Void> swingWorker = new SwingWorker<List<DatabaseTable>, Void>() {
			@Override
			protected List<DatabaseTable> doInBackground() throws Exception {
				return DependentDatabaseTableRowsQuery.this.doInBackground();
			}

			@Override
			protected void done() {
				try {
					DependentDatabaseTableRowsQuery.this.done(get());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		swingWorker.execute();
	}

	protected abstract void done(List<DatabaseTable> result);

}
