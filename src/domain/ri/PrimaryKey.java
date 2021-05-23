package domain.ri;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKey {

	private TableDefinition tableDefinition;
	private String name;
	private List<ColumnDefinition> columnDefinitions;
	
	private List<ForeignKey> referencingForeignKeys = new ArrayList<>();

	public PrimaryKey(String name, List<ColumnDefinition> columnDefinitions) {
		this.name = name;
		this.columnDefinitions = columnDefinitions;
	}

	public void setTableDefinition(TableDefinition tableDefinition) {
		this.tableDefinition = tableDefinition;
		for (ColumnDefinition columnDefinition : columnDefinitions) {
			columnDefinition.setTableDefinition(tableDefinition);
		}
	}

	public String getName() {
		return name;
	}

	public List<ColumnDefinition> getColumnDefinitions() {
		return columnDefinitions;
	}
	
	public String getTableName() {
		if (this.tableDefinition != null) {
			return this.tableDefinition.getTableName();
		}
		return null;
	}

	@Override
	public String toString() {
		return getTableName() + "." + columnDefinitions;
	}

	public void addReferencingForeignKey(ForeignKey foreignKey) {
		if (!referencingForeignKeys.contains(foreignKey)) {
			referencingForeignKeys.add(foreignKey);
		}
	}

	public List<ForeignKey> getReferencingForeignKeys() {
		return referencingForeignKeys;
	}

}
