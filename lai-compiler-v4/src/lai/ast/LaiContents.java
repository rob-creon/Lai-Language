package lai.ast;

public class LaiContents extends Node {

	public LaiList<LaiFunction> functions;
	public LaiList<LaiVariable> variables;
	public LaiList<LaiStatement> statements;

	public LaiContents() {
		functions = new LaiList<LaiFunction>("LaiFunction");
		variables = new LaiList<LaiVariable>("LaiVariable");
		statements = new LaiList<LaiStatement>("LaiStatement");

		node_children.add(functions);
		node_children.add(variables);
		node_children.add(statements);
	}

	@Override
	protected String getDebugName() {
		return "<LaiContents>";
	}

	@Override
	public void resetNodeReferences() {
		node_children.clear();

		node_children.add(functions);
		node_children.add(variables);
		node_children.add(statements);
	}
}
