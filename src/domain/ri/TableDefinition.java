package domain.ri;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TableDefinition {
	private Schema schema;
	private String tableName;
	private PrimaryKey primaryKey;
	private List<ForeignKey> foreignKeys = new ArrayList<>();
	private List<ColumnDefinition> columnDefinitions;

	public TableDefinition(Schema schema, String tableName) {
		this.schema = schema;
		this.tableName = tableName;
	}

	public Optional<PrimaryKey> getPrimaryKey() {
		return Optional.ofNullable(this.primaryKey);
	}

	public String getTableName() {
		return tableName;
	}

	public void setPrimaryKey(PrimaryKey primaryKey) {
		this.primaryKey = primaryKey;
		primaryKey.setTableDefinition(this);
	}

	public void addForeignKey(ForeignKey foreignKey) {
		foreignKeys.add(foreignKey);
		foreignKey.setTableDefinition(this);
	}

	@Override
	public String toString() {
		return "TableDefinition [tableName=" + tableName + "]";
	}

	public List<ForeignKey> getForeignKeys() {
		return foreignKeys;
	}

	public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
		this.columnDefinitions = columnDefinitions;
	}

	public List<ColumnDefinition> getColumnDefinitions() {
		return this.columnDefinitions;
	}

	public boolean dependsOn(TableDefinition tableDefinitionB) {
		SchemaVisitor schemaVisitor = new SchemaVisitor() {
			@Override
			public boolean visit(TableDefinition tableDefinition) {
				if (tableDefinition.equals(tableDefinitionB)) {
					found = true;
				}
				return false;
			}
		};
		this.acceptForeignKeyVisitor(schemaVisitor);
		return schemaVisitor.isFound();
	}
	
	public void acceptForeignKeyVisitor(SchemaVisitor schemaVisitor) {
		schemaVisitor.visit(this);
		
		List<ForeignKey> foreignKeys = this.getForeignKeys();
		for (ForeignKey foreignKey : foreignKeys) {
			PrimaryKey referencedPrimaryKey = foreignKey.getReferencedPrimaryKey();
			String pkTableName = referencedPrimaryKey.getTableName();
			TableDefinition tableDefinitionByName = schema.getTableDefinitionByName(pkTableName).get();
			tableDefinitionByName.acceptForeignKeyVisitor(schemaVisitor);
		}
	}

	public List<Integer> getPrimaryKeyColumnIndexes() {
		return primaryKey.getColumnDefinitions().stream().map(c -> this.columnDefinitions.indexOf(c)).collect(Collectors.toList());
	}

	public boolean hasPrimaryKey() {
		return primaryKey != null;
	}
}
