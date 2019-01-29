package lai.ast;

public class LaiVariable extends Node {

	public LaiIdentifier identifier;
	public LaiType type;
	public int identTokenPosition;

	public LaiVariable(LaiIdentifier id, LaiType type, int identTokenPosition) {

		this.identifier = id;
		this.type = type;
		this.identTokenPosition = identTokenPosition;

		super.addChild(identifier);
		super.addChild(type);
	}

	@Override
	protected String getDebugName() {
		return "<LaiVariable>";
	}

	@Override
	public void resetNodeReferences() {
		this.node_children.clear();
		super.addChild(identifier);
		super.addChild(type);
	}
}