package lai.ast;

public class LaiExpressionUninitialized extends LaiExpression {
	@Override
	protected String getDebugName() {
		return "<LaiExpressionUninit>";
	}

	@Override
	protected LaiType getDefaultReturnType() {
		return new LaiType(LaiType.Type.LaiTypeUnknown);
	}

	@Override
	public void resetNodeReferences() {

	}
}
