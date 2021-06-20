package ui;

import domain.TableCell;
import domain.TableRow;

/**
 * Handler for a specific event on a table cell, e.g. double click
 *
 * @param <T> the table row
 * @param <U> the table cell
 */
public abstract class TableViewClickAdapter<T extends TableRow, U extends TableCell> {

	public abstract void cellSelected(T row, U cell);

}
