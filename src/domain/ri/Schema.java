package domain.ri;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * All metadata for a database schema
 */
public class Schema {
	
	Map<String, TableDefinition> tableDefinitionsByName = new HashMap<>();
	Map<String, PrimaryKey> primaryKeysByName = new HashMap<>();
	Map<String, UniqueConstraint> uniqueConstraintsByName = new HashMap<>();
	Map<String, ForeignKey> foreignKeysByName = new HashMap<>();

	public Optional<TableDefinition> getTableDefinitionByName(String tableName) {
		return Optional.ofNullable(tableDefinitionsByName.get(tableName));
	}
	
	public TableDefinition addTable(String tableName) {
		if (tableDefinitionsByName.get(tableName) == null) {
			tableDefinitionsByName.put(tableName, new TableDefinition(this, tableName));
		}
		return tableDefinitionsByName.get(tableName);
	}
	
	public PrimaryKey getPrimaryKeyByName(String primaryKeyName) {
		return primaryKeysByName.get(primaryKeyName);
	}
	
	public UniqueConstraint getUniqueConstraintByName(String primaryKeyName) {
		return uniqueConstraintsByName.get(primaryKeyName);
	}
	
	public PrimaryKey addPrimaryKey(String tableName, String primaryKeyName, List<ColumnDefinition> primaryKeyColumnDefinitions) {
		System.out.println("Reading schema:     add PK " + tableName + "." + primaryKeyColumnDefinitions);
		if (primaryKeysByName.get(primaryKeyName) == null) {
			PrimaryKey primaryKey = new PrimaryKey(primaryKeyName, primaryKeyColumnDefinitions);
			TableDefinition tableDefinition = this.addTable(tableName);
			tableDefinition.setPrimaryKey(primaryKey);
			primaryKeysByName.put(primaryKey.getName(), primaryKey);
			// also add primary key as unique constraint
			if (uniqueConstraintsByName.containsKey(primaryKey.getName())) {
				for (ForeignKey fk : uniqueConstraintsByName.get(primaryKey.getName()).getReferencingForeignKeys()) {
					primaryKey.addReferencingForeignKey(fk);
				}
			}
			System.out.println("Reading schema:     add UK " + tableName + "." + primaryKey.getColumnDefinitions());
			tableDefinition.addUniqueConstraint(primaryKey);
			uniqueConstraintsByName.put(primaryKey.getName(), primaryKey);
		}
		return primaryKeysByName.get(primaryKeyName);
	}
	
	public UniqueConstraint addUniqueConstraint(String tableName, String uniqueConstraintName, List<ColumnDefinition> uniqueConstraintColumnDefinitions) {
		System.out.println("Reading schema:     add UK " + tableName + "." + uniqueConstraintColumnDefinitions);
		if (uniqueConstraintsByName.get(uniqueConstraintName) == null) {
			UniqueConstraint uniqueConstraint = new UniqueConstraint(uniqueConstraintName, uniqueConstraintColumnDefinitions);
			TableDefinition tableDefinition = this.addTable(tableName);
			tableDefinition.addUniqueConstraint(uniqueConstraint);
			uniqueConstraintsByName.put(uniqueConstraint.getName(), uniqueConstraint);
		}
		return uniqueConstraintsByName.get(uniqueConstraintName);
	}

	public ForeignKey addForeignKey(String tableName, String foreignKeyName, List<ColumnDefinition> foreignKeyColumnDefinitions, UniqueConstraint uniqueConstraint) {
		System.out.println("Reading schema:     add FK " + tableName + "." + foreignKeyColumnDefinitions);
		if (foreignKeysByName.get(foreignKeyName) == null) {
			ForeignKey foreignKey = new ForeignKey(foreignKeyName, foreignKeyColumnDefinitions, uniqueConstraint);
			TableDefinition tableDefinition = this.addTable(tableName);
			tableDefinition.addForeignKey(foreignKey);
			foreignKeysByName.put(foreignKey.getName(), foreignKey);
		}
		return foreignKeysByName.get(foreignKeyName);
	}

}
