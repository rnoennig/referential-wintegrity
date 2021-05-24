package ui;

import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import domain.Table;
import domain.ri.TableDefinition;

/**
 * 
 *
 */
public class FormattedDatabaseTableView extends TableView {
	private static final long serialVersionUID = 1L;

	public FormattedDatabaseTableView(Table table) {
		super(table);

		TableDefinition tableDefinition = table.getTableDefinition();
		if (tableDefinition.hasPrimaryKey()) {
			enablePrimaryKeyColumnBoldRenderer(tableDefinition);
		}
	}

	private void enablePrimaryKeyColumnBoldRenderer(TableDefinition tableDefinition) {
		List<Integer> pkIndexes = tableDefinition.getPrimaryKeyColumnIndexes();

		jTable.getTableHeader().setDefaultRenderer(
				createPrimaryKeyCellRenderer(jTable.getTableHeader().getDefaultRenderer(), pkIndexes));
		jTable.setDefaultRenderer(Object.class,
				createPrimaryKeyCellRenderer(jTable.getDefaultRenderer(Object.class), pkIndexes));
	}

	/**
	 * 
	 * @param tableCellRenderer original cell renderer is used to preserve the look
	 *                          and feel
	 * @param pkIndexes
	 * @return
	 */
	private DefaultTableCellRenderer createPrimaryKeyCellRenderer(TableCellRenderer tableCellRenderer,
			List<Integer> pkIndexes) {
		DefaultTableCellRenderer primaryKeyCellRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				final Component result = tableCellRenderer.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);

				// FIXME column is the displayed columnindex, not the index in the column model,
				// so rearranging columns doesn't affect in which column the style is changed
				if (pkIndexes.contains(column)) {
					result.setFont(result.getFont().deriveFont(Font.BOLD, result.getFont().getSize()));
				}

				return result;
			}
		};
		return primaryKeyCellRenderer;
	}
}
