package lai.ast;

public abstract class LaiExpressionBasicMath extends LaiExpression {
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

	@Override
	public void resetNodeReferences() {
		node_children.clear();
		this.addChild(expA);
		this.addChild(expB);
	}
}