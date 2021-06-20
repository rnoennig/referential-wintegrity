package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import domain.DatabaseTable;
import domain.DatabaseTableCell;
import domain.DatabaseTableRow;
import domain.TableCell;
import domain.ri.TableDefinition;

/**
 * TableView with the content of physical database columns as table columns
 *
 */
public class DatabaseTableView extends TableView<DatabaseTableRow, DatabaseTableCell> {
	private static final long serialVersionUID = 1L;

	public DatabaseTableView(DatabaseTable table) {
		super(table, false);
		TableModel model = this.jTable.getModel();
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
			@Override
			public int getViewRowCount() {
				return super.getViewRowCount();
			}
		};
		this.jTable.setRowSorter(sorter);

		TableDefinition tableDefinition = table.getTableDefinition();
		if (tableDefinition.getPrimaryKey().isPresent()) {
			List<RowSorter.SortKey> sortKeys = new ArrayList<>();
			for (Integer pkIndex : tableDefinition.getPrimaryKeyColumnIndexes()) {
				sortKeys.add(new RowSorter.SortKey(pkIndex.intValue(), SortOrder.ASCENDING));
			}
			sorter.setSortKeys(sortKeys);
		}
	}
	
	public DatabaseTable getTable() {
		return (DatabaseTable) this.table;
	}
	
	@Override
	protected void renderCell(JLabel tableCellRenderer, Component result, TableCell cell) {
		// sets the text of the cell
		super.renderCell(tableCellRenderer, result, cell);
		
		result.setForeground(Color.BLACK);
		if (((DatabaseTableCell)cell).isPrimaryKey()) {
			result.setFont(result.getFont().deriveFont(Font.BOLD, result.getFont().getSize()));
		}
		if (((DatabaseTableCell)cell).isForeignKey()) {
			result.setForeground(Color.RED);
		}
	}
	
	protected String renderCellType(Object cellValue) {
		try {
			// TODO how to make this modular
			if (cellValue instanceof oracle.sql.TIMESTAMPTZ) {
				return ((oracle.sql.TIMESTAMPTZ) cellValue).localDateTimeValue().toString();
			}
		} catch (SQLException e) {
			// ignore
		}
		
		return super.renderCellType(cellValue);
	}
}
