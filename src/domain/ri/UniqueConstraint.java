package domain.ri;

import java.util.ArrayList;
import java.util.List;

/**
 * Constraint that guarantees uniqueness in its defining table over the defined
 * columns
 */
public class UniqueConstraint extends Constraint {

	protected List<ForeignKey> referencingForeignKeys = new ArrayList<>();

	public UniqueConstraint(String name, List<ColumnDefinition> columnDefinitions) {
		super(name, columnDefinitions);
	}

	public void addReferencingForeignKey(ForeignKey foreignKey) {
		if (!referencingForeignKeys.contains(foreignKey)) {
			referencingForeignKeys.add(foreignKey);
		}
	}

	/**
	 * 
	 * @return a list of all foreign key constraints referencing this unique constraint
	 */
	public List<ForeignKey> getReferencingForeignKeys() {
		return referencingForeignKeys;
	}

}
