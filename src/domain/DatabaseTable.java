package domain;

import java.util.ArrayList;
import java.util.List;

import domain.ri.TableDefinition;

public class DatabaseTable extends Table {
	
	private TableDefinition tableDefinition;

	public DatabaseTable() {
	}

	public DatabaseTable(DatabaseTable table) {
		this.header = new ArrayList<>();
		for (TableCell tableCell : table.getTableHeader()) {
			this.header.add(new DatabaseTableCell((DatabaseTableCell) tableCell));
		}
		this.columnNames = table.getColumnNames();
		this.tableName = table.getTableName();
		this.tableDefinition = table.getTableDefinition();
	}

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
	public String toString() {
		return "DatabaseTable [getTableName()=" + getTableName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((tableDefinition == null) ? 0 : tableDefinition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatabaseTable other = (DatabaseTable) obj;
		if (tableDefinition == null) {
			if (other.tableDefinition != null)
				return false;
		} else if (!tableDefinition.equals(other.tableDefinition))
			return false;
		return true;
	}

}
