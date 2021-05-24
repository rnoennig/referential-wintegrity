package domain.ri;

import java.util.ArrayList;
import java.util.List;

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

	public List<ForeignKey> getReferencingForeignKeys() {
		return referencingForeignKeys;
	}

}
