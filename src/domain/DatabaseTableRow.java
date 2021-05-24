package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import domain.ri.ColumnDefinition;
import domain.ri.PrimaryKey;
import domain.ri.TableDefinition;

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
		TableDefinition tableDefinition = ((DatabaseTable)this.table).getTableDefinition();
		Optional<PrimaryKey> primaryKey = tableDefinition.getPrimaryKey();
		String tableName = getTable().getTableName();
		if (primaryKey.isPresent()) {
			List<ColumnDefinition> pkColDefs = primaryKey.get().getColumnDefinitions();
			return "["+tableName+":"+pkColDefs+"="+getColumnValues(pkColDefs)+"]";
		}
		return "["+tableName+": row #"+table.getTableRows().indexOf(this)+"]";
	}
}
