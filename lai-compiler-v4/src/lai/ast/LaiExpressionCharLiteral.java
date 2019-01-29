package lai.ast;

public class LaiExpressionCharLiteral extends LaiExpression {

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

	@Override
	public void resetNodeReferences() {

	}
}
