package domain.ri;

import java.util.List;

public class PrimaryKey extends UniqueConstraint {

	public PrimaryKey(String name, List<ColumnDefinition> columnDefinitions) {
		super(name, columnDefinitions);
	}


}
