package domain;

import java.util.List;
import java.util.stream.Collectors;

import domain.ri.ColumnDefinition;

/**
 * Represents a record originating from a database table row 
 *
 */
public class TableRow {
	protected Table table;
	
	private List<? extends TableCell> values;
	public TableRow(Table table, List<? extends TableCell> values) {
		this.table = table;
		this.values = values;
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

	public List<? extends TableCell> getValues() {
		return values;
	}
	public void setValues(List<TableCell> values) {
		this.values = values;
	}

	public Object getColumnValue(String columnName) {
		List<String> columnNames = this.table.getColumnNames().stream().map(String::toLowerCase).collect(Collectors.toList());
		int columnIndex = columnNames.indexOf(columnName.toLowerCase());
		return this.values.get(columnIndex).getValue();
	}

	public Object getColumnValue(int i) {
		return values.get(i).getValue();
	}

	public List<Object> getColumnValues(List<ColumnDefinition> columnDefinitions) {
		return columnDefinitions.stream().map(cd -> getColumnValue(cd.getColumnName())).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		String tableName = getTable().getTableName();
		int rowIndex = table.getTableRows().indexOf(this);
		return "["+tableName+": row #"+rowIndex+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	
	
}
