package domain.ri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Schema {
	
	Map<String, TableDefinition> tableDefinitionsByTableName = new HashMap<>();
	Map<String, PrimaryKey> primaryKeysByPrimaryKeyName = new HashMap<>();
	Map<String, ForeignKey> foreignKeysByForeignKeyName = new HashMap<>();

	public Optional<TableDefinition> getTableDefinitionByName(String tableName) {
		return Optional.ofNullable(tableDefinitionsByTableName.get(tableName));
	}
	
	public TableDefinition addTable(String tableName) {
		if (tableDefinitionsByTableName.get(tableName) == null) {
			tableDefinitionsByTableName.put(tableName, new TableDefinition(this, tableName));
		}
		return tableDefinitionsByTableName.get(tableName);
	}
	
	public PrimaryKey getPrimaryKeyByName(String primaryKeyName) {
		return primaryKeysByPrimaryKeyName.get(primaryKeyName);
	}

	public PrimaryKey addPrimaryKey(String tableName, String primaryKeyName, List<ColumnDefinition> primaryKeyColumnDefinitions) {
		System.out.println("adding " + primaryKeyColumnDefinitions + " PK to table " + tableName);
		if (primaryKeysByPrimaryKeyName.get(primaryKeyName) == null) {
			PrimaryKey primaryKey = new PrimaryKey(primaryKeyName, primaryKeyColumnDefinitions);
			TableDefinition tableDefinition = this.addTable(tableName);
			tableDefinition.setPrimaryKey(primaryKey);
			primaryKeysByPrimaryKeyName.put(primaryKey.getName(), primaryKey);
		}
		return primaryKeysByPrimaryKeyName.get(primaryKeyName);
	}                               

	public ForeignKey addForeignKey(String tableName, String foreignKeyName, List<ColumnDefinition> foreignKeyColumnDefinitions, PrimaryKey primaryKey) {
		System.out.println("adding " + foreignKeyColumnDefinitions + " FK to table " + tableName);
		if (foreignKeysByForeignKeyName.get(foreignKeyName) == null) {
			ForeignKey foreignKey = new ForeignKey(foreignKeyName, foreignKeyColumnDefinitions, primaryKey);
			TableDefinition tableDefinition = this.addTable(tableName);
			tableDefinition.addForeignKey(foreignKey);
			foreignKeysByForeignKeyName.put(foreignKey.getName(), foreignKey);
		}
		return foreignKeysByForeignKeyName.get(foreignKeyName);
	}

}
