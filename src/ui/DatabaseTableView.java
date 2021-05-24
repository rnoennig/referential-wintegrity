package ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;

import domain.DatabaseTable;
import domain.DatabaseTableCell;
import domain.TableCell;

/**
 * TODO sort by primary key columns by default
 *
 */
public class DatabaseTableView extends TableView {
	private static final long serialVersionUID = 1L;

	public DatabaseTableView(DatabaseTable table) {
		super(table);

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
