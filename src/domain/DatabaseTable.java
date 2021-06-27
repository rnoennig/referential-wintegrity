package domain;

import java.util.ArrayList;
import java.util.List;

import domain.ri.TableDefinition;

/**
 * Database table model representing a table of {@link DatabaseTableRow}s
 */
public class DatabaseTable extends Table<DatabaseTableRow, DatabaseTableCell> {
	
	private TableDefinition tableDefinition;

	public DatabaseTable() {
	}

	/**
	 * Initializes this instance based on the given {@link DatabaseTable}
	 * @param table
	 */
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
	
	@Override
	public List<DatabaseTableRow> getTableRows() {
		return super.getTableRows();
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
