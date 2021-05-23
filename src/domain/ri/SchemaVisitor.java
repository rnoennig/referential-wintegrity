package domain.ri;

public abstract class SchemaVisitor {
	protected boolean found = false;
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
