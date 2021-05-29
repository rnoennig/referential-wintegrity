package ui;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Iterator;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import domain.Table;
import domain.TableCell;

public class TableView extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private List<ClickAdapter> clickAdapters = new LinkedList<>();
	
	protected JTable jTable;
	
	protected Table table;
	
	private boolean autoWidth = false;

	private boolean autoHeight;

	public TableView(Table table, boolean autoWidth) {
		super();
		this.table = table;
		this.autoWidth = autoWidth;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel label = new JLabel(table.getTableName());
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
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
				return table.getColumnNames().get(column);
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
				int rowModelIndex = jTable.convertRowIndexToModel(row);
				int col = jTable.columnAtPoint(e.getPoint());
				for (ClickAdapter clickAdapter : clickAdapters) {
					clickAdapter.cellSelected(table.getTableRows().get(rowModelIndex), (TableCell)jTable.getValueAt(row, col));
				}
			}
		});
		jTable.getTableHeader().setDefaultRenderer(
				createStylePresevingCellRenderer(jTable.getTableHeader().getDefaultRenderer()));
		jTable.setDefaultRenderer(Object.class,
				createStylePresevingCellRenderer(jTable.getDefaultRenderer(Object.class)));
		
		if (!autoWidth) {
			resizeColumnsToFitContent(true);
		}
		
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
				int height = getPreferredSize().height;
				if (height > maxHeight) {
					height = maxHeight;
				}
				if (TableView.this.autoHeight) 
				{
					height = getPreferredSize().height;
				}
				// MAX_VALUE means grab as much width as possible
				int width = Integer.MAX_VALUE;
				if (!TableView.this.autoWidth) {
					width = getPreferredSize().width;
				}
				return new Dimension(width, height);
			}
		};
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.add(scrollPane);
	}

	private void resizeColumnsToFitContent(boolean includeHeader) {
		TableColumnModel columnModel = jTable.getColumnModel();
		Iterator<TableColumn> it = columnModel.getColumns().asIterator();
		while(it.hasNext() && !autoWidth) {
			TableColumn col = it.next();
			String columnName = col.getHeaderValue().toString();
			Canvas c = new Canvas();
			FontMetrics metrics = c.getFontMetrics(getFont());
			int colCharLength = this.table.getMaxColumnSize(x -> metrics.stringWidth(x), columnName);
			int preferredWidth = Math.max(colCharLength, metrics.stringWidth(columnName));
			col.setPreferredWidth(preferredWidth);
			col.setMinWidth(preferredWidth);
		}
	}

	public boolean isAutoWidth() {
		return autoWidth;
	}

	public void setAutoWidth(boolean autoWidth) {
		this.autoWidth = autoWidth;
	}

	public void addClicklistener(ClickAdapter clickAdapter) {
		this.clickAdapters.add(clickAdapter);
	}
	
	protected DefaultTableCellRenderer createStylePresevingCellRenderer(TableCellRenderer tableCellRenderer) {
		DefaultTableCellRenderer primaryKeyCellRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable tableForRendering, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				final Component result = tableCellRenderer.getTableCellRendererComponent(tableForRendering, value, isSelected,
						hasFocus, row, column);
				
				Object cell;
				if (row < 0) {
					cell = TableView.this.table.getTableHeader().get(tableForRendering.convertColumnIndexToModel(column));
				} else {
					cell = tableForRendering.getValueAt(row, column);
				}
				if (cell instanceof TableCell) {
					renderCell((JLabel) tableCellRenderer, result, (TableCell)cell);
				}
				
				return result;
			}
		};
		return primaryKeyCellRenderer;
	}

	protected void renderCell(JLabel tableCellRenderer, Component result, TableCell cell) {
		Object value = cell.getValue();
		String cellText = value == null ? "<NULL>" : renderCellType(value);
		tableCellRenderer.setText(cellText);
	}

	private String renderCellType(Object cellValue) {
		try {
			if (cellValue instanceof oracle.sql.TIMESTAMPTZ) {
				return ((oracle.sql.TIMESTAMPTZ) cellValue).localDateTimeValue().toString();
			}
		} catch (SQLException e) {
			// ignore
		}
		
		return cellValue.toString();
	}

	public Table getTable() {
		return this.table;
	}

	public void setAutoHeight(boolean autoHeight) {
		this.autoHeight = autoHeight;
	}
}
