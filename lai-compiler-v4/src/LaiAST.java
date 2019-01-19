import java.util.ArrayList;

public class LaiAST {

	public static class Node {
		public int lineNumber, charNumber;
		public ArrayList<Object> children = new ArrayList<Object>();

		public Node(int lineNumber, int charNumber) {
			this.lineNumber = lineNumber;
			this.charNumber = charNumber;
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

	public static class LaiVariable<T> {
		public final String name;
		public final LaiType type;
		public T value;

		public LaiVariable(String name, LaiType type) {
			this.name = name;
			this.type = type;
		}
	}

	public static class CreateVar extends Node {

		public final LaiVariable var;

		public CreateVar(int lineNumber, int charNumber, LaiVariable var) {
			super(lineNumber, charNumber);
			this.var = var;
		}
	}

}
