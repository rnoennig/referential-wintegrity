package dao;

import java.util.List;

import domain.TableRow;
import domain.ri.ForeignKey;
import domain.ri.Schema;
import domain.ri.TableDefinition;

public class JdbcQueryUtil {

	public static List<String> generateQueryOfRowsReferencing(Schema schema, TableRow startRow) {
		TableDefinition tableDefinition = schema.getTableDefinitionByName(startRow.getTableName()).get();
		List<ForeignKey> referencingForeignKeys = tableDefinition.getPrimaryKey().getReferencingForeignKeys();
		
		for (ForeignKey foreignKey : referencingForeignKeys) {
			
		}
		return null;
	}

}
