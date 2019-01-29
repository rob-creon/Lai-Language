package lai.ast;

public class LaiExpressionDivide extends LaiExpressionBasicMath {

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