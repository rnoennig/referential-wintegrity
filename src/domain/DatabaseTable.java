package domain;

import java.util.List;

import domain.ri.TableDefinition;

public class DatabaseTable extends Table {
	
	private TableDefinition tableDefinition;

	public TableDefinition getTableDefinition() {
		return this.tableDefinition;
	}

	public void setTableDefinition(TableDefinition tableDefinition) {
		this.tableDefinition = tableDefinition;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<DatabaseTableRow> getTableRows() {
		return (List<DatabaseTableRow>)super.getTableRows();
	}
	
	@Override
	public DatabaseTable setData(List<? extends TableRow> data) {
		this.data = data;
		return this;
	}

}
