package lai.ast;

public class LaiExpressionFunctionCall extends LaiExpression {

	public LaiFunction function;
	public LaiList<LaiExpression> params;

	public LaiExpressionFunctionCall(LaiFunction function, LaiList<LaiExpression> functionParams) {
		this.function = function;
		this.params = functionParams;
		this.returnType = function.returnType;

		super.addChild(function);
		super.addChild(params);
	}

	@Override
	protected LaiType getDefaultReturnType() {
		return new LaiType(LaiType.Type.LaiTypeUnknown);
	}

	@Override
	protected String getDebugName() {
		return "<LaiExpressionFunctionCall>";
	}

	@Override
	public void resetNodeReferences() {
		this.node_children.clear();
		super.addChild(function);
		super.addChild(params);
	}

}
