package domain.ri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TableDefinition {
	private Schema schema;
	private String tableName;
	private PrimaryKey primaryKey;
	private List<UniqueConstraint> uniqueConstraints = new ArrayList<>();
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
	
	public void addUniqueConstraint(UniqueConstraint uniqueConstraint) {
		uniqueConstraints.add(uniqueConstraint);
		uniqueConstraint.setTableDefinition(this);
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
		SchemaVisitor schemaVisitor = new SchemaVisitor(new HashSet<>()) {
			@Override
			public boolean visit(TableDefinition tableDefinition) {
				if (this.visited.contains(tableDefinition)) {
					return false;
				}
				if (tableDefinition.equals(tableDefinitionB)) {
					found = true;
				}
				this.visited.add(tableDefinition);
				return true;
			}
		};
		this.acceptForeignKeyVisitor(schemaVisitor);
		return schemaVisitor.isFound();
	}

	public void acceptForeignKeyVisitor(SchemaVisitor schemaVisitor) {
		if (!schemaVisitor.visit(this)) {
			return;
		}
		
		for (ForeignKey foreignKey : this.foreignKeys) {
			Constraint referencedConstraint = foreignKey.getReferencedConstraint();
			String pkTableName = referencedConstraint.getTableName();
			TableDefinition tableDefinitionByName = schema.getTableDefinitionByName(pkTableName).get();
			tableDefinitionByName.acceptForeignKeyVisitor(schemaVisitor);
		}
	}

	public List<Integer> getPrimaryKeyColumnIndexes() {
		return primaryKey.getColumnDefinitions().stream().map(c -> Integer.valueOf(this.columnDefinitions.indexOf(c))).collect(Collectors.toList());
	}

	public boolean hasPrimaryKey() {
		return primaryKey != null;
	}
}
