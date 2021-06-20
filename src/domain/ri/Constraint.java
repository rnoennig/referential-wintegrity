package domain.ri;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract constraint of a table
 */
public abstract class Constraint {

	private TableDefinition tableDefinition;
	protected String name;
	protected List<ColumnDefinition> columnDefinitions;
	
	public Constraint(String name, List<ColumnDefinition> columnDefinitions) {
		this.name = name;
		this.columnDefinitions = columnDefinitions;
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

	public List<String> getColumnNames() {
		return columnDefinitions.stream().map(cd -> cd.getColumnName()).collect(Collectors.toList());
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getTableName() + "." + columnDefinitions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnDefinitions == null) ? 0 : columnDefinitions.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Constraint other = (Constraint) obj;
		if (columnDefinitions == null) {
			if (other.columnDefinitions != null)
				return false;
		} else if (!columnDefinitions.equals(other.columnDefinitions))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tableDefinition == null) {
			if (other.tableDefinition != null)
				return false;
		} else if (!tableDefinition.equals(other.tableDefinition))
			return false;
		return true;
	}

}
