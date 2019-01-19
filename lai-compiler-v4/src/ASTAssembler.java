import java.util.ArrayList;

public class ASTAssembler {

	private class ASTFile {
		public String filename;
		private LaiAST.Node root;

		public ASTFile(String filename) {
			this.filename = filename;
			root = new LaiAST.Node(0, 0); // The file's root node is defined at line 0, char 0 for simplicity's sake.
		}
	}

	public LaiAST.Node getFileAST(String filename) {
		for (ASTFile file : files) {
			if (file.filename.equals(filename)) {
				return file.root;
			}
		}
		return null;// TODO error message
	}

	private ArrayList<ASTFile> files;

	public ASTAssembler() {
		files = new ArrayList<ASTFile>();
	}

	public LaiAST.LaiVariable createVariable(ASTFile file, LaiLexer.Identifier ident, LaiLexer.Token type) {

		if (type.type == LaiLexer.TokenType.TypeString) {
			LaiAST.LaiVariable<String> var = new LaiAST.LaiVariable<String>(ident.value, LaiAST.LaiType.TypeString);
			return var;
		} else if (type.type == LaiLexer.TokenType.TypeInt) {
			LaiAST.LaiVariable<Integer> var = new LaiAST.LaiVariable<Integer>(ident.value, LaiAST.LaiType.TypeInt);
			return var;
		} else {
			Main.error(file.filename, type.lineNumber, type.lineNumber,
					"Expected type, but got " + type.type + " instead.");
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public LaiAST.LaiVariable createVariable(ASTFile file, LaiLexer.Identifier ident, LaiLexer.ValuedToken value) {
		LaiAST.LaiType varType = null;

		if (value instanceof LaiLexer.Identifier) {
			// TODO type inferencing when set to other identifiers
		} else {

			// For String values
			if (value.type == LaiLexer.TokenType.StringLiteral) {
				varType = LaiAST.LaiType.TypeString;
				LaiAST.LaiVariable<String> var = new LaiAST.LaiVariable<String>(ident.value, varType);
				var.value = ((LaiLexer.ValuedToken<String>) value).value;
				return var;
			}

			// For Integer values
			if (value.type == LaiLexer.TokenType.IntegerLiteral) {
				varType = LaiAST.LaiType.TypeInt;
				LaiAST.LaiVariable<Integer> var = new LaiAST.LaiVariable<Integer>(ident.value, varType);
				var.value = ((LaiLexer.ValuedToken<Integer>) value).value;
				return var;
			}

		}

		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void parseFile(String filename, ArrayList<LaiLexer.Token> tokens) {

		ASTFile file = new ASTFile(filename);
		files.add(file);

		for (int tokenIndex = 0; tokenIndex < tokens.size(); tokenIndex++) {
			LaiLexer.Token token = tokens.get(tokenIndex);

			/*
			 * Operator :=
			 */
			if (token.type == LaiLexer.TokenType.OpInferTypeAssignValue) {
				if (tokenIndex == 0) {
					Main.error(filename, token.lineNumber, token.charNumber, "Expected identifier to the left of :=");
					continue;
				}
				// Take the identifier and value tokens from the left and right side of :=
				LaiLexer.Token ident_check = tokens.get(tokenIndex - 1);
				LaiLexer.Token value_check = tokens.get(tokenIndex + 1);

				/*
				 * Error Conditions!
				 */
				if (ident_check.type != LaiLexer.TokenType.Identifier) {
					Main.error(filename, ident_check.lineNumber, ident_check.charNumber,
							"Expected identifier, but got " + ident_check.type + " instead.");
				}
				if (!(value_check instanceof LaiLexer.ValuedToken)) {
					Main.error(filename, value_check.lineNumber, value_check.charNumber,
							"Expected value, but got " + value_check.type + " instead.");
				}

				// We just did error checking, this will work.
				LaiLexer.Identifier ident = (LaiLexer.Identifier) ident_check;
				LaiLexer.ValuedToken value = (LaiLexer.ValuedToken) value_check;

				LaiAST.LaiVariable var = createVariable(file, ident, value);
				LaiAST.CreateVar createVarStatement = new LaiAST.CreateVar(ident_check.lineNumber,
						ident_check.charNumber, var);
				file.root.children.add(createVarStatement);
			} else

			/*
			 * Operator :
			 */

			if (token.type == LaiLexer.TokenType.OpTypeDec) {
				if (tokenIndex == 0) {
					Main.error(filename, token.lineNumber, token.charNumber, "Expected identifier to the left of :");
					continue;
				}
				LaiLexer.Token ident_check = tokens.get(tokenIndex - 1);
				LaiLexer.Token type_check = tokens.get(tokenIndex + 1);
				LaiLexer.Token assign_op_check = tokens.get(tokenIndex + 2);

				/*
				 * Errors
				 */
				if (ident_check.type != LaiLexer.TokenType.Identifier) {
					Main.error(filename, ident_check.lineNumber, ident_check.charNumber,
							"Expected identifier, but got " + ident_check.type + " instead.");
					continue;
				}
				LaiAST.LaiVariable var = createVariable(file, (LaiLexer.Identifier) ident_check, type_check);

				// Convert the type declaration to LaiAST.LaiType
				LaiAST.LaiType convertedType = LaiAST.getASTConvertedType(type_check.type);

				// If there is an '=' op, we are also setting a value.
				if (assign_op_check.type == LaiLexer.TokenType.OpAssignValue) {
					LaiLexer.Token value_check = tokens.get(tokenIndex + 3);

					if (!(value_check instanceof LaiLexer.ValuedToken)
							&& !(value_check.type == LaiLexer.TokenType.UnitializeValue)) {
						Main.error(filename, value_check.lineNumber, value_check.charNumber,
								"Expected value, but got " + value_check.type + " instead.");
					}

					// If it is explicitly uninitialized, set the value to null
					if (value_check.type == LaiLexer.TokenType.UnitializeValue) {
						var.value = null;
					} else if (convertedType == LaiAST.LaiType.TypeString) {// If it is a string type, check it against
																			// the literal value.
						if (value_check.type == LaiLexer.TokenType.StringLiteral) {
							// Good! We're done.
							var.value = ((LaiLexer.StringLiteral) value_check).value;
						} else {
							Main.error(filename, value_check.lineNumber, value_check.charNumber,
									"Expected StringLiteral but got '" + value_check.type + "' instead.");
						}
					}

					else if (convertedType == LaiAST.LaiType.TypeInt) {// If it is a integer type, check it against the
																		// integer value.
						if (value_check.type == LaiLexer.TokenType.IntegerLiteral) {
							// Good!
							var.value = ((LaiLexer.IntegerLiteral) value_check).value;
						} else {
							Main.error(filename, value_check.lineNumber, value_check.charNumber,
									"Expected IntegerLiteral but got '" + value_check.type + "' instead.");
						}
					}

				} else {
					// If there is no assign operator, then we can initialize to a default value.
					if (convertedType == LaiAST.LaiType.TypeInt) {
						var.value = 0;
					} else if (convertedType == LaiAST.LaiType.TypeString) {
						var.value = "";
					}
				}

				LaiAST.CreateVar createVarStatement = new LaiAST.CreateVar(ident_check.lineNumber,
						ident_check.charNumber, var);
				file.root.children.add(createVarStatement);
			}
		}
	}
}
