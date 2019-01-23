package lai;

import java.util.ArrayList;

import lai.AST.*;

public class BackendC extends Backend {

	private ArrayList<LaiFile> files;

	public BackendC(ArrayList<LaiFile> files) {
		this.files = files;
	}

	private String getIncludes() {
		return "#include <stdio.h>\n#include <stdlib.h>\n";
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

	private String getVariableSignature(LaiVariable v) {
		//return 
		return "";
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
				if (f.isCImport) {
					// The function is already natively supported in C. We have no use for it's
					// definition.
				} else {
					declarations += assembleFunctionSignature(f) + "();\n";
				}
			}
		}
		return declarations;
	}

	private String getStatements() {

		String statements = "int main(void) {";

		for (LaiFile fileNode : files) {
			for (LaiStatement f : fileNode.contents.statements.list_children) {
				if (f instanceof LaiStatementFunctionCall) {
					LaiStatementFunctionCall call = (LaiStatementFunctionCall) f;

					String CFunctionName = call.function.identifier.identifier;
					if (call.function.isCImport) {
						CFunctionName = call.function.Cname;
					}
					statements += CFunctionName + "(";

					// Create params string
					String params = "";
					for (int i = 0; i < call.params.list_children.size(); ++i) {
						LaiExpression e = call.params.list_children.get(i);
						if (e instanceof LaiExpressionStringLiteral) {
							LaiExpressionStringLiteral lesl = (LaiExpressionStringLiteral) e;
							params += "\"" + lesl.literalValue + "\"";
						}
					}
					statements += params + ");";
				}
			}
		}
		statements += "}";

		return statements;
	}

	@Override
	public String compile() {
		return getIncludes() + getFunctionForwardDeclarations() + getStatements();
	}
}
