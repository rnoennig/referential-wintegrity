package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import domain.Table;
import domain.TableCell;
import domain.TableRow;

/**
 * Panel with a JTable that can display tabular data
 *
 * @param <T> table row
 * @param <U> table cell
 */
public class TableView<T extends TableRow, U extends TableCell> extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private List<TableViewClickAdapter<T, U>> clickAdapters = new LinkedList<>();
	
	protected JTable jTable;
	
	protected TableRowSorter<TableModel> sorter;
	
	protected Table<T, U> table;
	
	private boolean autoWidth = false;

	private boolean autoHeight;

	private JScrollPane scrollPane;

	private boolean tableHeaderVisible = true;

	private boolean tableNameVisible = true;

	private JLabel tableNameLabel;

	public TableView(Table<T, U> table, boolean autoWidth, boolean tableNameVisible, boolean tableHeaderVisible) {
		super();
		this.table = table;
		this.autoWidth = autoWidth;
		this.tableHeaderVisible = tableHeaderVisible;
		this.tableNameVisible = tableNameVisible;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.tableNameLabel = new JLabel(table.getTableName());
		this.tableNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		if (this.tableNameVisible) {
			this.add(this.tableNameLabel);
		}

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

			@SuppressWarnings("unchecked")
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				U tableCell;

				if (rowIndex < 0) {
					tableCell = (U) table.getTableHeader().get(columnIndex);
				} else {
					tableCell = (U) table.getTableRows().get(rowIndex).getValues().get(columnIndex);
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
				int rowViewIndex = jTable.rowAtPoint(e.getPoint());
				int rowModelIndex = jTable.convertRowIndexToModel(rowViewIndex);
				int colViewIndex = jTable.columnAtPoint(e.getPoint());
				for (TableViewClickAdapter<T, U> clickAdapter : clickAdapters) {
					T rowObj = table.getTableRows().get(rowModelIndex);
					@SuppressWarnings("unchecked")
					U cellObj = (U) jTable.getValueAt(rowViewIndex, colViewIndex);
					clickAdapter.cellSelected(rowObj, cellObj);
				}
			}
		});
		
		jTable.getTableHeader().setDefaultRenderer(
				createStylePresevingCellRenderer(jTable.getTableHeader().getDefaultRenderer()));
		jTable.setDefaultRenderer(Object.class,
				createStylePresevingCellRenderer(jTable.getDefaultRenderer(Object.class)));
		
		if (!this.autoWidth) {
			resizeColumnsToFitContent(true);
		}
		
		if (!this.tableHeaderVisible) {
			jTable.setTableHeader(null);
		}
		
		// if the table doesn't specify a preferred scrollable viewport size the default
		// scrollpane size is larger than the table
		int numOfVisibleRows = table.getRowCount();
		int cols = jTable.getColumnModel().getTotalColumnWidth();
		int rows = jTable.getRowHeight() * numOfVisibleRows;
		Dimension d = new Dimension(cols, rows);
		jTable.setPreferredScrollableViewportSize(d);
		
		sorter = new TableRowSorter<TableModel>(dm);
		this.jTable.setRowSorter(sorter);
		sorter.addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				TableView.this.resizeColumnsToFitContent(true);
			}
		});
		
		scrollPane = new JScrollPane(jTable) {
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
		JLabel comp = new JLabel();
		comp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		this.add(comp);
	}

	public void resizeColumnsToFitContent(boolean includeHeader) {
		TableColumnModel columnModel = jTable.getColumnModel();
		Iterator<TableColumn> it = columnModel.getColumns().asIterator();
		while (it.hasNext() && !autoWidth) {
			TableColumn col = it.next();
			String columnName = col.getHeaderValue().toString();
			int preferredWidth = this.getMaxColumnSize(columnName);
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

	public void addClicklistener(TableViewClickAdapter<T, U> clickAdapter) {
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

	protected String renderCellType(Object cellValue) {
		return cellValue.toString();
	}

	public Table<T, U> getTable() {
		return this.table;
	}

	public void setAutoHeight(boolean autoHeight) {
		this.autoHeight = autoHeight;
	}
	
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	@Override
	public Dimension preferredSize() {
		Dimension tableMaxSize = this.scrollPane.getMaximumSize();
		int height = tableMaxSize.height;
		int width = tableMaxSize.width;
				
		if (this.tableNameVisible) {
			height += TableView.this.tableNameLabel.getPreferredSize().height;
		}
		
		height += 10;
		return new Dimension(width, height);
	}

	public int getMaxColumnSize(String columnName) {
		FontMetrics metrics = jTable.getFontMetrics(jTable.getFont());
		int additionalPadding = 4;
		int additionalPaddingWhenSorted = isColumnSorted(columnName) ? 10 : 0;
		return table.getMaxColumnSize(x -> SwingUtilities.computeStringWidth(metrics, x) + additionalPadding + additionalPaddingWhenSorted, columnName);
	}

	private boolean isColumnSorted(String columnName) {
		RowSorter<? extends TableModel> rowSorter = jTable.getRowSorter();
		if (rowSorter == null) {
			return false;
		}
		List<? extends SortKey> sortKeys = rowSorter.getSortKeys();
		return sortKeys.stream().anyMatch(x -> this.table.getColumnNames().get(x.getColumn()).equalsIgnoreCase(columnName));
	}
}
