package domain;

import java.util.List;

import ui.TextWidthCalculator;

/**
 * Represents a set of {@link TableRow}s
 *
 */
public class Table<T extends TableRow, U extends TableCell> {
	List<T> data;
	List<TableCell> header;
	List<String> columnNames;
	protected String tableName;
	public List<T> getTableRows() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
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
		return this.columnNames.size();
	}

	public List<TableCell> getTableHeader() {
		return header;
	}

	public void setHeader(List<TableCell> header) {
		this.header = header;
	}

	public int getColumnIndex(String columnName) {
		return this.columnNames.indexOf(columnName);
	}

	/**
	 * 
	 * @param textWidthCalculator
	 * @param columnName
	 * @return size of the column for the longest value in all rows
	 */
	public int getMaxColumnSize(TextWidthCalculator textWidthCalculator, String columnName) {
		String headerText = String.valueOf(header.get(getColumnIndex(columnName)).getValue());
		int maxLength = textWidthCalculator.calculate(headerText);
		for (TableRow tableRow : data) {
			Object columnValue = tableRow.getColumnValue(columnName);
			if (columnValue == null) {
				continue;
			}
			int len = textWidthCalculator.calculate(columnValue.toString());
			maxLength = len > maxLength ? len : maxLength;
		}
		return maxLength;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Table<?,?> other = (Table<?,?>) obj;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
		return result;
	}
}
