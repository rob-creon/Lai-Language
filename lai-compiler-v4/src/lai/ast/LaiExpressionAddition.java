package lai.ast;

public class LaiExpressionAddition extends LaiExpressionBasicMath {

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