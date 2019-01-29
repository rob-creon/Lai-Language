package lai.ast;

public class LaiStatementReturn extends LaiStatement {

	public LaiExpression exp;

	public LaiStatementReturn(LaiExpression exp) {
		this.exp = exp;
		addChild(exp);
	}

	@Override
	protected String getDebugName() {
		return "<LaiStatementReturn>";
	}

	@Override
	public void resetNodeReferences() {
		this.node_children.clear();
		addChild(exp);
	}
}
