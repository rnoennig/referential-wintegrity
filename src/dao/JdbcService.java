package dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import domain.Table;
import domain.TableRow;
import domain.ri.ColumnDefinition;
import domain.ri.ForeignKey;
import domain.ri.PrimaryKey;
import domain.ri.Schema;
import domain.ri.TableDefinition;

public class JdbcService {

	private Schema schema;

	/**
	 * 
	 * @param tableName
	 * @return all rows of the given database table
	 */
	public Table getTableRows(String tableName) {
		Table table = null;
		try {
			Connection conn = createConnection();
			table = selectRows(tableName, "select * from " + tableName, conn);
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
	public Table getTables() {
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
			List<TableRow> data = new ArrayList<>();
			for (String tableName : tableNames) {
				TableRow row = new TableRow(table, Arrays.asList(tableName));
				data.add(row);
			}
			table.setData(data);
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

	public List<Table> getDependentRows(TableRow row) {
		Set<TableRow> rows = new HashSet<>();
		rows.add(row);
		try {
			Connection conn = createConnection();
			Set<TableRow> visitedRowsFollowingPrimaryKeys = new HashSet<>();
			getDependentRowsRecursiveFollowingPrimaryKeys(row, visitedRowsFollowingPrimaryKeys, rows, conn);
			Set<TableRow> visitedRowsFollowingForeignKeys = new HashSet<>();
			getDependentRowsRecursiveFollowingForeignKeys(row, visitedRowsFollowingForeignKeys, rows, conn);
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Table> tables = rows.stream().collect(Collectors.groupingBy(TableRow::getTable)).entrySet().stream()
				.map(e -> e.getKey().setData(e.getValue())).collect(Collectors.toList());
		Collections.sort(tables, new Comparator<>() {

			@Override
			public int compare(Table a, Table b) {
				TableDefinition tableDefinitionA = schema.getTableDefinitionByName(a.getTableName()).get();
				TableDefinition tableDefinitionB = schema.getTableDefinitionByName(b.getTableName()).get();
				
				if (tableDefinitionA.dependsOn(tableDefinitionB)) {
					return 1;
				}
				
				if (tableDefinitionB.dependsOn(tableDefinitionA)) {
					return -1;
				}
				
				return tableDefinitionA.getTableName().compareTo(tableDefinitionB.getTableName());
			}
		});
		return tables;
	}

	public void getDependentRowsRecursiveFollowingPrimaryKeys(TableRow startRow, Set<TableRow> visitedRows, Set<TableRow> rows,
			Connection conn) throws SQLException {
		if (visitedRows.contains(startRow)) {
			return;
		}
		visitedRows.add(startRow);
		
		TableDefinition tableDefinition = schema.getTableDefinitionByName(startRow.getTableName()).get();
		Optional<PrimaryKey> primaryKey = tableDefinition.getPrimaryKey();
		if (primaryKey.isEmpty()) {
			return;
		}
		List<ForeignKey> referencingForeignKeys = primaryKey.get().getReferencingForeignKeys();
		// TODO in theory there could be a table A that references a table B with more
		// than one foreign key, this needs to be handled here if that actually happens
		for (ForeignKey referencingForeignKey : referencingForeignKeys) {
			String fkTableName = referencingForeignKey.getTableName();
			String where = referencingForeignKey.buildJoinByForeignKeyExpression(startRow);
			
			Table table = selectRows(fkTableName , "select * from " + fkTableName + " where " + where, conn);
			for (TableRow row : table.getTableRows()) {
				rows.add(row);
			}
			for (TableRow dependantRow : table.getTableRows()) {
				getDependentRowsRecursiveFollowingPrimaryKeys(dependantRow, visitedRows, rows, conn);
			}
		}

	}
	
	public void getDependentRowsRecursiveFollowingForeignKeys(TableRow startRow, Set<TableRow> visitedRows, Set<TableRow> rows,
			Connection conn) throws SQLException {
		if (visitedRows.contains(startRow)) {
			return;
		}
		visitedRows.add(startRow);
		
		TableDefinition tableDefinition = schema.getTableDefinitionByName(startRow.getTableName()).get();
		List<ForeignKey> foreignKeys = tableDefinition.getForeignKeys();
		for (ForeignKey foreignKey : foreignKeys) {
			PrimaryKey referencedPrimaryKey = foreignKey.getReferencedPrimaryKey();
			String pkTableName = referencedPrimaryKey.getTableName();
			String where = foreignKey.buildJoinByPrimaryKeyExpression(startRow);
			Table table = selectRows(pkTableName, "select * from " + pkTableName + " where " + where, conn);
			for (TableRow row : table.getTableRows()) {
				rows.add(row);
			}
			for (TableRow dependantRow : table.getTableRows()) {
				getDependentRowsRecursiveFollowingForeignKeys(dependantRow, visitedRows, rows, conn);
			}
		}
	}
	
	public Table selectRows(String tableName, String query) {
		Table table = null;
		try {
			Connection conn = createConnection();
			table = selectRows(tableName, query, conn);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return table;
	}

	private Table selectRows(String tableName, String query, Connection conn) throws SQLException {
		System.out.println("Executing query: " + query);
		
		TableDefinition tableDefinition = schema.getTableDefinitionByName(tableName).get();
		List<ColumnDefinition> columnDefinitions = tableDefinition.getColumnDefinitions();

		Table table = new Table();
		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery(query);

		List<TableRow> rows = new ArrayList<>();
		while (res.next()) {
			List<String> row = new ArrayList<>();

			for (ColumnDefinition columnDefinition : columnDefinitions) {
				String columnName = columnDefinition.getColumnName();
				String value = res.getObject(columnName).toString();
				row.add(value);
			}

			rows.add(new TableRow(table, row));
		}

		table.setTableDefinition(tableDefinition);
		table.setTableName(tableName);
		table.setColumnNames(columnDefinitions.stream().map(cd -> cd.getColumnName()).toArray(String[]::new));
		table.setData(rows);

		return table;
	}

	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost/?user=postgres&password=mysecretpassword&ssl=false";
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}

	public Schema readSchemaGraph() {
		System.out.println("Reading schema");
		schema = new Schema();

		try {
			Connection conn = createConnection();
			DatabaseMetaData databaseMetaData = conn.getMetaData();

			Table tables = getTables();
			for (TableRow tableRow : tables.getTableRows()) {
				String tableName = tableRow.getColumnValue(0);
				//System.out.println("Getting PK and FK from " + tableName);
				
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
//					String fkTableName = importedKeysResultSet.getString("FKTABLE_NAME");
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
					PrimaryKey primaryKey = schema.addPrimaryKey(pkTableName, pkName, fkPkColumns.get(fkName));
					ForeignKey foreignKey = schema.addForeignKey(tableName, fkName, fkFkColumns.get(fkName),
							schema.getPrimaryKeyByName(pkName));
					primaryKey.addReferencingForeignKey(foreignKey);
				}
				
				ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null);
				List<ColumnDefinition> columnDefinitions = new ArrayList<>();
				while (columns.next()) {
					String columnName = columns.getString("COLUMN_NAME");
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
