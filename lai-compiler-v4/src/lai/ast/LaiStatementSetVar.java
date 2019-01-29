package lai.ast;

public class LaiStatementSetVar extends LaiStatement {

	public LaiVariable var;
	public LaiExpression exp;

	public LaiStatementSetVar(LaiVariable var, LaiExpression exp) {
		this.var = var;
		this.exp = exp;

		this.node_children.add(var);
		this.node_children.add(exp);
	}

	@Override
	protected String getDebugName() {
		return "<SetVar>";
	}

	@Override
	public void resetNodeReferences() {
		node_children.clear();
		this.node_children.add(var);
		this.node_children.add(exp);
	}
}
