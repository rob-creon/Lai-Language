package lai.ast;

public class LaiExpressionBoolNotEquals extends LaiExpressionBasicMath {

	public LaiExpressionBoolNotEquals(LaiExpression expA, LaiExpression expB) {
		super(expA, expB);
	}

	@Override
	protected String getDebugName() {
		return "<LaiExpressionBoolNotEquals>";
	}

	@Override
	protected LaiType getDefaultReturnType() {
		return new LaiType(LaiType.Type.LaiTypeBoolean);
	}
}
