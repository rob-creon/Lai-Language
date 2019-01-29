package lai.ast;

public class LaiIdentifier extends Node {

	public final String identifier;

	public LaiIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	protected String getDebugName() {
		return "<LaiIdentifier:" + identifier + ">";
	}

	@Override
	public void resetNodeReferences() {

	}
}
