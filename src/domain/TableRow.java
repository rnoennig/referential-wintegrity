package domain;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a record originating from a database table row 
 *
 */
public class TableRow {
	private Table table;
	
	private String[] values;
	public TableRow(Table table, List<String> values) {
		this.table = table;
		this.values = values.toArray(new String[0]);
	}
	
	public String getTableName() {
		return table.getTableName();
	}
	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public String[] getValues() {
		return values;
	}
	public void setValues(String[] values) {
		this.values = values;
	}

	public String getColumnValue(String columnName) {
		List<String> columnNames = Arrays.stream(this.table.getColumnNames()).map(x -> x.toLowerCase()).collect(Collectors.toList());
		int columnIndex = columnNames.indexOf(columnName.toLowerCase());
		return this.values[columnIndex];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		result = prime * result + Arrays.hashCode(values);
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
		TableRow other = (TableRow) obj;
		if (table == null) {
			if (other.table != null)
				return false;
		} else if (!table.equals(other.table))
			return false;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String[] primaryKeys = getTable().getPrimaryKeys();
		String tableName = getTable().getTableName();
		String pkNames = Arrays.stream(primaryKeys).collect(Collectors.joining(","));
		String pkValues = Arrays.stream(primaryKeys).map(col -> getColumnValue(col)).collect(Collectors.joining(","));
		return "["+tableName+":"+pkNames+"="+pkValues+"]";
	}

	public String getColumnValue(int i) {
		return values[i];
	}
	
}
