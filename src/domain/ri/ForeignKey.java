package domain.ri;

import java.util.List;

import domain.TableRow;

public class ForeignKey {
	private TableDefinition tableDefinition;
	private String name;
	private List<ColumnDefinition> columnDefinitions;
	private PrimaryKey referencedPrimaryKey;

	public ForeignKey(String name, List<ColumnDefinition> columnDefinition, PrimaryKey referencedPrimaryKey) {
		this.name = name;
		this.columnDefinitions = columnDefinition;
		this.referencedPrimaryKey = referencedPrimaryKey;
	}

	public void setTableDefinition(TableDefinition tableDefinition) {
		this.tableDefinition = tableDefinition;
		for (ColumnDefinition columnDefinition : columnDefinitions) {
			columnDefinition.setTableDefinition(tableDefinition);
		}
	}
	
	public String getTableName() {
		if (this.tableDefinition != null) {
			return this.tableDefinition.getTableName();
		}
		return null;
	}

	public List<ColumnDefinition> getColumnDefinitions() {
		return columnDefinitions;
	}
	
	public String getName() {
		return name;
	}

	public String buildJoinByForeignKeyExpression(TableRow row) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < columnDefinitions.size(); i++) {
			if (i > 0) {
				sb.append(" AND ");
			}
			sb.append(columnDefinitions.get(i).getColumnName());
			sb.append(" = ");
			sb.append(row.getColumnValue(referencedPrimaryKey.getColumnDefinitions().get(i).getColumnName()));
		}
		return sb.toString();
	}
	
	public String buildJoinByPrimaryKeyExpression(TableRow row) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < columnDefinitions.size(); i++) {
			if (i > 0) {
				sb.append(" AND ");
			}
			sb.append(referencedPrimaryKey.getColumnDefinitions().get(i).getColumnName());
			sb.append(" = ");
			sb.append(row.getColumnValue(columnDefinitions.get(i).getColumnName()));
		}
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnDefinitions == null) ? 0 : columnDefinitions.hashCode());
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
		ForeignKey other = (ForeignKey) obj;
		if (columnDefinitions == null) {
			if (other.columnDefinitions != null)
				return false;
		} else if (!columnDefinitions.equals(other.columnDefinitions))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return getTableName() + "." + columnDefinitions + "->" + referencedPrimaryKey;
	}

	public PrimaryKey getReferencedPrimaryKey() {
		return referencedPrimaryKey;
	}
	
}
