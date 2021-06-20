package domain;

/**
 * A single value of a record 
 */
public class TableCell {

	protected Object value;
	protected boolean header = false;

	public TableCell(Object value) {
		this.value = value;
	}

	// TODO move header to tablerow
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (header ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableCell other = (TableCell) obj;
		if (header != other.header)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
