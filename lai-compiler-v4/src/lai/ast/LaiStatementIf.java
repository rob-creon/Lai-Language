package lai.ast;

public class LaiStatementIf extends LaiStatement {

	public LaiExpression expression;
	public LaiContents contents;

	public LaiStatementIf(LaiExpression exp, LaiContents contents) {
		this.expression = exp;
		this.contents = contents;

		this.node_children.add(exp);
		this.node_children.add(contents);
	}

	@Override
	protected String getDebugName() {
		return "<LaiIfStatement>";
	}

	@Override
	public void resetNodeReferences() {
		this.node_children.add(expression);
		this.node_children.add(contents);
	}
}