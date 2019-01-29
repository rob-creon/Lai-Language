package lai.ast;

import java.util.ArrayList;

import lai.LaiLexer;

public class LaiFunction extends Node {

	public LaiIdentifier identifier;
	public LaiList<LaiVariable> params;
	public LaiType returnType;
	public LaiContents contents;

	public boolean isCImport;
	public String Cname;

	public ArrayList<LaiLexer.Token> bodyTokens = new ArrayList<LaiLexer.Token>();

	public int identTokenPosition;

	public LaiFunction(LaiIdentifier identifier, LaiList<LaiVariable> params, LaiType type, int identTokenPosition,
			boolean isCImport) {
		this(identifier, params, type, new LaiContents(), identTokenPosition, isCImport);
	}

	public LaiFunction(LaiIdentifier identifier, LaiList<LaiVariable> params, LaiType type, LaiContents contents,
			int identTokenPosition, boolean isCImport) {
		this.identifier = identifier;
		this.returnType = type;
		this.params = params;
		this.contents = contents;
		this.identTokenPosition = identTokenPosition;
		this.isCImport = isCImport;

		node_children.add(identifier);
		node_children.add(type);
		node_children.add(params);
		node_children.add(contents);
	}

	@Override
	protected String getDebugName() {
		return "<LaiFunction>";
	}

	@Override
	public void resetNodeReferences() {
		node_children.clear();
		node_children.add(identifier);
		node_children.add(returnType);
		node_children.add(params);
		node_children.add(contents);
	}

}