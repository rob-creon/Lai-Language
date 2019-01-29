package lai.ast;

public abstract class LaiExpression extends Node {

	public LaiType returnType;

	public LaiExpression() {
		returnType = this.getDefaultReturnType();
		this.addChild(returnType);
	}

	public LaiType getReturnType() {
		return returnType;
	}

	protected abstract LaiType getDefaultReturnType();
}
