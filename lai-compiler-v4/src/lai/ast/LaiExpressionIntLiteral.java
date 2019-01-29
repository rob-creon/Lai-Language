package lai.ast;

public class LaiExpressionIntLiteral extends LaiExpression {

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

	@Override
	public void resetNodeReferences() {

	}
}