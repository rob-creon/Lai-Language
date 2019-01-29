package lai.ast;

public class LaiExpressionBoolEquals extends LaiExpressionBasicMath {

	public LaiExpressionBoolEquals(LaiExpression expA, LaiExpression expB) {
		super(expA, expB);
	}

	@Override
	protected String getDebugName() {
		return "<LaiExpressionBoolEquals>";
	}

	@Override
	protected LaiType getDefaultReturnType() {
		return new LaiType(LaiType.Type.LaiTypeBoolean);
	}
}
