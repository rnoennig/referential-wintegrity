package domain.ri;

/**
 * A named column of a table
 */
public class ColumnDefinition {
	private TableDefinition tableDefinition;
	private String columnName;
	public ColumnDefinition(TableDefinition tableDefinition, String columnName) {
		this.tableDefinition = tableDefinition;
		this.columnName = columnName;
	}
	public String getColumnName() {
		return columnName;
	}
	public String getTableName() {
		if (this.tableDefinition != null) {
			return this.tableDefinition.getTableName();
		}
		return null;
	}
	public void setTableDefinition(TableDefinition tableDefinition) {
		this.tableDefinition = tableDefinition;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((tableDefinition == null) ? 0 : tableDefinition.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnDefinition other = (ColumnDefinition) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (tableDefinition == null) {
			if (other.tableDefinition != null)
				return false;
		} else if (!tableDefinition.equals(other.tableDefinition))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return columnName;
	}
	
	
}
