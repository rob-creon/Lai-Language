package lai;

import java.util.ArrayList;

import lai.AST.LaiFile;
import lai.AST.LaiFunction;
import lai.AST.LaiType;

public class BackendC extends Backend {

	private ArrayList<LaiFile> files;

	public BackendC(ArrayList<LaiFile> files) {
		this.files = files;
	}

	private String getCTypeFromLai(LaiType type) {
		LaiType.Type t = type.type;

		switch (t) {
		case LaiInteger:
			return "int16_t";
		case LaiChar:
			return "unsigned char";
		case LaiString:
			return "unsigned char*";
		case LaiTypeUnknown:
			System.err.println("LaiType passed to code generator was unknown type.");
			return null;
		default:
			System.err.println("C generator doesn't support the return type '" + t.name() + "'.");
			return null;
		}
	}

	private String assembleFunctionSignature(LaiFunction f) {

		String type = getCTypeFromLai(f.returnType);
		String name = f.identifier.identifier;

		return type + " " + name;
	}

	private String getFunctionForwardDeclarations() {
		String declarations = "";
		for (LaiFile fileNode : files) {
			for (LaiFunction f : fileNode.contents.functions.list_children) {
				declarations += assembleFunctionSignature(f) + "();\n";
			}
		}
		return declarations;
	}

	@Override
	public String compile() {
		return getFunctionForwardDeclarations();
	}
}
