import java.util.ArrayList;

public class LaiAST {

	public abstract static class Node {
		public int lineNumber, charNumber;
		public ArrayList<Node> children = new ArrayList<Node>();

		public Node(int lineNumber, int charNumber) {
			this.lineNumber = lineNumber;
			this.charNumber = charNumber;
		}

		public abstract String getNodeName();
	}

	public static class FileNode extends Node {

		public final String filename;

		public FileNode(String filename) {
			super(0, 0);
			this.filename = filename;
		}

		@Override
		public String getNodeName() {
			return "FileRoot(" + filename + ")";
		}

	}

	public static enum LaiType {

		TypeString("string"), TypeInt("int");

		public final String name;

		private LaiType(String name) {
			this.name = name;
		}

	}

	public static LaiType getASTConvertedType(LaiLexer.TokenType type) {
		if (!type.isPrimitiveType) {
			System.out.println("LaiLexer Token was not a Type!");

			return null;
		}
		for (LaiType t : LaiType.values()) {
			if (t.name.equals(type.name)) {
				return t;
			}
		}
		System.out.println("LaiType not found!");
		return null;
	}

	public static class LaiVariable extends Node {
		public final String name;
		public final LaiType type;

		public LaiVariable(int lineNumber, int charNumber, String name, LaiType type) {
			super(lineNumber, charNumber);
			this.name = name;
			this.type = type;
		}

		@Override
		public String getNodeName() {
			return "Var(" + name + " : " + type + ")";
		}
	}

	public static class CreateVar extends Node {

		public final LaiVariable var;

		public CreateVar(int lineNumber, int charNumber, LaiVariable var) {
			super(lineNumber, charNumber);
			this.var = var;

			this.children.add(var);
		}

		@Override
		public String getNodeName() {
			return "CreateVar";
		}
	}

	public static class SetVar extends Node {
		public final LaiVariable var;
		public Node value;

		public SetVar(int lineNumber, int charNumber, LaiVariable var, Node value) {
			super(lineNumber, charNumber);
			this.var = var;
			this.value = value;

			this.children.add(var);
			this.children.add(value);
		}

		@Override
		public String getNodeName() {
			return "SetVar";
		}
	}

	public static class StringLiteral extends Node {
		public final String value;

		public StringLiteral(int lineNumber, int charNumber, LaiLexer.StringLiteral value) {
			super(lineNumber, charNumber);
			this.value = value.value;
		}

		public StringLiteral(int lineNumber, int charNumber, String value) {
			super(lineNumber, charNumber);
			this.value = value;
		}

		@Override
		public String getNodeName() {
			return "StringLiteral(\"" + value + "\")";
		}
	}

	public static class IntegerLiteral extends Node {
		public final int value;

		public IntegerLiteral(int lineNumber, int charNumber, LaiLexer.IntegerLiteral value) {
			super(lineNumber, charNumber);
			this.value = value.value;
		}

		public IntegerLiteral(int lineNumber, int charNumber, int value) {
			super(lineNumber, charNumber);
			this.value = value;
		}

		@Override
		public String getNodeName() {
			return "IntegerLiteral(" + value + ")";
		}
	}

}
