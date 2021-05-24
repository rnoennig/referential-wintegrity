package dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import domain.DatabaseTable;
import domain.DatabaseTableCell;
import domain.DatabaseTableRow;
import domain.Table;
import domain.TableCell;
import domain.TableRow;
import domain.ri.ColumnDefinition;
import domain.ri.ForeignKey;
import domain.ri.PrimaryKey;
import domain.ri.Schema;
import domain.ri.TableDefinition;
import domain.ri.UniqueConstraint;

public class JdbcService {

	private Schema schema;

	/**
	 * 
	 * @param tableName
	 * @return all rows of the given database table
	 */
	public DatabaseTable getTableRows(String tableName) {
		DatabaseTable table = null;
		try {
			Connection conn = createConnection();
			table = selectRows(tableName, Collections.emptyList(), Collections.emptyList(), conn);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

	/**
	 * 
	 * @return all tables in the connected database
	 */
	public Table selectAllTableNames() {
		Table table = null;
		try {
			Connection conn = createConnection();
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			ResultSet res = databaseMetaData.getTables(null, null, null, new String[] { "TABLE" });
			List<String> tableNames = new ArrayList<>();
			while (res.next()) {
				String systemTableName = res.getString("TABLE_NAME");
				tableNames.add(systemTableName);
			}

			table = new Table();
			table.setTableName("All tables");
			table.setColumnNames("TABLE_NAME");
			table.setHeader(Arrays.asList(new TableCell("TABLE_NAME", true)));
			List<TableRow> data = new ArrayList<>();
			for (String tableName : tableNames) {
				TableRow row = new TableRow(table, Arrays.asList(new TableCell(tableName)));
				data.add(row);
			}
			table.setData(data);
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

	public List<DatabaseTable> getDependentRows(DatabaseTableRow selectedRow) {
		Set<DatabaseTableRow> dependantRows = new HashSet<>();
		// since the given row is likely already part of a table we need a deep copy
		DatabaseTableRow databaseTableRow = new DatabaseTableRow(selectedRow);
		dependantRows.add(databaseTableRow);
		try {
			Connection conn = createConnection();
			Set<DatabaseTableRow> visitedRowsFollowingPrimaryKeys = new HashSet<>();
			getDependentRowsRecursiveFollowingPrimaryKeys(databaseTableRow, visitedRowsFollowingPrimaryKeys, dependantRows, conn);
			Set<DatabaseTableRow> visitedRowsFollowingForeignKeys = new HashSet<>();
			getDependentRowsRecursiveFollowingForeignKeys(databaseTableRow, visitedRowsFollowingForeignKeys, dependantRows, conn);
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// group by table
		Map<DatabaseTable, List<DatabaseTableRow>> databaseTableRowsByTable = new HashMap<>();
		for (DatabaseTableRow row : dependantRows) {
			databaseTableRowsByTable.putIfAbsent(row.getTable(), new ArrayList<>());
			databaseTableRowsByTable.get(row.getTable()).add(row);
		}
		// collect to lists
		for (DatabaseTable databaseTable : databaseTableRowsByTable.keySet()) {
			List<DatabaseTableRow> rows = databaseTableRowsByTable.get(databaseTable);
			for ( DatabaseTableRow row : rows) {
//				System.out.println("Row before: " + row);
				row.setTable(databaseTable);
//				System.out.println("Row  after: " + row);
			}
			databaseTable.setData(rows);
		}
		List<DatabaseTable> tables = new ArrayList<>(databaseTableRowsByTable.keySet());
//		Collections.sort(tables, new Comparator<>() {
//
//			@Override
//			public int compare(DatabaseTable a, DatabaseTable b) {
//				TableDefinition tableDefinitionA = schema.getTableDefinitionByName(a.getTableName()).get();
//				TableDefinition tableDefinitionB = schema.getTableDefinitionByName(b.getTableName()).get();
//				
//				if (tableDefinitionA.dependsOn(tableDefinitionB)) {
//					return 1;
//				}
//				
//				if (tableDefinitionB.dependsOn(tableDefinitionA)) {
//					return -1;
//				}
//				
//				return tableDefinitionA.getTableName().compareTo(tableDefinitionB.getTableName());
//			}
//		});
		return tables;
	}

	public void getDependentRowsRecursiveFollowingPrimaryKeys(DatabaseTableRow startRow, Set<DatabaseTableRow> visitedRows, Set<DatabaseTableRow> rows,
			Connection conn) throws SQLException {
		if (visitedRows.contains(startRow)) {
			System.err.println("Already visited this row: " + startRow);
			return;
		}
		visitedRows.add(startRow);
		
		TableDefinition tableDefinition = schema.getTableDefinitionByName(startRow.getTableName()).get();
		Optional<PrimaryKey> primaryKey = tableDefinition.getPrimaryKey();
		if (primaryKey.isEmpty()) {
			System.err.println("No PK found in " + startRow + " stop following primary keys up to generate schema.");
			return;
		}
		List<ForeignKey> referencingForeignKeys = primaryKey.get().getReferencingForeignKeys();
		for (ForeignKey referencingForeignKey : referencingForeignKeys) {
			String fkTableName = referencingForeignKey.getTableName();
			
			List<String> whereColumnNames = referencingForeignKey.getColumnDefinitions().stream().map(cd -> cd.getColumnName()).collect(Collectors.toList());
			List<Object> whereColumnValues = startRow.getColumnValues(referencingForeignKey.getReferencedConstraint().getColumnDefinitions());
			
			DatabaseTable table = selectRows(fkTableName, whereColumnNames, whereColumnValues, conn);
			for (DatabaseTableRow row : table.getTableRows()) {
				rows.add(row);
			}
			for (DatabaseTableRow dependantRow : table.getTableRows()) {
				getDependentRowsRecursiveFollowingPrimaryKeys(dependantRow, visitedRows, rows, conn);
			}
		}

	}
	
	public void getDependentRowsRecursiveFollowingForeignKeys(DatabaseTableRow startRow, Set<DatabaseTableRow> visitedRows, Set<DatabaseTableRow> rows,
			Connection conn) throws SQLException {
		if (visitedRows.contains(startRow)) {
			System.err.println("Already visited this row: " + startRow);
			return;
		}
		visitedRows.add(startRow);
		
		TableDefinition tableDefinition = schema.getTableDefinitionByName(startRow.getTableName()).get();
		List<ForeignKey> foreignKeys = tableDefinition.getForeignKeys();
		for (ForeignKey foreignKey : foreignKeys) {
			UniqueConstraint referencedUniqueConstraint = foreignKey.getReferencedConstraint();
			String pkTableName = referencedUniqueConstraint.getTableName();
			
			List<String> whereColumnNames = foreignKey.getReferencedConstraint().getColumnNames();
			List<Object> whereColumnValues = startRow.getColumnValues(foreignKey.getColumnDefinitions());
			
			DatabaseTable table = selectRows(pkTableName, whereColumnNames, whereColumnValues, conn);
			for (DatabaseTableRow row : table.getTableRows()) {
				rows.add(row);
			}
			for (DatabaseTableRow dependantRow : table.getTableRows()) {
				getDependentRowsRecursiveFollowingForeignKeys(dependantRow, visitedRows, rows, conn);
			}
		}
	}
	
	public DatabaseTable selectRows(String tableName, List<String> whereColumnNames, List<Object> whereColumnValues) {
		DatabaseTable table = null;
		try {
			Connection conn = createConnection();
			table = selectRows(tableName, whereColumnNames, whereColumnValues, conn);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

	private DatabaseTable selectRows(String tableName, List<String> whereColumnNames, List<Object> whereColumnValues, Connection conn) throws SQLException {
		Statement stmtFormat = conn.createStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(stmtFormat.enquoteIdentifier(tableName, false) );
		if (whereColumnNames != null && !whereColumnNames.isEmpty()) {
			sb.append(" where ");
		}
		for (String columnName : whereColumnNames) {
			sb.append(stmtFormat.enquoteIdentifier(columnName, false));
			sb.append(" = ?");
		}
		String query = sb.toString();
		System.out.println("Executing query: " + query);
		
		TableDefinition tableDefinition = schema.getTableDefinitionByName(tableName).get();
		List<ColumnDefinition> columnDefinitions = tableDefinition.getColumnDefinitions();

		DatabaseTable table = new DatabaseTable();
		PreparedStatement stmt = conn.prepareStatement(query);
		for (int i = 0; i < whereColumnValues.size(); i++) {
			stmt.setObject(i+1, whereColumnValues.get(i));
		}
		ResultSet res = stmt.executeQuery();

		List<DatabaseTableRow> rows = new ArrayList<>();
		while (res.next()) {
			List<DatabaseTableCell> row = new ArrayList<>();

			for (ColumnDefinition columnDefinition : columnDefinitions) {
				String columnName = columnDefinition.getColumnName();
				Object value = res.getObject(columnName);
				row.add(new DatabaseTableCell(tableDefinition, columnDefinition, value));
			}

			rows.add(new DatabaseTableRow(table, row));
		}

		table.setTableDefinition(tableDefinition);
		table.setTableName(tableName);
		table.setColumnNames(columnDefinitions.stream().map(cd -> cd.getColumnName()).toArray(String[]::new));
		table.setHeader(columnDefinitions.stream().map(cd -> new DatabaseTableCell(tableDefinition, cd, cd.getColumnName(), true)).collect(Collectors.toList()));
		table.setData(rows);

		return table;
	}

	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost/?user=postgres&password=mysecretpassword&ssl=false";
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}
	
	// TODO create intermedia textual representation of the schema, e.g. list of primary key, foreign keys and columns
	public Schema readSchemaGraph() {
		System.out.println("Reading schema");
		schema = new Schema();

		try {
			Connection conn = createConnection();
			DatabaseMetaData databaseMetaData = conn.getMetaData();

			Table tables = selectAllTableNames();
			for (TableRow tableRow : tables.getTableRows()) {
				String tableName = tableRow.getColumnValue(0).toString();
				System.out.println("Reading schema: processing table " + tableName);
				ResultSet primaryKeysResultSet = databaseMetaData.getPrimaryKeys(null, null, tableName);
				Map<String, List<ColumnDefinition>> primaryKeys = new HashMap<>();
				while (primaryKeysResultSet.next()) {
					String primaryKeyName = primaryKeysResultSet.getString("PK_NAME");
					String primaryKeyColumnName = primaryKeysResultSet.getString("COLUMN_NAME");

					if (!primaryKeys.containsKey(primaryKeyName)) {
						primaryKeys.put(primaryKeyName, new ArrayList<>());
					}
					List<ColumnDefinition> primaryKeyColumnDefinitions = primaryKeys.get(primaryKeyName);
					primaryKeyColumnDefinitions.add(new ColumnDefinition(schema.addTable(tableName), primaryKeyColumnName));
				}

				for (Entry<String, List<ColumnDefinition>> primaryKeyEntry : primaryKeys.entrySet()) {
					String pkName = primaryKeyEntry.getKey();
					List<ColumnDefinition> primaryKeyColumnDefinitions = primaryKeyEntry.getValue();
					schema.addPrimaryKey(tableName, pkName, primaryKeyColumnDefinitions);
				}

				// imported keys mean imported primary keys from other tables
				ResultSet importedKeysResultSet = databaseMetaData.getImportedKeys(null, null, tableName);
				Map<String, List<ColumnDefinition>> fkFkColumns = new HashMap<>();
				Map<String, List<ColumnDefinition>> fkPkColumns = new HashMap<>();
				Map<String, String> fkPkTableNames = new HashMap<>();
				Map<String, String> fkPkNames = new HashMap<>();
				while (importedKeysResultSet.next()) {
					String pkTableName = importedKeysResultSet.getString("PKTABLE_NAME");
					String pkColumnName = importedKeysResultSet.getString("PKCOLUMN_NAME");
					String fkColumnName = importedKeysResultSet.getString("FKCOLUMN_NAME");
					String pkName = importedKeysResultSet.getString("PK_NAME");
					String fkName = importedKeysResultSet.getString("FK_NAME");

					fkPkTableNames.putIfAbsent(fkName, pkTableName);
					fkPkNames.putIfAbsent(fkName, pkName);

					if (!fkFkColumns.containsKey(fkName)) {
						fkFkColumns.put(fkName, new ArrayList<>());
					}
					List<ColumnDefinition> foreignKeyColumnDefinitions = fkFkColumns.get(fkName);
					foreignKeyColumnDefinitions.add(new ColumnDefinition(schema.addTable(tableName), fkColumnName));

					if (!fkPkColumns.containsKey(fkName)) {
						fkPkColumns.put(fkName, new ArrayList<>());
					}
					List<ColumnDefinition> foreignKeyPkColumnDefinitions = fkPkColumns.get(fkName);
					foreignKeyPkColumnDefinitions.add(new ColumnDefinition(schema.addTable(pkTableName), pkColumnName));
				}

				for (String fkName : fkPkNames.keySet()) {
					String pkTableName = fkPkTableNames.get(fkName);
					String pkName = fkPkNames.get(fkName);
					// referenced columns aren't always primary keys, but can also be unique constraints
					// need to differentiate between referenced keys and primary keys!
					// primary key is a special kind of referencable key!
					UniqueConstraint uniqueConstraint = schema.addUniqueConstraint(pkTableName, pkName, fkPkColumns.get(fkName));
					ForeignKey foreignKey = schema.addForeignKey(tableName, fkName, fkFkColumns.get(fkName),
							uniqueConstraint);
					uniqueConstraint.addReferencingForeignKey(foreignKey);
				}
				
				ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null);
				List<ColumnDefinition> columnDefinitions = new ArrayList<>();
				while (columns.next()) {
					String columnName = columns.getString("COLUMN_NAME");
					System.out.println("Reading schema:     add column " + columnName);
//					String columnSize = columns.getString("COLUMN_SIZE");
//					String datatype = columns.getString("DATA_TYPE");
//					String isNullable = columns.getString("IS_NULLABLE");
//					String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
					columnDefinitions.add(new ColumnDefinition(schema.addTable(tableName), columnName));
				}
				schema.getTableDefinitionByName(tableName).get().setColumnDefinitions(columnDefinitions);
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished reading schema");
		return schema;
	}

}
