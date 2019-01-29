package lai.ast;

import java.util.ArrayList;

public class LaiList<T extends Node> extends Node {
	private final String debugName;
	public ArrayList<T> list_children;

	public LaiList(String debugName) {
		this.debugName = debugName;
		this.list_children = new ArrayList<T>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addChild(Node n) {
		try {
			list_children.add((T) n);
		} catch (Exception e) {
			System.err.println("COMPILER ERROR: " + "tried to add incompatible type to AST.LaiList<T>");
			return;
		}
		if (n instanceof LaiStatementFunctionCall) {
			System.out.println();
		}
		n.node_parent = this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getDebugString(int layer) {
		return super.getDSFromList(layer, (ArrayList<Node>) list_children);
	}

	@Override
	protected String getDebugName() {
		return "LaiList<" + debugName + ">";
	}

	@Override
	public void resetNodeReferences() {
//		this.node_children.clear();
//		this.addChild("");
	}
}