package domain;

import java.util.Optional;

import domain.ri.ColumnDefinition;
import domain.ri.PrimaryKey;
import domain.ri.TableDefinition;

/**
 * Cell corresponding to a database query result's column
 */
public class DatabaseTableCell extends TableCell {
	
	private TableDefinition tableDefinition;
	private ColumnDefinition columnDefinition;

	public DatabaseTableCell(TableDefinition tableDefinition, ColumnDefinition columnDefinition, Object value) {
		super(value);
		this.tableDefinition = tableDefinition;
		this.columnDefinition = columnDefinition;
	}

	public DatabaseTableCell(TableDefinition tableDefinition, ColumnDefinition columnDefinition, Object value, boolean header) {
		super(value, header);
		this.tableDefinition = tableDefinition;
		this.columnDefinition = columnDefinition;
	}

	public DatabaseTableCell(DatabaseTableCell tableCell) {
		this(tableCell.getTableDefinition(), tableCell.getColumnDefinition(), tableCell.getValue(), tableCell.isHeader());
	}

	private ColumnDefinition getColumnDefinition() {
		return this.columnDefinition;
	}

	private TableDefinition getTableDefinition() {
		return this.tableDefinition;
	}

	public boolean isPrimaryKey() {
		Optional<PrimaryKey> primaryKey = tableDefinition.getPrimaryKey();
		if (primaryKey.isPresent()) {
			return primaryKey.get().getColumnNames().contains(this.columnDefinition.getColumnName());
		}
		return false;
	}

	public boolean isForeignKey() {
		return tableDefinition.getForeignKeys().stream()
				.flatMap(fk -> fk.getColumnDefinitions().stream())
				.anyMatch(cd -> cd.equals(this.columnDefinition));
	}

}
