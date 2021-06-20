package domain.ri;

import java.util.List;

/**
 * Marker class for a primary key constraint as specialized variant of a unique
 * constraint
 */
public class PrimaryKey extends UniqueConstraint {

	public PrimaryKey(String name, List<ColumnDefinition> columnDefinitions) {
		super(name, columnDefinitions);
	}

}
