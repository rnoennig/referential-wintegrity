package domain;

import java.util.List;

/**
 * Represents a set of {@link TableRow}s
 * @author wiesel
 *
 */
public class Table {
	List<? extends TableRow> data;
	List<TableCell> header;
	String[] columnNames;
	private String tableName;
	public List<? extends TableRow> getTableRows() {
		return data;
	}

	public Table setData(List<? extends TableRow> data) {
		this.data = data;
		return this;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String... columnNames) {
		this.columnNames = columnNames;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getRowCount() {
		return this.data.size();
	}

	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
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
		Table other = (Table) obj;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

	public List<TableCell> getTableHeader() {
		return header;
	}

	public void setHeader(List<TableCell> header) {
		this.header = header;
	}
}
