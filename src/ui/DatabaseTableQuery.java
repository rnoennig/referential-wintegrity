package ui;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

public abstract class DatabaseTableQuery<T> {

	public DatabaseTableQuery() {
	}
	
	protected abstract T doInBackground() throws Exception;
	
	public void execute() {
		SwingWorker<T, Void> swingWorker = new SwingWorker<T, Void>() {
			@Override
			protected T doInBackground() throws Exception {
				return DatabaseTableQuery.this.doInBackground();
			}

			@Override
			protected void done() {
				try {
					DatabaseTableQuery.this.done(get());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		swingWorker.execute();
	}

	protected abstract void done(T result);

}
