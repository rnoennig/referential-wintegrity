package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.ri.ColumnDefinition;
import domain.ri.TableDefinition;
import domain.ri.UniqueConstraint;

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
		
		return "["+tableName+": row #"+table.getTableRows().indexOf(this)+"]";
	}
}
