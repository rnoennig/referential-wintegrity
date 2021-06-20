package dao;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Converts database vendor specific types to SQL expressions
 */
public class DatabaseTypeFormatter {

	public Object format(Object columnValue, Statement stmtFormat) throws SQLException {
		if (columnValue instanceof String) {
			return stmtFormat.enquoteLiteral((String) columnValue);
		}
		if (isTime(columnValue)) {
			return stmtFormat.enquoteLiteral(columnValue.toString());
		}
		return columnValue.toString();
	}

	private boolean isTime(Object columnValue) {
		return columnValue instanceof java.sql.Timestamp || columnValue instanceof java.sql.Date;
	}

}
