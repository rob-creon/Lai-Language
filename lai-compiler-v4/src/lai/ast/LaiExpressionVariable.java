package lai.ast;

import lai.ast.LaiType.Type;

public class LaiExpressionVariable extends LaiExpression {

	public LaiVariable var;

	public LaiExpressionVariable(LaiVariable var) {
		this.var = var;
		this.addChild(var);

		super.returnType.type = var.type.type;
	}

	@Override
	protected LaiType getDefaultReturnType() {
		return new LaiType(Type.LaiTypeUnknown); // Overrided in the constructor.
	}

	@Override
	protected String getDebugName() {
		return "<LaiExpressionVariable>";
	}

	@Override
	public void resetNodeReferences() {
		node_children.clear();
		this.addChild(var);
	}

}