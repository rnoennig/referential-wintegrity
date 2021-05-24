package domain.ri;

import java.util.List;
/**
 * 
 *
 */
public class ForeignKey extends Constraint {
	private UniqueConstraint referencedUniqueConstraint;

	public ForeignKey(String name, List<ColumnDefinition> columnDefinition, UniqueConstraint referencedUniqueConstraint) {
		super(name, columnDefinition);
		this.referencedUniqueConstraint = referencedUniqueConstraint;
	}

	@Override
	public String toString() {
		return getTableName() + "." + columnDefinitions + "->" + referencedUniqueConstraint;
	}

	public UniqueConstraint getReferencedConstraint() {
		return referencedUniqueConstraint;
	}
	
}
