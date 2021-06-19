package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import domain.ri.Schema;
import domain.ri.TableDefinition;
import domain.ri.UniqueConstraint;
import service.IniFile;
import service.IniFileSection;
import service.JvmArgumentConfig;

public class JdbcService {

	private Schema schema;
	private String schemaName = null;
	private String databaseSchemaFilePath;
	private String jdbcDriverClassName;
	private String jdbcConnectionUrl;
	private String jdbcConnectionUser;
	private String jdbcConnectionPassword;
	
	public JdbcService(JvmArgumentConfig config) {
		this.schemaName = config.getDatabaseSchemaName();
		this.databaseSchemaFilePath = config.getDatabaseSchemaFilePath();
		this.jdbcDriverClassName = config.getJdbcDriverClassName();
		this.jdbcConnectionUrl = config.getJdbcConnectionUrl();
		this.jdbcConnectionUser = config.getJdbcConnectionUser();
		this.jdbcConnectionPassword = config.getJdbcConnectionPassword();
	}

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
			conn.close();
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
	public Table<TableRow> selectAllTableNames() {
		Table<TableRow> table = null;
		try {
			Connection conn = createConnection();
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			ResultSet res = databaseMetaData.getTables(null, schemaName, null, new String[] { "TABLE" });
			List<String> tableNames = new ArrayList<>();
			while (res.next()) {
				String systemTableName = res.getString("TABLE_NAME");
				tableNames.add(systemTableName);
			}
			res.close();
			conn.close();

			table = new Table<>();
			table.setTableName("All tables");
			table.setColumnNames(Arrays.asList("TABLE_NAME"));
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
			conn.close();
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
		Collections.sort(tables, new Comparator<>() {

			@Override
			public int compare(DatabaseTable a, DatabaseTable b) {
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

	public void getDependentRowsRecursiveFollowingPrimaryKeys(DatabaseTableRow startRow, Set<DatabaseTableRow> visitedRows, Set<DatabaseTableRow> rows,
			Connection conn) throws SQLException {
		if (visitedRows.contains(startRow)) {
			System.err.println("Already visited this row: " + startRow);
			return;
		}
		visitedRows.add(startRow);
		
		TableDefinition tableDefinition = schema.getTableDefinitionByName(startRow.getTableName()).get();
		if (tableDefinition.getUniqueConstraints().isEmpty()) {
			System.err.println("No unique constraints found in " + startRow + "! Will stop following unique keys upwards to generate schema graph.");
			return;
		}
		for (UniqueConstraint uniqueConstraint : tableDefinition.getUniqueConstraints()) {
			List<ForeignKey> referencingForeignKeys = uniqueConstraint.getReferencingForeignKeys();
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
			conn.close();
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
		for (int i = 0; i < whereColumnNames.size(); i++) {
			String columnName = whereColumnNames.get(i);
			if (i > 0) {
				sb.append(" AND ");
			}
			sb.append(stmtFormat.enquoteIdentifier(columnName, false));
			sb.append(" = ?");
		}
		stmtFormat.close();
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
		
		res.close();
		stmt.close();

		table.setTableDefinition(tableDefinition);
		table.setTableName(tableName);
		table.setColumnNames(columnDefinitions.stream()
				.map(cd -> cd.getColumnName())
				.collect(Collectors.toList()));
		table.setHeader(columnDefinitions.stream().map(cd -> new DatabaseTableCell(tableDefinition, cd, cd.getColumnName(), true)).collect(Collectors.toList()));
		table.setData(rows);

		return table;
	}

	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Class.forName(jdbcDriverClassName);
		System.out.println("Connection to database " + schemaName + " at url " + jdbcConnectionUrl + " with user " + jdbcConnectionUser);
		Connection conn = DriverManager.getConnection(jdbcConnectionUrl, jdbcConnectionUser, jdbcConnectionPassword);
		return conn;
	}
	
	public Schema readSchemaGraph() {
		if (!new File(databaseSchemaFilePath).exists()) {
			IniFile iniFile = readSchemaGraphFromDb();
			writeIniFile(iniFile);
		}
		// TODO provide schema file name as program argument
		IniFile iniFileReadFromFS = readIniFile(databaseSchemaFilePath);
		Schema schema = readSchemaFromFile(iniFileReadFromFS);
		return schema;
	}
	private IniFile readIniFile(String fileName) {
		IniFile iniFile = new IniFile();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			iniFile.read(br);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iniFile;
	}

	private Schema readSchemaFromFile(IniFile iniFile) {
		System.out.println("Reading schema from file");
		schema = new Schema();
		List<String> tableNames = iniFile.getSection(IniFileSection.INI_FILE_SECTION_COLUMNS).get().getColumn("TABLE_NAME").stream().distinct().collect(Collectors.toList());
		System.out.println("Fetching metadata");
		System.out.println("Selecting all tables");

		for (String tableName : tableNames) {
			System.out.println("Reading schema: processing table " + tableName);
			IniFileSection primaryKeysSection = iniFile.getSection(IniFileSection.INI_FILE_SECTION_PRIMARY_KEYS).get();
			Map<String, List<ColumnDefinition>> primaryKeys = new HashMap<>();
			
			for (List<String> record : primaryKeysSection.getRecordsWithCondition(r -> r.get(primaryKeysSection.getIndex("TABLE_NAME")).equals(tableName))) {
				String primaryKeyName = record.get(primaryKeysSection.getIndex("PK_NAME"));
				String primaryKeyColumnName = record.get(primaryKeysSection.getIndex("COLUMN_NAME"));

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
			IniFileSection foreignKeysSection = iniFile.getSection(IniFileSection.INI_FILE_SECTION_FOREIGN_KEYS).get();
			Map<String, List<ColumnDefinition>> fkFkColumns = new HashMap<>();
			Map<String, List<ColumnDefinition>> fkPkColumns = new HashMap<>();
			Map<String, String> fkPkTableNames = new HashMap<>();
			Map<String, String> fkPkNames = new HashMap<>();
			for (List<String> record : foreignKeysSection.getRecordsWithCondition(r -> r.get(foreignKeysSection.getIndex("FKTABLE_NAME")).equals(tableName))) {
				String pkTableName = record.get(foreignKeysSection.getIndex("PKTABLE_NAME"));
				String pkColumnName = record.get(foreignKeysSection.getIndex("PKCOLUMN_NAME"));
				String fkColumnName = record.get(foreignKeysSection.getIndex("FKCOLUMN_NAME"));
				String pkName = record.get(foreignKeysSection.getIndex("PK_NAME"));
				String fkName = record.get(foreignKeysSection.getIndex("FK_NAME"));

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
			
			IniFileSection columnsSection = iniFile.getSection(IniFileSection.INI_FILE_SECTION_COLUMNS).get();
			List<ColumnDefinition> columnDefinitions = new ArrayList<>();
			for (List<String> record : columnsSection.getRecordsWithCondition(r -> r.get(columnsSection.getIndex("TABLE_NAME")).equals(tableName))) {
				String columnName = record.get(columnsSection.getIndex("COLUMN_NAME"));
				System.out.println("Reading schema:     add column " + columnName);
				columnDefinitions.add(new ColumnDefinition(schema.addTable(tableName), columnName));
			}
			schema.getTableDefinitionByName(tableName).get().setColumnDefinitions(columnDefinitions);
		}
		System.out.println("Finished reading schema from file");
		return schema;
	}

	private void writeIniFile(IniFile iniFile) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.schemaName + ".schema"))) {
			iniFile.write(bw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *  create intermedia textual representation of the schema, e.g. list of primary key, foreign keys and columns
	 */
	public IniFile readSchemaGraphFromDb() {
		System.out.println("Reading database metadata");
		IniFile iniFile = new IniFile();
		IniFileSection primaryKeysSection = new IniFileSection(IniFileSection.INI_FILE_SECTION_PRIMARY_KEYS);
		primaryKeysSection.setHeader("TABLE_NAME", "PK_NAME", "COLUMN_NAME");
		iniFile.addSection(primaryKeysSection);
		
		IniFileSection foreignKeysSection = new IniFileSection(IniFileSection.INI_FILE_SECTION_FOREIGN_KEYS);
		foreignKeysSection.setHeader("PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_NAME", "FKCOLUMN_NAME", "PK_NAME", "FK_NAME");
		iniFile.addSection(foreignKeysSection);
		
		IniFileSection columnsSection = new IniFileSection(IniFileSection.INI_FILE_SECTION_COLUMNS);
		columnsSection.setHeader("TABLE_NAME", "COLUMN_NAME");
		iniFile.addSection(columnsSection);

		try {
			Connection conn = createConnection();
			System.out.println("Fetching metadata");
			DatabaseMetaData databaseMetaData = conn.getMetaData();
			System.out.println("Selecting all tables");

			Table<TableRow> tables = selectAllTableNames();
			for (TableRow tableRow : tables.getTableRows()) {
				String tableName = tableRow.getColumnValue(0).toString();
				System.out.println("Reading schema: processing table " + tableName);
				ResultSet primaryKeysResultSet = databaseMetaData.getPrimaryKeys(null, schemaName, tableName);
				while (primaryKeysResultSet.next()) {
					String primaryKeyName = primaryKeysResultSet.getString("PK_NAME");
					String primaryKeyColumnName = primaryKeysResultSet.getString("COLUMN_NAME");
					primaryKeysSection.addRecord(tableName, primaryKeyName, primaryKeyColumnName);
				}
				primaryKeysResultSet.close();

				// imported keys mean imported primary keys from other tables
				ResultSet importedKeysResultSet = databaseMetaData.getImportedKeys(null, schemaName, tableName);
				while (importedKeysResultSet.next()) {
					String pkTableName = importedKeysResultSet.getString("PKTABLE_NAME");
					String pkColumnName = importedKeysResultSet.getString("PKCOLUMN_NAME");
					String fkColumnName = importedKeysResultSet.getString("FKCOLUMN_NAME");
					String pkName = importedKeysResultSet.getString("PK_NAME");
					String fkName = importedKeysResultSet.getString("FK_NAME");
					foreignKeysSection.addRecord(pkTableName, pkColumnName, tableName, fkColumnName, pkName, fkName);
				}
				importedKeysResultSet.close();
				
				ResultSet columns = databaseMetaData.getColumns(null, schemaName, tableName, null);
				while (columns.next()) {
					String columnName = columns.getString("COLUMN_NAME");
					columnsSection.addRecord(tableName, columnName);
				}
				columns.close();
			}
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished reading database metadata");
		return iniFile;
	}

	public List<String> toInsertStatements(DatabaseTable table) {
		List<String> result = new ArrayList<>();
		try {
			Connection conn = createConnection();
			Statement stmtFormat = conn.createStatement();
			DatabaseTypeFormatter databaseTypeFormatter = new DatabaseTypeFormatter();
			for (DatabaseTableRow row: table.getTableRows()) {
				String stmt = toInsertStatement(row, stmtFormat, databaseTypeFormatter, conn);
				result.add(stmt);
			}
			stmtFormat.close();
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private String toInsertStatement(DatabaseTableRow row, Statement stmtFormat, DatabaseTypeFormatter databaseTypeFormatter, Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(stmtFormat.enquoteIdentifier(row.getTableName(), false));
		sb.append(" (");
		List<ColumnDefinition> columns = row.getTable().getTableDefinition().getColumnDefinitions();
		for (int i = 0; i < columns.size(); i++) {
			ColumnDefinition col = columns.get(i);
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(stmtFormat.enquoteIdentifier(col.getColumnName(), false));
		}
		sb.append(") VALUES (");
		for (int i = 0; i < columns.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(databaseTypeFormatter.format(row.getColumnValue(i), stmtFormat));
		}
		sb.append(");");
		return sb.toString();
	}
	
	

	public List<String> toDeleteStatements(DatabaseTable table) {
		List<String> result = new ArrayList<>();
		try {
			Connection conn = createConnection();
			Statement stmtFormat = conn.createStatement();
			DatabaseTypeFormatter databaseTypeFormatter = new DatabaseTypeFormatter();
			for (DatabaseTableRow row: table.getTableRows()) {
				String stmt = toDeleteStatement(row, stmtFormat, databaseTypeFormatter, conn);
				result.add(stmt);
			}
			stmtFormat.close();
			conn.close();
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private String toDeleteStatement(DatabaseTableRow row, Statement stmtFormat,
			DatabaseTypeFormatter databaseTypeFormatter, Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(stmtFormat.enquoteIdentifier(row.getTableName(), false));
		sb.append(" WHERE ");
		List<ColumnDefinition> columns;
		if (row.getTable().getTableDefinition().getPrimaryUniqueConstraint().isPresent()) {
			columns = row.getTable().getTableDefinition().getPrimaryUniqueConstraint().get().getColumnDefinitions();
		} else {
			columns = row.getTable().getTableDefinition().getColumnDefinitions();
		}
		for (int i = 0; i < columns.size(); i++) {
			ColumnDefinition col = columns.get(i);
			if (i > 0) {
				sb.append(" AND ");
			}
			sb.append(stmtFormat.enquoteIdentifier(col.getColumnName(), false));
			sb.append(" = ");
			sb.append(databaseTypeFormatter.format(row.getColumnValue(i), stmtFormat));
		}
		sb.append(";");
		return sb.toString();
	}

}
