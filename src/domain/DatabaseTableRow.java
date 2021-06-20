package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.ri.ColumnDefinition;
import domain.ri.Constraint;
import domain.ri.ForeignKey;
import domain.ri.TableDefinition;
import domain.ri.UniqueConstraint;

/**
 * Represents a record originating from a database table row 
 *
 */
public class DatabaseTableRow extends TableRow {
	public DatabaseTableRow(DatabaseTable table, List<DatabaseTableCell> row) {
		super(table, row);
	}
	
	public DatabaseTableRow(DatabaseTableRow selectedRow) {
		super(new DatabaseTable(selectedRow.getTable()), new ArrayList<>(selectedRow.getValues()));
	}

	@Override
	public DatabaseTable getTable() {
		return (DatabaseTable)super.getTable();
	}
	
	/**
	 * 
	 * @return a unique description of this table row if possible
	 */
	// TODO compare with #toString()
	public String getUniqueDescription() {
		
		TableDefinition tableDefinition = ((DatabaseTable)this.getTable()).getTableDefinition();
		Optional<UniqueConstraint> primaryUniqueConstraints = tableDefinition.getPrimaryUniqueConstraint();
		List<ForeignKey> foreignKeys = tableDefinition.getForeignKeys();
		StringBuilder tabTitle = new StringBuilder(this.getTableName());

		Constraint constraint = null;
		if (primaryUniqueConstraints.isPresent()) {
			constraint = primaryUniqueConstraints.get();
		} else if (!foreignKeys.isEmpty()) {
			constraint = foreignKeys.get(0);
		}
		if (constraint != null) {
			tabTitle.append("#");
			tabTitle.append(constraint.getColumnDefinitions());
			tabTitle.append("=");
			tabTitle.append(this.getColumnValues(constraint.getColumnDefinitions()));
		}
		 
		return tabTitle.toString();
	}

	/**
	 * 
	 * @return <tt>true</tt> if there are no unique and foreign constraints for this row
	 */
	public boolean hasNoRelations() {
		TableDefinition tableDefinition = ((DatabaseTable)this.getTable()).getTableDefinition();
		Optional<UniqueConstraint> primaryUniqueConstraints = tableDefinition.getPrimaryUniqueConstraint();
		List<ForeignKey> foreignKeys = tableDefinition.getForeignKeys();
		return primaryUniqueConstraints.isEmpty() && foreignKeys.isEmpty();
	}

	@Override
	public String toString() {
		UniqueConstraint uniqueConstraint = null;
		TableDefinition tableDefinition = ((DatabaseTable)this.table).getTableDefinition();
		String tableName = getTable().getTableName();
		
		Optional<UniqueConstraint> primaryUniqueKey = tableDefinition.getPrimaryUniqueConstraint();
		if (primaryUniqueKey.isPresent()) {
			uniqueConstraint = primaryUniqueKey.get();
			List<ColumnDefinition> ukColDefs = uniqueConstraint.getColumnDefinitions();
			return "["+tableName+":"+ukColDefs+"="+getColumnValues(ukColDefs)+"]";
		}
		
		List<ForeignKey> foreignKeys = tableDefinition.getForeignKeys();
		if (!foreignKeys.isEmpty()) {
			// TODO get the first not-null foreign key
			List<ColumnDefinition> columnDefinitions = foreignKeys.get(0).getColumnDefinitions();
			return "["+tableName+":"+columnDefinitions+"="+getColumnValues(columnDefinitions)+"]";
		}
		
		if (table == null || !table.getTableRows().isEmpty()) {
			return "["+tableName+": row #"+table.getTableRows().indexOf(this)+"]";
		}
		
		return "["+tableName+": row "+this.getValues()+"]";
	}
}
