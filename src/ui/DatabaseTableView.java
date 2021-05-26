package ui;

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import domain.DatabaseTable;
import domain.DatabaseTableCell;
import domain.TableCell;
import domain.ri.TableDefinition;

/**
 *
 *
 */
public class DatabaseTableView extends TableView {
	private static final long serialVersionUID = 1L;

	public DatabaseTableView(DatabaseTable table) {
		super(table, false);
		TableModel model = this.jTable.getModel();
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) {
			@Override
			public int getViewRowCount() {
//				System.out.println("-----------> getViewRowCount for table "+table.getTableName()+" is " + super.getViewRowCount());
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
	
	@Override
	protected void renderCell(JLabel tableCellRenderer, Component result, TableCell cell) {
		// sets the text of the cell
		super.renderCell(tableCellRenderer, result, cell);
		
		if (((DatabaseTableCell)cell).isPrimaryKey()) {
			result.setFont(result.getFont().deriveFont(Font.BOLD, result.getFont().getSize()));
		}
	}
}
