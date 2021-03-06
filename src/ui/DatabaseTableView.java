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
import javax.swing.UIManager;

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

	public DatabaseTableView(DatabaseTable table, boolean tableNameVisible) {
		super(table, false, tableNameVisible, true);

		TableDefinition tableDefinition = table.getTableDefinition();
		if (tableDefinition.getPrimaryKey().isPresent()) {
			List<RowSorter.SortKey> sortKeys = new ArrayList<>();
			for (Integer pkIndex : tableDefinition.getPrimaryKeyColumnIndexes()) {
				sortKeys.add(new RowSorter.SortKey(pkIndex.intValue(), SortOrder.ASCENDING));
			}
			this.sorter.setSortKeys(sortKeys);
		}
		
		// scroll parent when child cannot scroll further but parent could
		addScrollPaneMouseWheelListener();
	}
	
	public DatabaseTable getTable() {
		return (DatabaseTable) this.table;
	}
	
	@Override
	protected void renderCell(JLabel tableCellRenderer, Component result, TableCell cell) {
		// sets the text of the cell
		super.renderCell(tableCellRenderer, result, cell);
		Color textColor = UIManager.getColor("controlText");
		// RED 175 75 105
		// ORANGE 175 115 75
		// GREEN 105 175 75
		result.setForeground(textColor);

		if (((DatabaseTableCell)cell).isForeignKey()) {
			result.setFont(result.getFont().deriveFont(Font.ITALIC, result.getFont().getSize()));
		}
		if (((DatabaseTableCell)cell).isPrimaryKey()) {
			result.setFont(result.getFont().deriveFont(Font.BOLD, result.getFont().getSize()));
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

	public void addScrollPaneMouseWheelListener() {
		this.scrollPane.addMouseWheelListener(new MouseWheelScrollListener(scrollPane));
	}
}
