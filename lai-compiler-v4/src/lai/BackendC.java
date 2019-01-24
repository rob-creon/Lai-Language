package lai;

import java.util.ArrayList;

import lai.AST.LaiContents;
import lai.AST.LaiExpression;
import lai.AST.LaiExpressionAddition;
import lai.AST.LaiExpressionIntLiteral;
import lai.AST.LaiExpressionMinus;
import lai.AST.LaiExpressionStringLiteral;
import lai.AST.LaiExpressionVariable;
import lai.AST.LaiFile;
import lai.AST.LaiFunction;
import lai.AST.LaiStatement;
import lai.AST.LaiStatementFunctionCall;
import lai.AST.LaiStatementSetVar;
import lai.AST.LaiType;
import lai.AST.LaiVariable;

public class BackendC extends Backend {

	private ArrayList<LaiFile> files;
	private ArrayList<LaiFunction> functions = new ArrayList<LaiFunction>();
	private ArrayList<LaiVariable> variables = new ArrayList<LaiVariable>();

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

	private String getFunctionName(LaiFunction f) {
		if (f.isCImport) {
			return f.Cname;
		} else {
			return f.identifier.identifier + functions.indexOf(f);
		}
	}

	private String getVariableName(LaiVariable v) {
		return v.identifier.identifier + variables.indexOf(v);
	}

	private String getFunctionSignature(LaiFunction f) {
		String output = "";
		// Function type and name
		output += getCTypeFromLai(f.returnType) + " " + getFunctionName(f) + "(";

		// Function parameters
		for (int i = 0; i < f.params.list_children.size(); ++i) {
			LaiVariable p = f.params.list_children.get(i);
			output += getCTypeFromLai(p.type) + " " + getVariableName(p);
			// arg vars should have the prefix for consistency and simplicity.

			if (i != f.params.list_children.size() - 1) {
				output += ", ";
			}
		}
		output += ")";
		return output;
	}

	private String parseFunctionDeclarations(LaiContents c) {

		String output = "";
		for (LaiFunction f : c.functions.list_children) {
			if (!f.isCImport) { // C imported functions dont need to be declared
				output += getFunctionSignature(f) + ";\n";
				output += parseFunctionDeclarations(f.contents);
			}
		}

		return output;
	}

	private String parseFunctionDefinitions(LaiContents c) {
		String output = "";
		for (LaiFunction f : c.functions.list_children) {
			if (!f.isCImport) { // C imported functions dont need to be declared
				output += getFunctionSignature(f) + "{\n";
				output += parseContents(f.contents, false);
				output += "}\n";
				output += parseFunctionDefinitions(f.contents);
			}
		}

		return output;
	}

	private String parseStatement(LaiStatement statement) {
		String output = "";

		if (statement instanceof LaiStatementFunctionCall) {
			LaiStatementFunctionCall FunctionCall = (LaiStatementFunctionCall) statement;
			output += getFunctionName(FunctionCall.function);
			output += "(";
			for (int i = 0; i < FunctionCall.params.list_children.size(); ++i) {
				LaiExpression e = FunctionCall.params.list_children.get(i);
				output += parseExpression(e);
				if (i != FunctionCall.params.list_children.size() - 1) {
					output += ", ";
				}
			}
			output += ")";
			return output;
		} else if (statement instanceof LaiStatementSetVar) {
			LaiStatementSetVar SetVar = (LaiStatementSetVar) statement;
			output += getVariableName(SetVar.var) + " = " + parseExpression(SetVar.exp);
			return output;
		}

		return "unknown statement";
	}

	private String parseExpression(LaiExpression expression) {
		if (expression instanceof LaiExpressionStringLiteral) {
			LaiExpressionStringLiteral stringLiteral = (LaiExpressionStringLiteral) expression;
			return "\"" + stringLiteral.literalValue + "\"";
		} else if (expression instanceof LaiExpressionIntLiteral) {
			LaiExpressionIntLiteral intLiteral = (LaiExpressionIntLiteral) expression;
			return "" + intLiteral.literalValue;
		} else if (expression instanceof LaiExpressionAddition) {
			LaiExpressionAddition addition = (LaiExpressionAddition) expression;
			return parseExpression(addition.expA) + " + " + parseExpression(addition.expB);
		} else if (expression instanceof LaiExpressionMinus) {
			LaiExpressionMinus addition = (LaiExpressionMinus) expression;
			return parseExpression(addition.expA) + " - " + parseExpression(addition.expB);
		} else if (expression instanceof LaiExpressionVariable) {
			LaiExpressionVariable var = (LaiExpressionVariable) expression;
			return getVariableName(var.var);
		}

		return "unknown expression";
	}

	private String getGlobalVariableDeclarations(LaiContents contents) {
		String output = "";
		for (LaiVariable v : contents.variables.list_children) {
			output += getCTypeFromLai(v.type) + " " + getVariableName(v) + ";\n";
		}
		return output;
	}

	private String parseContents(LaiContents contents, boolean isMain) {
		String output = "";

		if (!isMain)// We don't put global variable declarations here because we already have
			// Variable declarations
			for (LaiVariable v : contents.variables.list_children) {
				output += "\t" + getCTypeFromLai(v.type) + " " + getVariableName(v) + ";\n";
			}

		if (isMain) {
			output += "\nint main() {\n";
		}

		for (LaiStatement s : contents.statements.list_children) {
			output += "\t" + parseStatement(s) + ";\n";
		}

		if (isMain) {
			output += "\n}";
		}
		return output;
	}

	private void addFunctions(LaiContents c) {
		variables.addAll(c.variables.list_children);
		for (LaiFunction f : c.functions.list_children) {
			if (!f.isCImport) {
				addFunctions(f.contents);
				functions.add(f);
				variables.addAll(f.params.list_children);
			}
		}
	}

	@Override
	public String compile() {
		String output = "";

		// Generate function IDs
		for (LaiFile f : files)
			addFunctions(f.contents);

		output += getIncludes();
		for (LaiFile f : files)
			output += getGlobalVariableDeclarations(f.contents) + parseFunctionDeclarations(f.contents)
					+ parseFunctionDefinitions(f.contents) + parseContents(f.contents, true);

		return output;
	}

}
