package lai;

import java.util.ArrayList;

import lai.ast.LaiContents;
import lai.ast.LaiExpression;
import lai.ast.LaiExpressionAddition;
import lai.ast.LaiExpressionBoolEquals;
import lai.ast.LaiExpressionBoolNotEquals;
import lai.ast.LaiExpressionDivide;
import lai.ast.LaiExpressionFunctionCall;
import lai.ast.LaiExpressionIntLiteral;
import lai.ast.LaiExpressionMinus;
import lai.ast.LaiExpressionMultiply;
import lai.ast.LaiExpressionStringLiteral;
import lai.ast.LaiExpressionVariable;
import lai.ast.LaiFile;
import lai.ast.LaiFunction;
import lai.ast.LaiStatement;
import lai.ast.LaiStatementFunctionCall;
import lai.ast.LaiStatementIf;
import lai.ast.LaiStatementReturn;
import lai.ast.LaiStatementSetVar;
import lai.ast.LaiType;
import lai.ast.LaiVariable;

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
			return "int32_t";
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
		} else if (statement instanceof LaiStatementIf) {
			LaiStatementIf IfState = (LaiStatementIf) statement;
			output += "if(" + parseExpression(IfState.expression) + "){" + parseContents(IfState.contents, false) + "}";
			return output;
		} else if (statement instanceof LaiStatementReturn) {
			LaiStatementReturn Ret = (LaiStatementReturn) statement;
			output += "return " + parseExpression(Ret.exp);
			return output;
		}

		if (!output.equals("")) {
			System.out.println("YOU FORGOT A RETURN AGAIN");
		}
		return "unknown statement";

	}

	private String parseExpression(LaiExpression expression) {

		String output = "(";

		if (expression instanceof LaiExpressionStringLiteral) {
			LaiExpressionStringLiteral stringLiteral = (LaiExpressionStringLiteral) expression;
			output += "\"" + stringLiteral.literalValue + "\"";
		} else if (expression instanceof LaiExpressionIntLiteral) {
			LaiExpressionIntLiteral intLiteral = (LaiExpressionIntLiteral) expression;
			output += "" + intLiteral.literalValue;
			//////////////////////////////////////
		} else if (expression instanceof LaiExpressionAddition) {
			LaiExpressionAddition addition = (LaiExpressionAddition) expression;
			output += parseExpression(addition.expA) + " + " + parseExpression(addition.expB);
		} else if (expression instanceof LaiExpressionMinus) {
			LaiExpressionMinus addition = (LaiExpressionMinus) expression;
			output += parseExpression(addition.expA) + " - " + parseExpression(addition.expB);
		} else if (expression instanceof LaiExpressionMultiply) {
			LaiExpressionMultiply addition = (LaiExpressionMultiply) expression;
			output += parseExpression(addition.expA) + " * " + parseExpression(addition.expB);
		} else if (expression instanceof LaiExpressionDivide) {
			LaiExpressionDivide addition = (LaiExpressionDivide) expression;
			output += parseExpression(addition.expA) + " / " + parseExpression(addition.expB);
			//////////////////////////////////////
		} else if (expression instanceof LaiExpressionBoolEquals) {
			LaiExpressionBoolEquals addition = (LaiExpressionBoolEquals) expression;
			output += parseExpression(addition.expA) + " == " + parseExpression(addition.expB);
		} else if (expression instanceof LaiExpressionBoolNotEquals) {
			LaiExpressionBoolNotEquals addition = (LaiExpressionBoolNotEquals) expression;
			output += parseExpression(addition.expA) + " != " + parseExpression(addition.expB);
		} else if (expression instanceof LaiExpressionVariable) {
			LaiExpressionVariable var = (LaiExpressionVariable) expression;
			output += getVariableName(var.var);
		} else if (expression instanceof LaiExpressionFunctionCall) {
			LaiExpressionFunctionCall call = (LaiExpressionFunctionCall) expression;
			output += getFunctionName(call.function) + "(";
			for (int i = 0; i < call.params.list_children.size(); ++i) {
				output += parseExpression(call.params.list_children.get(i));
				if (i != call.params.list_children.size() - 1) {
					output += ", ";
				}
			}
			output += ")";
		} else {
			output += "unknown expression";
		}

		output += ")";
		return output;

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
