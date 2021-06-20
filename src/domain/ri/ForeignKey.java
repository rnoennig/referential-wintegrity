package domain.ri;

import java.util.List;

/**
 * Foreign key constraint that references a unique key constraint of another database table
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
