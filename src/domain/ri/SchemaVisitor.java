package domain.ri;

import java.util.Set;

/**
 * Visitor that can be used with a {@link Schema}
 */
public abstract class SchemaVisitor {
	protected Set<TableDefinition> visited;
	
	protected boolean found = false;
	public SchemaVisitor(Set<TableDefinition> visited) {
		this.visited = visited;
	}
	public boolean isFound() {
		return found;
	}
	/**
	 * 
	 * @param tableDefinition
	 * @return if <tt>false</tt> is returned no more nodes will be visited
	 */
	public abstract boolean visit(TableDefinition tableDefinition);
}
