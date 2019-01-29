package lai.ast;

public class LaiFile extends Node {
	public final String filename;
	public LaiContents contents;

	public LaiFile(String filename) {
		this.filename = filename;
		this.contents = new LaiContents();
	}

	@Override
	protected String getDebugName() {
		return "<LaiFile:" + filename + ">";
	}

	@Override
	public void resetNodeReferences() {
		node_children.clear();
		addChild(contents);
	}
}