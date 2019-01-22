package lai;

import java.util.ArrayList;

import lai.AST.LaiType.Type;

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
		public LaiContents contents;

		public LaiFile(String filename) {
			this.filename = filename;
			this.contents = new LaiContents("File Origin");
		}

		@Override
		protected String getDebugName() {
			return "<LaiFile:" + filename + ">";
		}
	}

	public static class LaiContents extends Node {

		public LaiList<LaiFunction> functions;
		public LaiList<LaiVariable> variables;
		public LaiList<LaiStatement> statements;

		protected LaiContents(LaiContents copy) {
			this.node_parent = copy.node_parent;
			this.functions = new LaiList<LaiFunction>(copy.functions);
			this.variables = new LaiList<LaiVariable>(copy.variables);
			this.statements = new LaiList<LaiStatement>(copy.statements);
		}

		protected LaiContents(String deb) {
			System.out.println("initing contents " + deb);

			functions = new LaiList<LaiFunction>("LaiFunction");
			variables = new LaiList<LaiVariable>("LaiVariable");
			statements = new LaiList<LaiStatement>("LaiStatement");

			node_children.add(functions);
			node_children.add(variables);
			node_children.add(statements);
		}

		@Override
		public String getDebugString(int layer) {
			return super.getDebugString(layer);
		}

		@Override
		protected String getDebugName() {
			return "<LaiContents>";
		}
	}

	public static class LaiList<T extends Node> extends Node {
		private final String debugName;
		public ArrayList<T> list_children;

		public LaiList(String debugName) {
			this.debugName = debugName;
			this.list_children = new ArrayList<T>();
		}

		public LaiList(LaiList<T> copy) {
			this.debugName = copy.debugName;
			this.list_children = new ArrayList<T>(copy.list_children);
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

		public static LaiType convertLexerTypeToLaiType(LaiLexer.Token t) {
			return new LaiType(convertLexerTypeToLaiType(t.type));
		}

		private static Type convertLexerTypeToLaiType(LaiLexer.TokenType t) {
			if (!t.isPrimitiveType) {
				System.err.println("somewhere there was a fuck up --convertLexerTypeToLaiType(), AST.java");
				return null;
			}
			if (t == LaiLexer.TokenType.TypeInt) {
				return Type.LaiInteger;
			}
			if (t == LaiLexer.TokenType.TypeString) {
				return Type.LaiString;
			}
			if (t == LaiLexer.TokenType.TypeChar) {
				return Type.LaiChar;
			}
			System.err.println("how the heck did that type get past?? --convertLexerTypeToLaiType(), AST.java");
			return null;
		}

		public static enum Type {
			LaiInteger, LaiString, LaiChar, LaiTypeUnknown,
		}

		public Type type;

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

		public boolean isCImport;

		public ArrayList<LaiLexer.Token> bodyTokens = new ArrayList<LaiLexer.Token>();

		public int identTokenPosition;

		public LaiFunction(LaiIdentifier identifier, LaiList<LaiVariable> params, LaiType type, int identTokenPosition,
				boolean isCImport) {
			this(identifier, params, type, new LaiContents("from " + identifier.identifier), identTokenPosition,
					isCImport);
		}

		public LaiFunction(LaiIdentifier identifier, LaiList<LaiVariable> params, LaiType type, LaiContents contents,
				int identTokenPosition, boolean isCImport) {
			this.identifier = identifier;
			this.returnType = type;
			this.params = params;
			this.contents = contents;
			this.identTokenPosition = identTokenPosition;
			this.isCImport = isCImport;

			node_children.add(identifier);
			node_children.add(type);
			node_children.add(params);
			node_children.add(contents);
		}

		@Override
		protected String getDebugName() {
			return "<LaiFunction>";
		}

	}

	public static class LaiVariable extends Node {

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
	}

	public static abstract class LaiStatement extends Node {

	}

	public static class LaiStatementSetVar extends LaiStatement {

		public LaiVariable var;
		public LaiExpression exp;

		public LaiStatementSetVar(LaiVariable var, LaiExpression exp) {
			this.var = var;
			this.exp = exp;

			this.node_children.add(var);
			this.node_children.add(exp);
		}

		@Override
		protected String getDebugName() {
			return "<SetVar>";
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

	public static abstract class LaiExpression extends Node {

		protected LaiType returnType;

		public LaiExpression() {
			returnType = this.getDefaultReturnType();
			this.addChild(returnType);
		}

		public LaiType getReturnType() {
			return returnType;
		}

		protected abstract LaiType getDefaultReturnType();
	}

	public static class LaiExpressionIntLiteral extends LaiExpression {

		public int literalValue;

		public LaiExpressionIntLiteral(int value) {
			this.literalValue = value;
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionIntLiteral=" + literalValue + ">";
		}

		@Override
		protected LaiType getDefaultReturnType() {
			return new LaiType(LaiType.Type.LaiInteger);
		}
	}

	public static class LaiExpressionStringLiteral extends LaiExpression {

		public String literalValue;

		public LaiExpressionStringLiteral(String value) {
			this.literalValue = value;
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionStringLiteral=\"" + literalValue + "\">";
		}

		@Override
		protected LaiType getDefaultReturnType() {
			return new LaiType(LaiType.Type.LaiString);
		}
	}

	public static class LaiExpressionCharLiteral extends LaiExpression {

		public char literalValue;

		public LaiExpressionCharLiteral(char value) {
			this.literalValue = value;
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionCharLiteral='" + literalValue + "'>";
		}

		@Override
		protected LaiType getDefaultReturnType() {
			return new LaiType(LaiType.Type.LaiChar);
		}
	}

	public static class LaiExpressionUninit extends LaiExpression {

		@Override
		protected String getDebugName() {
			return "<LaiExpressionUninit>";
		}

		@Override
		protected LaiType getDefaultReturnType() {
			return new LaiType(LaiType.Type.LaiTypeUnknown);
		}

	}

	public static class LaiExpressionVariable extends LaiExpression {

		public LaiVariable var;

		public LaiExpressionVariable(LaiVariable var) {
			this.var = var;
			this.addChild(var);

			super.returnType.type = var.type.type;
		}

		@Override
		protected LaiType getDefaultReturnType() {
			return new LaiType(Type.LaiTypeUnknown); // Overrided in the constructor.
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionVariable>";
		}

	}

	public static abstract class LaiExpressionBasicMath extends LaiExpression {
		public LaiExpression expA;
		public LaiExpression expB;

		public LaiExpressionBasicMath(LaiExpression expA, LaiExpression expB) {
			this.expA = expA;
			this.expB = expB;

			this.addChild(expA);
			this.addChild(expB);
		}

		@Override
		protected LaiType getDefaultReturnType() {
			return new LaiType(LaiType.Type.LaiTypeUnknown);
		}

	}

	public static class LaiExpressionAddition extends LaiExpressionBasicMath {

		public LaiExpressionAddition(LaiExpression expA, LaiExpression expB) {
			super(expA, expB);
			if (expA.returnType.type != expB.returnType.type) {
				// There is only one case where this is okay, which is when the first expression
				// is a string
				if (expA.returnType.type == LaiType.Type.LaiString) {
					this.returnType = new LaiType(LaiType.Type.LaiString);
				} else {
					this.returnType = new LaiType(LaiType.Type.LaiTypeUnknown);
				}
			} else {
				this.returnType = new LaiType(expA.returnType.type);
			}
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionAddition>";
		}

	}

	public static class LaiExpressionMinus extends LaiExpressionBasicMath {

		public LaiExpressionMinus(LaiExpression expA, LaiExpression expB) {
			super(expA, expB);
			if (expA.returnType.type != expB.returnType.type) {
				// This is never okay (er until we add floats i guess). //TODO
				this.returnType = new LaiType(LaiType.Type.LaiTypeUnknown);
			} else {
				this.returnType = new LaiType(expA.returnType.type);
			}
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionMinus>";
		}

	}

	public static class LaiExpressionMultiply extends LaiExpressionBasicMath {

		public LaiExpressionMultiply(LaiExpression expA, LaiExpression expB) {
			super(expA, expB);
			if (expA.returnType.type != expB.returnType.type) {
				// This is never okay (er until we add floats i guess). //TODO
				this.returnType = new LaiType(LaiType.Type.LaiTypeUnknown);
			} else {
				this.returnType = new LaiType(expA.returnType.type);
			}
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionMultiply>";
		}
	}

	public static class LaiExpressionDivide extends LaiExpressionBasicMath {

		public LaiExpressionDivide(LaiExpression expA, LaiExpression expB) {
			super(expA, expB);
			if (expA.returnType.type != expB.returnType.type) {
				// This is never okay (er until we add floats i guess). //TODO
				this.returnType = new LaiType(LaiType.Type.LaiTypeUnknown);
			} else {
				this.returnType = new LaiType(expA.returnType.type);
			}
		}

		@Override
		protected String getDebugName() {
			return "<LaiExpressionDivide>";
		}
	}

	public static class LaiStatementFunctionCall extends LaiStatement {

		public LaiFunction function;
		public LaiList<LaiExpression> params;

		public LaiStatementFunctionCall(LaiFunction function, LaiList<LaiExpression> functionParams) {
			this.function = function;
			this.params = functionParams;

			super.addChild(function);
			super.addChild(functionParams);
		}

		@Override
		protected String getDebugName() {
			return "<LaiStatementFunctionCall>";
		}

	}
}
