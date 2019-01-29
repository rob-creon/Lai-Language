package lai.ast;

public class LaiStatementFunctionCall extends LaiStatement {

	public LaiFunction function;
	public LaiList<LaiExpression> params;

	public LaiStatementFunctionCall(LaiFunction function, LaiList<LaiExpression> functionParams) {
		this.function = function;
		this.params = functionParams;

		super.addChild(function);
		super.addChild(functionParams);
	}

	@Override
	protected String getDebugName() {
		return "<LaiStatementFunctionCall>";
	}

	@Override
	public void resetNodeReferences() {
		this.node_children.clear();
		super.addChild(function);
		super.addChild(params);
	}
}
