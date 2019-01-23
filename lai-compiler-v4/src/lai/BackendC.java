package lai;

import java.util.ArrayList;

import lai.AST.LaiContents;
import lai.AST.LaiFile;
import lai.AST.LaiFunction;
import lai.AST.LaiType;
import lai.AST.LaiVariable;

public class BackendC extends Backend {

	private ArrayList<LaiFile> files;
	public String functionDeclarations;

	public BackendC(ArrayList<LaiFile> files) {
		this.files = files;
	}

	private String getIncludes() {
		return "#include <stdio.h>\n#include <stdlib.h>\n#include <stdint.h>\n";
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

	private String getFunctionSignature(LaiFunction f, String prefix) {
		String output = "";
		// Function type and name
		output += getCTypeFromLai(f.returnType) + " " + prefix + f.identifier.identifier + "(";

		// Function parameters
		for (int i = 0; i < f.params.list_children.size(); ++i) {
			LaiVariable p = f.params.list_children.get(i);
			output += getCTypeFromLai(p.type) + " " + prefix + f.identifier.identifier + p.identifier.identifier;
			// arg vars should have the prefix for consistency and simplicity.

			if (i != f.params.list_children.size() - 1) {
				output += ", ";
			}
		}
		output += ")";
		return output;
	}

	private String parseFunctionDeclarations(LaiContents c, String prefix) {

		String output = "";
		for (LaiFunction f : c.functions.list_children) {
			if (!f.isCImport) { // C imported functions dont need to be declared
				output += getFunctionSignature(f, prefix) + ";\n";
				output += parseFunctionDeclarations(f.contents, prefix + f.identifier.identifier);
			}
		}

		return output;
	}

	private String parseFunctionDefinitions(LaiContents c, String prefix) {
		String output = "";
		for (LaiFunction f : c.functions.list_children) {
			if (!f.isCImport) { // C imported functions dont need to be declared
				output += getFunctionSignature(f, prefix) + "{";
				output += parseContents(c, prefix, false);
				output += parseFunctionDefinitions(f.contents, prefix + f.identifier.identifier);
			}
		}

		return output;
	}

	private String parseContents(LaiContents contents, String localPrefix, boolean isMain) {
		String output = "";

		if (isMain) {
			output += "\nint main() {\n";
		}

		if (isMain) {
			output += "\n}";
		}
		return output;
	}

	@Override
	public String compile() {
		String output = "";

		output += getIncludes();
		for (LaiFile f : files)
			output += parseFunctionDeclarations(f.contents, "lai_glob_")
					+ parseFunctionDefinitions(f.contents, "lai_glob_") + parseContents(f.contents, "lai_glob_", true);

		return output;
	}

}
