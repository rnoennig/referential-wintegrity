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
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import domain.Table;

public class TableView extends JScrollPane {
	private static final long serialVersionUID = 1L;
	private List<ClickAdapter> clickAdapters = new LinkedList<>();
	JScrollPane jScrollPane;

	public TableView(Table table) {
		super();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		setViewportView(panel);

		JLabel label = new JLabel(table.getTableName());
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(label);

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
				return table.getTableRows().get(rowIndex).getValues()[columnIndex];
			}

			@Override
			public String getColumnName(int column) {
				return table.getColumnNames()[column];
			}
		};
		
		JTable jTable = new JTable(dm);
		jTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() < 2) {
					return;
				}
				int row = jTable.rowAtPoint(e.getPoint());
				int col = jTable.columnAtPoint(e.getPoint());
				for (ClickAdapter clickAdapter : clickAdapters) {
					clickAdapter.cellSelected(table.getTableRows().get(row), jTable.getValueAt(row, col).toString());
				}
			}
		});
		jTable.getTableHeader().setVisible(true);
		panel.add(jTable);
		updateUI();
	}
	
	/**
	 * scrollpane should be small if the content is small otherwise have a max height
	 */
	@Override
	public Dimension getMaximumSize() {
		int maxHeight = 200;
		// 10000 means grab as much width as possible
		return new Dimension(10000, Math.min(getPreferredSize().height, maxHeight));
	}

	public void addClicklistener(ClickAdapter clickAdapter) {
		this.clickAdapters.add(clickAdapter);
	}
}
