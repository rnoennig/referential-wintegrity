package domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import domain.ri.ColumnDefinition;
import domain.ri.PrimaryKey;
import domain.ri.TableDefinition;

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
		TableDefinition tableDefinition = this.table.getTableDefinition();
		Optional<PrimaryKey> primaryKey = tableDefinition.getPrimaryKey();
		String tableName = getTable().getTableName();
		if (primaryKey.isPresent()) {
			List<ColumnDefinition> pkColDefs = primaryKey.get().getColumnDefinitions();
			return "["+tableName+":"+pkColDefs+"="+getColumnValues(pkColDefs)+"]";
		}
		return "["+tableName+": row #"+table.getTableRows().indexOf(this)+"]";
	}

	public String getColumnValue(int i) {
		return values[i];
	}

	public List<String> getColumnValues(List<ColumnDefinition> columnDefinitions) {
		return columnDefinitions.stream().map(cd -> getColumnValue(cd.getColumnName())).collect(Collectors.toList());
	}
	
}
