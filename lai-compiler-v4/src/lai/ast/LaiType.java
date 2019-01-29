package lai.ast;

import lai.LaiLexer;

public class LaiType extends Node {

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
		LaiInteger, LaiString, LaiChar, LaiTypeBoolean, LaiTypeUnknown,
	}

	public Type type;

	public LaiType(Type type) {
		this.type = type;
	}

	@Override
	protected String getDebugName() {
		return "<LaiType:" + type.name() + ">";
	}

	@Override
	public void resetNodeReferences() {

	}

}
