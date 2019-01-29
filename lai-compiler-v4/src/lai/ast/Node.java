package lai.ast;

import java.util.ArrayList;

import lai.Main;

public abstract class Node {

	public Node node_parent;
	public ArrayList<Node> node_children;

	public static final int maxLayers = 10;

	protected Node() {
		node_children = new ArrayList<Node>();
	}

	protected String getDSFromList(int layer, ArrayList<Node> c) {
		String out = Main.getIndents(layer) + this.getDebugName() + "\n";
		for (Node n : c) {
			out += n.getDebugString(layer + 1);
		}

		return out;
	}

	public String getDebugString(int layer) {
		if (layer > maxLayers)
			return Main.getIndents(layer) + getDebugName() + "...Max recursion depth reached.";
		return getDSFromList(layer, node_children);
	}

	public void addChild(Node n) {
		n.node_parent = this;
		node_children.add(n);
	}

	protected abstract String getDebugName();

	/**
	 * Java's pass by reference/value decisions can mess up stuff because of how
	 * childs are stored in lists, so if a node's child is being replaced, it's
	 * reference in the list needs to be updated. This function should just readd
	 * all children to the list.
	 * 
	 * @param none
	 */

	public abstract void resetNodeReferences();
}
