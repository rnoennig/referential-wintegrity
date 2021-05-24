package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import domain.Table;
import domain.TableCell;

public class TableView extends JPanel {
	private static final long serialVersionUID = 1L;
	private List<ClickAdapter> clickAdapters = new LinkedList<>();
	protected JTable jTable;

	public TableView(Table table) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(table.getTableName());
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.add(label);

		TableModel dm = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getRowCount() {
				return table.getRowCount();
			}

			@Override
			public int getColumnCount() {
				return table.getColumnCount();
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				TableCell tableCell;
				if (rowIndex < 0) {
					tableCell = table.getTableHeader().get(columnIndex);
				} else {
					tableCell = table.getTableRows().get(rowIndex).getValues().get(columnIndex);
				}
				return tableCell;
			}

			@Override
			public String getColumnName(int column) {
				return table.getColumnNames()[column];
			}
		};

		jTable = new JTable(dm);
		jTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// must be a double click
				if (e.getClickCount() < 2) {
					return;
				}
				int row = jTable.rowAtPoint(e.getPoint());
				int col = jTable.columnAtPoint(e.getPoint());
				for (ClickAdapter clickAdapter : clickAdapters) {
					clickAdapter.cellSelected(table.getTableRows().get(row), (TableCell)jTable.getValueAt(row, col));
				}
			}
		});
		jTable.getTableHeader().setDefaultRenderer(
				createStylePresevingCellRenderer(jTable.getTableHeader().getDefaultRenderer()));
		jTable.setDefaultRenderer(Object.class,
				createStylePresevingCellRenderer(jTable.getDefaultRenderer(Object.class)));
		
		// if the table doesn't specify a preferred scrollable viewport size the default
		// scrollpane size is larger than the table
		int numOfVisibleRows = table.getRowCount();
		int cols = jTable.getColumnModel().getTotalColumnWidth();
		int rows = jTable.getRowHeight() * numOfVisibleRows;
		Dimension d = new Dimension(cols, rows);
		jTable.setPreferredScrollableViewportSize(d);

		JScrollPane scrollPane = new JScrollPane(jTable) {
			private static final long serialVersionUID = 1L;

			/**
			 * scrollpane should be small if the content is small otherwise have a max
			 * height
			 */
			@Override
			public Dimension getMaximumSize() {
				int maxHeight = 200;
				// 10000 means grab as much width as possible
				return new Dimension(10000, Math.min(getPreferredSize().height, maxHeight));
			}
		};
		this.add(scrollPane);
	}

	public void addClicklistener(ClickAdapter clickAdapter) {
		this.clickAdapters.add(clickAdapter);
	}
	
	protected DefaultTableCellRenderer createStylePresevingCellRenderer(TableCellRenderer tableCellRenderer) {
		DefaultTableCellRenderer primaryKeyCellRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				final Component result = tableCellRenderer.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, row, column);
				
				Object cell = table.getValueAt(row, column);
				if (cell instanceof TableCell) {
					renderCell((JLabel) tableCellRenderer, result, (TableCell)cell);
				}
				
				return result;
			}
		};
		return primaryKeyCellRenderer;
	}

	protected void renderCell(JLabel tableCellRenderer, Component result, TableCell cell) {
		String cellText = cell.getValue().toString();
		tableCellRenderer.setText(cellText);
	}
}
