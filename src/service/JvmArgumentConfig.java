package service;

public class JvmArgumentConfig {

	public String getDatabaseSchemaName() {
		return System.getProperty("databaseSchemaName");
	}

	public String getDatabaseSchemaFilePath() {
		return System.getProperty("databaseSchemaFilePath");
	}

	public String getJdbcDriverClassName() {
		return System.getProperty("jdbcDriverClassName");
	}

	public String getJdbcConnectionUrl() {
		return System.getProperty("jdbcConnectionUrl");
	}

	public String getJdbcConnectionUser() {
		return System.getProperty("jdbcConnectionUser");
	}

	public String getJdbcConnectionPassword() {
		return System.getProperty("jdbcConnectionPassword");
	}

}
