package lai;

import java.util.ArrayList;

public class AST {

	public static abstract class Node {

		public Node node_parent;
		public ArrayList<Node> node_children;

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
			return getDSFromList(layer, node_children);
		}

		public void addChild(Node n) {
			n.node_parent = this;
			node_children.add(n);
		}

		protected abstract String getDebugName();
	}

	public static class LaiFile extends Node {
		public final String filename;

		public LaiFile(String filename) {
			this.filename = filename;
		}

		@Override
		protected String getDebugName() {
			return "<LaiFile:" + filename + ">";
		}
	}

	public static class LaiContents extends Node {

		public LaiList<LaiFunction> functions = new LaiList<LaiFunction>("LaiFunction");
		public LaiList<LaiVariable> variables = new LaiList<LaiVariable>("LaiVariable");
		public LaiList<LaiStatement> statements = new LaiList<LaiStatement>("LaiStatement");

		protected LaiContents() {
			node_children.add(functions);
			node_children.add(variables);
			node_children.add(statements);
		}

		@Override
		protected String getDebugName() {
			return "<LaiContents>";
		}
	}

	public static class LaiList<T extends Node> extends Node {
		private final String debugName;
		public ArrayList<T> list_children = new ArrayList<T>();

		public LaiList(String debugName) {
			this.debugName = debugName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addChild(Node n) {
			try {
				list_children.add((T) n);
			} catch (Exception e) {
				System.out.println("COMPILER ERROR: " + "tried to add incompatible type to AST.LaiList<T>");
				return;
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
	}

	public static class LaiType extends Node {

		public static enum Type {
			LaiInteger, LaiString, LaiTypeUnknown,
		}

		public final Type type;

		public LaiType(Type type) {
			this.type = type;
		}

		@Override
		protected String getDebugName() {
			return "<LaiType:" + type.name() + ">";
		}

	}

	public static class LaiFunction extends Node {

		public LaiIdentifier identifier;
		public LaiList<LaiVariable> params;
		public LaiType returnType;
		public LaiContents contents;

		public LaiFunction(LaiIdentifier identifier, LaiList<LaiVariable> params, LaiType type) {
			this(identifier, params, type, new LaiContents());

		}

		public LaiFunction(LaiIdentifier identifier, LaiList<LaiVariable> params, LaiType type, LaiContents contents) {
			this.identifier = identifier;
			this.returnType = type;
			this.params = params;

			node_children.add(identifier);
			node_children.add(type);
			node_children.add(contents);
		}

		@Override
		protected String getDebugName() {
			return "<LaiFunction>";
		}

	}

	public static class LaiVariable extends Node {
		public LaiVariable() {
		}

		@Override
		protected String getDebugName() {
			return "<LaiVariable>";
		}
	}

	public static class LaiStatement extends Node {
		public LaiStatement() {
		}

		@Override
		protected String getDebugName() {
			return "<LaiStatement>";
		}
	}

	public static class LaiIdentifier extends Node {

		public final String identifier;

		protected LaiIdentifier(String identifier) {
			this.identifier = identifier;
		}

		@Override
		protected String getDebugName() {
			return "<LaiIdentifier:" + identifier + ">";
		}

	}
}
