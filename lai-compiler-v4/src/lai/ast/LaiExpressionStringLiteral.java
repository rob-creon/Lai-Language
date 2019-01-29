package lai.ast;

public class LaiExpressionStringLiteral extends LaiExpression {

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

	@Override
	public void resetNodeReferences() {

	}
}
