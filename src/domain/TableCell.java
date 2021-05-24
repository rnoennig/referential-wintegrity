package domain;

public class TableCell {

	private Object value;
	private boolean header = false;

	public TableCell(Object value) {
		this.value = value;
	}

	public TableCell(Object value, boolean header) {
		this(value);
		this.header = header;
	}

	public Object getValue() {
		return value;
	}

	public boolean isHeader() {
		return header;
	}
}
