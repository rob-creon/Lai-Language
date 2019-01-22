package lai;

import java.util.ArrayList;
import java.util.Stack;

import lai.AST.*;
import lai.AST.LaiType.Type;
import lai.LaiLexer.TokenType;

public class ASTAssembler {

	public ArrayList<LaiFile> files;

	private int tokenCTR = 0;
	private LaiLexer.Token token;
	private String filename;

	private ArrayList<LaiLexer.Token> tokens;

	public ASTAssembler() {
		files = new ArrayList<LaiFile>();
	}

	private enum TokenContext {
		SEMICOLON, END, NORMAL
	}

	private TokenContext nextToken() {
		tokenCTR++;
		if (tokenCTR >= tokens.size()) {
			return TokenContext.END;
		} else {
			token = tokens.get(tokenCTR);

			if (token.type == LaiLexer.TokenType.OpSemicolon) {
				return TokenContext.SEMICOLON;
			}
			return TokenContext.NORMAL;
		}
	}

	private boolean safeNextToken() {
		if (nextToken() != TokenContext.NORMAL) {
			Main.error(filename, token.lineNumber, token.charNumber, "Unexpected end.");
			return false;
		}
		return true;
	}

	private void skipToEndOfLine() {
		// Skip to the end of the line
		while (token.type != LaiLexer.TokenType.OpSemicolon) {
			if (nextToken() == TokenContext.END) {
				Main.error(filename, token.lineNumber, token.charNumber, "Unexpected end of file.");
			}
		}
	}

	private void parseVariablesAndFunctionsToContent(String filename, ArrayList<LaiLexer.Token> tokens,
			LaiContents contents) {
		this.tokens = tokens;
		this.filename = filename;

		// First pass we are searching for functions and variables.
		tokenLoop: for (tokenCTR = 0; nextToken() != TokenContext.END;) {

			if (token.type == LaiLexer.TokenType.OpSemicolon) {
				continue;
			}

			if (token.type == LaiLexer.TokenType.Identifier) {
				// If it is an identifier, figure out what it's doing. Is it declaring a
				// variable or a function? Or is it referencing an existing var or function?
				LaiLexer.Identifier ident = (LaiLexer.Identifier) token;
				int ident_token_location = tokenCTR;

				if (!safeNextToken())
					continue tokenLoop;

				/*****************************************************************************
				 * * * * * * * * * * * * * * * * * * FUNCTION * * * * * * * * * * * * * * * *
				 *****************************************************************************/

				if (token.type == LaiLexer.TokenType.OpOpenParenthesis) {
					// This is some kind of function, either a definition or a function call.

					// Check for a {. If it exists, then this is a function definition.
					int lookAheadCTR = tokenCTR;
					LaiLexer.Token lookAheadToken;
					boolean foundMatchingCloseParenthesis = false;
					do {
						lookAheadToken = tokens.get(lookAheadCTR);
						if (lookAheadToken.type == LaiLexer.TokenType.OpOpenBrace
								|| lookAheadToken.type == LaiLexer.TokenType.OpSemicolon) {
							break;
						}
						if (lookAheadToken.type == LaiLexer.TokenType.OpCloseParenthesis) {
							foundMatchingCloseParenthesis = true;
						}
						lookAheadCTR++;
					} while (lookAheadCTR < tokens.size());

					if (!foundMatchingCloseParenthesis) {
						Main.error(filename, lookAheadToken.lineNumber, lookAheadToken.charNumber,
								"Found " + lookAheadToken.type.name + " instead of expected ).");
						continue;
					}

					/*****************************************************************************
					 * * * * * * * * * * * * * * * FUNCTION DEFINITION * * * * * * * * * * * * * *
					 *****************************************************************************/
					if (lookAheadToken.type == LaiLexer.TokenType.OpOpenBrace) {
						// Yep, this is a function definition.

						// Parse parameters
						LaiList<LaiVariable> parameters = new LaiList<LaiVariable>("LaiVariable");

						if (!safeNextToken())
							continue;

						paramLoop: while (token.type != LaiLexer.TokenType.OpCloseParenthesis) {

							if (token.type == LaiLexer.TokenType.Identifier) {
								// This should be a parameter variable name.
								LaiIdentifier paramIdent = new LaiIdentifier(((LaiLexer.Identifier) token).value);

								// Check for the type declaration operator
								if (!safeNextToken())
									continue tokenLoop;
								if (token.type != LaiLexer.TokenType.OpTypeDec) {
									Main.error(filename, token.lineNumber, token.charNumber,
											"Expected : but got '" + token.type.name() + "' instead.");
								}

								// Check for the actual type declaration
								if (!safeNextToken())
									continue tokenLoop;
								if (!token.type.isPrimitiveType) {
									Main.error(filename, token.lineNumber, token.charNumber,
											"Expected type but got '" + token.type.name() + "' instead.");
								}
								// This is the parameter's return type.
								AST.LaiType paramType = AST.LaiType.convertLexerTypeToLaiType(token);

								// Now we can assemble the variable and add it to the parameters list of the
								// function.
								parameters.addChild(new AST.LaiVariable(paramIdent, paramType, ident_token_location));

								// Check if the next thing is a comma, close parenthesis, or something invalid.
								if (!safeNextToken())
									continue tokenLoop;
								if (token.type == LaiLexer.TokenType.OpComma) {
									// Then there are more, so we can just skip the comma and continue parsing
									// parameters.
									if (!safeNextToken())
										continue tokenLoop;

									continue paramLoop;
								} else if (token.type == LaiLexer.TokenType.OpCloseParenthesis) {
									// If the next token is a ), then we have reached the end of the parameter
									// definitions
									break paramLoop;
								} else {
									// Uhoh!
									Main.error(filename, token.lineNumber, token.charNumber,
											"Expected either a , or ), but found '" + token.type.name + " instead.");
									continue tokenLoop;
								}

							} else if (token.type == LaiLexer.TokenType.OpCloseParenthesis) {
								break;
							} else {
								Main.error(filename, token.lineNumber, token.charNumber,
										"Expected parameter identifier, but got " + token.type.name + " instead.");
								continue tokenLoop;
							}
						}
						// If everything worked correctly we should end on the token ")". Let's move
						// onto the next token.
						if (!safeNextToken())
							continue tokenLoop;

						// This should be a : no matter what. Just for aesthetics and understanding.
						if (token.type != LaiLexer.TokenType.OpTypeDec) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Function definitions must always have a return type in this way: functionName(parameter : type, parameter2 : type) : returnType {...}");
						}

						if (!safeNextToken())
							continue tokenLoop;

						// Now our current token should be the return type.
						if (!token.type.isPrimitiveType) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Expected function return type, but got '" + token.type.name + "' instead.");
							continue tokenLoop;
						}
						LaiType funcReturnType = AST.LaiType.convertLexerTypeToLaiType(token);

						LaiFunction function = new LaiFunction(new LaiIdentifier(ident.value), parameters,
								funcReturnType, ident_token_location);
						contents.functions.addChild(function);

						// Now we need to handle the function body, as that will be assembled to AST
						// once we have found all function and variable definitions. We should add it to
						// the function's token list (non-Node, so it won't be displayed, but this will
						// make things easier later).

						if (!safeNextToken())
							continue tokenLoop;
						if (token.type != LaiLexer.TokenType.OpOpenBrace) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Expected { but got '" + token.type.name + "' instead.");
							continue tokenLoop;
						}

						// this isn't always safe because even tho the lexer does a matching brace check
						// before it is passed to the AST, unfortunately this doesn't check the order.
						// so }{ will mess it up. TODO

						// Go to the next token for the sake of the upcoming while loop.
						if (nextToken() == TokenContext.END) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"End of file encountered before function close brace '}'.");
							continue tokenLoop;
						}
						if (token.type == LaiLexer.TokenType.OpCloseBrace) {
							// nani?? Empty codeblock... just leave it be
						} else {
							int openBraceCTR = 1;
							while (openBraceCTR > 0) {

								if (nextToken() == TokenContext.END) {
									Main.error(filename, token.lineNumber, token.charNumber,
											"End of file encountered before function close brace '}'.");
									continue tokenLoop;
								}
								if (token.type == LaiLexer.TokenType.OpCloseBrace) {
									openBraceCTR--;
								}

								if (token.type == LaiLexer.TokenType.OpOpenBrace) {
									openBraceCTR++;
								}

								if (openBraceCTR == 0) {
									break; // We don't want the code below to run if we have completed the block, we
											// don't want the } to be included.
								}

								function.bodyTokens.add(token);
							}
						}

					} else {
						/*****************************************************************************
						 * * * * * * * * * * * * * * * * FUNCTION CALL * * * * * * * * * * * * * * * *
						 *****************************************************************************/
						// We don't do anything here, we're just finding function & variable definitions
					}

				} else if (token.type == LaiLexer.TokenType.OpInferTypeAssignValue) {
					/*****************************************************************************
					 * * * * * * * * * *VARIABLE DECLARATION & DEFINITION INFER TYPE * * * * * * *
					 *****************************************************************************/

					// We don't do type inferencing in this section, because all we are doing is
					// finding variable and function declarations. What we will do is split "name :=
					// value" into "name : unknown; name = value"; and parse it, leaving it's type
					// as unknown for now. Type inferencing will be done when we parse statements.

					// We need to remember to parse := as = in the statements section.
					// Effectively this is what the compiler does:

					/*- source.lai:
					 * 
					 * 		foo := "foobar"
					 * 		//Other code
					 * 
					 * compiled (somewhere in program memory somehow):
					 * 
					 * 		<variables>                           [change to string!]
					 * 			foo	: unknowntype <----------------------------------------------]
					 * 		<functions>                                                          |
					 * 			...                                                              |
					 * 		<statements>                                                         |
					 * 			foo = "foobar" //statements parser will first infer the type of foo
					 * 
					 */

					// All we have to do is add a var with unknown type.
					contents.variables.addChild(new LaiVariable(new LaiIdentifier(ident.value),
							new LaiType(LaiType.Type.LaiTypeUnknown), ident_token_location));

					// Skip to the end of the line
					skipToEndOfLine();
				} else if (token.type == LaiLexer.TokenType.OpTypeDec) {
					/*****************************************************************************
					 * * * * * * * * * * * *VARIABLE DECLARATION EXPLICIT TYPE * * * * * * * * * *
					 *****************************************************************************/

					// Similarly to with :=, in the statement parse we will need to parse
					// "foo : type = value" as "foo : type; foo = value;".

					// We're currently on the ':' so lets move on to the type
					if (!safeNextToken())
						continue tokenLoop;

					// Double check this is a type
					if (!token.type.isPrimitiveType) {
						Main.error(filename, token.lineNumber, token.charNumber,
								"Expected a type, but got '" + token.type.name + " instead.");
						skipToEndOfLine();
						continue tokenLoop;
					}
					LaiType varType = LaiType.convertLexerTypeToLaiType(token);
					LaiVariable var = new LaiVariable(new LaiIdentifier(ident.value), varType, ident_token_location);
					contents.variables.addChild(var);

					// Check that the rest of the line is valid syntax.
					if (nextToken() == TokenContext.END) {
						Main.error(filename, token.lineNumber, token.charNumber, "Unexpected end of file.");
						continue tokenLoop;
					}

					// This should now either be a ';' or '=' operator.
					if (token.type == LaiLexer.TokenType.OpSemicolon
							|| token.type == LaiLexer.TokenType.OpAssignValue) {
						// Nice, we're good.
					} else {
						// Uhoh
						Main.error(filename, token.lineNumber, token.charNumber,
								"Expected either a ; or =, but found " + token.type.name + " instead.");
						skipToEndOfLine();
						continue tokenLoop;
					}

					// Skip to the end of the line
					skipToEndOfLine();
				} else {
					continue;
				}
			}
		}
	}

	private void parseStatements(String filename, ArrayList<LaiLexer.Token> tokens, LaiContents contents,
			LaiList<LaiVariable> params) {

		this.tokens = tokens;
		this.filename = filename;

		if (params == null) {
			// Prevent null exceptions
			params = new LaiList<LaiVariable>("LaiVariable");
		}

		tokenLoop: for (tokenCTR = 0; nextToken() != TokenContext.END;) {

			// Skip semicolons
			if (token.type == LaiLexer.TokenType.OpSemicolon) {
				continue;
			}

			if (token.type == LaiLexer.TokenType.Identifier) {
				// Check if this identifier has been created already
				LaiVariable localVar = null;
				for (LaiVariable v : contents.variables.list_children) {
					if (v.identifier.identifier.equals(((LaiLexer.Identifier) token).value)) {
						localVar = v;
						break;
					}
				}
				LaiVariable paramVar = null;
				for (LaiVariable v : params.list_children) {
					if (v.identifier.identifier.equals(((LaiLexer.Identifier) token).value)) {
						paramVar = v;
						break;
					}
				}
				LaiFunction function = null;
				for (LaiFunction f : contents.functions.list_children) {
					if (f.identifier.identifier.equals(((LaiLexer.Identifier) token).value)) {
						function = f;
						break;
					}
				}

				// If it hasn't been defined, error. It should have been defined in the
				// parseFunctionsAndVars()
				if (localVar == null && paramVar == null && function == null) {
					Main.error(filename, token.lineNumber, token.charNumber,
							"Unknown identifier '" + ((LaiLexer.Identifier) token).value + "'.");
					skipToEndOfLine();
					continue tokenLoop;
				}

				// Verify it was only defined once!
				int matchingIdentSum = ((localVar != null) ? 1 : 0) + ((paramVar != null) ? 1 : 0)
						+ ((function != null) ? 1 : 0);

				if (matchingIdentSum != 1) {
					Main.error(filename, token.lineNumber, token.charNumber,
							"There are multiple definitions of " + ((LaiLexer.Identifier) token).value);
					skipToEndOfLine();
					continue tokenLoop;
				}

				// If it's NOT a function, then it must be either a local variable or parameter
				// variable, which should be treated the same.
				if (localVar != null || paramVar != null) {

					LaiVariable var = null;
					if (localVar != null)
						var = localVar;
					if (paramVar != null)
						var = paramVar;

					if (var.identTokenPosition == tokenCTR) {
						// this is a variable definition, which means we most likely need to initialize
						// this variable with a SetVar statement, but we need to figure out what we are
						// setting it to as well as the type.

						// Find the operator for setting the value. It will be the next token.
						if (!safeNextToken())
							continue tokenLoop;

						if (token.type == LaiLexer.TokenType.OpInferTypeAssignValue) {
							// :=
							// The next token will be the beginning of the expression for the value of this
							// variable. Just parse it. Get it's return type and use that to set the
							// inferred type of this variable.

							// Parse the expression and create the statement.
							LaiExpression exp = parseExpression(filename, tokens, contents, params, tokenCTR, -1);

							if (var.type.type != AST.LaiType.Type.LaiTypeUnknown) {
								Main.error(filename, token.lineNumber, token.charNumber, "The variable '"
										+ var.identifier.identifier
										+ "' was already type inferred. Cannot type infer a variable multiple times.");
								skipToEndOfLine();
								continue tokenLoop;
							}
							// Because we found the type in the expression, we need 'infer' it and set the
							// variable's type now.
							var.type.type = exp.getReturnType().type;

							if (!(exp instanceof LaiExpressionUninit)) {
								contents.statements.addChild(new AST.LaiStatementSetVar(var, exp));
							}

							skipToEndOfLine();
							continue tokenLoop;

						} else if (token.type == LaiLexer.TokenType.OpTypeDec) {
							// :
							// The next token will be the variable type.
							if (!safeNextToken())
								continue tokenLoop;
							// We've already error checked that this is a type in the variable/function
							// declaration parser. So we can just move onto the next token, which should be
							// the = operator. If it's not then we can default initialize this.
							if (nextToken() == TokenContext.END) {
								Main.error(filename, token.lineNumber, token.charNumber, "Unexpected end of file.");
								skipToEndOfLine();
								continue tokenLoop;
							}

							if (token.type == LaiLexer.TokenType.OpAssignValue) {
								// The next token is the beginning of the expression to set this variable.
								if (!safeNextToken())
									continue tokenLoop;

								// Parse the expression and create the statement.
								LaiExpression exp = parseExpression(filename, tokens, contents, params, tokenCTR, -1);

								if (!(exp instanceof LaiExpressionUninit)) {
									// Check the types match.
									if (exp.returnType.type != var.type.type) {

										Main.error(filename, token.lineNumber, token.charNumber,
												"Expected a '" + var.type.type.name() + "', but got a '"
														+ exp.returnType.type.name() + "' instead.");
										skipToEndOfLine();
										continue tokenLoop;
									}
									contents.statements.addChild(new AST.LaiStatementSetVar(var, exp));
								}

								skipToEndOfLine();
								continue tokenLoop;

							} else if (token.type == LaiLexer.TokenType.OpSemicolon) {
								// Default initializer. We can do that easily here.

								LaiExpression literal;

								switch (var.type.type) {
								case LaiInteger:
									literal = new AST.LaiExpressionIntLiteral(0);
									break;
								case LaiString:
									literal = new AST.LaiExpressionStringLiteral(""); // Empty non-null string
									break;
								case LaiChar:
									literal = new AST.LaiExpressionCharLiteral('\u0000'); // Java code for null
																							// character,
																							// often displayed as a
																							// square
																							// or space in the terminal.
									break;
								default:
									Main.error(filename, token.lineNumber, token.charNumber,
											"Type does not support default initialization.");
									continue tokenLoop;
								}
								contents.statements.addChild(new AST.LaiStatementSetVar(var, literal));
								skipToEndOfLine();
								continue tokenLoop;
							}

						} else {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Expected : or := but got '" + token.type.name + "'.");
							skipToEndOfLine();
							continue tokenLoop;
						}

					} else if (var.identTokenPosition > tokenCTR) {
						Main.error(filename, token.lineNumber, token.charNumber, "Variable used before declared.");
						skipToEndOfLine();
						continue tokenLoop;
					} else {
						// This is a reference to the variable. Check what we are doing with it. We
						// should be setting it.
						if (!safeNextToken())
							continue tokenLoop;
						if (token.type != TokenType.OpAssignValue) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Expected = but got '" + token.type.name + "' instead.");
							skipToEndOfLine();
							continue tokenLoop;

						}

						// move forward to the beginning of the expression.
						if (!safeNextToken())
							continue tokenLoop;
						// okay we now just need to evaluate the expression it is set to and type check
						// it.
						// Parse the expression and create the statement.
						LaiExpression exp = parseExpression(filename, tokens, contents, params, tokenCTR, -1);

						if (exp.returnType.type != var.type.type) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Expected a '" + var.type.type.name() + "', but got a '"
											+ exp.returnType.type.name() + "' instead.");
							skipToEndOfLine();
							continue tokenLoop;
						}
						if (!(exp instanceof LaiExpressionUninit)) {
							contents.statements.addChild(new AST.LaiStatementSetVar(var, exp));
						}

						skipToEndOfLine();
						continue tokenLoop;
					}
				} else {
					// Ah so it must be a function.

					// Let's make sure this isn't the function definition. If it is, we have indexed
					// the function's body earlier, so we can safely skip it now. If not, then this
					// is a function call statement.

					if (function.identTokenPosition == tokenCTR) {
						// It's the definition.
						// The LaiFunction object contains the token contents of the function body, so
						// we can use the size of that to know how many tokens to skip. However, that is
						// starting from the { of the function dec. So let's find that first.

						while (token.type != TokenType.OpOpenBrace) {
							if (!safeNextToken())
								continue tokenLoop;
						}

						tokenCTR += function.bodyTokens.size() + 1;
						token = tokens.get(tokenCTR);
						// Below is a convenient way of double checking this.
//						Main.error(filename, token.lineNumber, token.charNumber,
//								"This is where the function ends. We added " + function.bodyTokens.size()
//										+ " tokens to tokenCTR.");
						continue tokenLoop;
					}

					// This is a function call statement. That means
					// that its return value doesn't matter because it is being used as a statement
					// like such: "foo();" rather than "var = foo();". All we have to do is parse
					// the params.

					// Check that there are ().
					if (!safeNextToken())
						continue tokenLoop;
					if (token.type != TokenType.OpOpenParenthesis) {
						Main.error(filename, token.lineNumber, token.charNumber,
								"Expected a '(' but got a '" + token.type.name + "' instead.");
						skipToEndOfLine();
						continue tokenLoop;
					}

					// Skip the (
					if (!safeNextToken())
						continue tokenLoop;
					// We need to find the parameter expressions, splitting by comma.
					int numParams = function.params.list_children.size();
					LaiList<LaiExpression> functionParams = new LaiList<LaiExpression>("LaiExpression");

					int currentExpressionStart = tokenCTR;
					int currentExpressionEnd;
					while (token.type != TokenType.OpCloseParenthesis) {

						// safe
						token = tokens.get(tokenCTR);
						if (token.type == TokenType.OpCloseParenthesis) {
							break;
						}

						if (!safeNextToken())
							continue tokenLoop;

						if (token.type == TokenType.OpComma || token.type == TokenType.OpCloseParenthesis) {
							currentExpressionEnd = tokenCTR;
							functionParams.addChild(parseExpression(filename, tokens, contents, params,
									currentExpressionStart, currentExpressionEnd));

							// Parse expression doesn't end in the right place. We need to correct the
							// tokenCTR.
							tokenCTR = currentExpressionEnd;

							// Now we can reset the expression markers.
							currentExpressionStart = tokenCTR + 1;
							currentExpressionEnd = -2; // I want an error if this value is used. I used -2 because -1 is
														// the way to make the parseExpression function just go until
														// reaching a ;.

						}
					}

					if (numParams != functionParams.list_children.size()) {
						Main.error(filename, token.lineNumber, token.charNumber, "Expected " + numParams
								+ " arguments, but found " + functionParams.list_children.size() + ".");
						skipToEndOfLine();
						continue tokenLoop;
					}
					// We need to type check the params.
					for (int argumentID = 0; argumentID < numParams; argumentID++) {

						LaiType.Type foundType = functionParams.list_children.get(argumentID).returnType.type;
						LaiType.Type expectedType = function.params.list_children.get(argumentID).type.type;

						if (foundType != expectedType) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Argument mismatch on argument (id=" + argumentID + ", where id starts at 0). Expected "
											+ expectedType.name() + ", but got " + foundType.name() + " instead.");
							skipToEndOfLine();
							continue tokenLoop;
						}
					}

					// Now that we have the params, we can create the function call statement.
					LaiStatementFunctionCall call = new LaiStatementFunctionCall(function, functionParams);
					contents.statements.addChild(call);
					skipToEndOfLine();
					continue tokenLoop;
				}
			} else {

			}

		}

		// Skip to the end of the line
		skipToEndOfLine();

	}

	/**
	 * Parse a single token expression. Either a variable, function call, or literal
	 * value.
	 * 
	 * @param filename
	 * @param expression
	 * @return
	 */
	private LaiExpression parseSingleTokenExpression(String filename, LaiLexer.Token expToken, LaiContents contents,
			LaiList<LaiVariable> parameters) {

		// ValuedToken = Literals and Identifier
		if (!(expToken instanceof LaiLexer.ValuedToken)) { // Check that the token is a literal
			Main.error(filename, expToken.lineNumber, expToken.charNumber,
					"This doesn't look right. I don't feel very good Mr Stark.");
			return null;
		}

		if (expToken instanceof LaiLexer.Identifier) {
			LaiVariable var = null;
			for (LaiVariable v : parameters.list_children) {
				if (v.identifier.identifier.equals(((LaiLexer.Identifier) expToken).value)) {
					var = v;
					break;
				}
			}
			if (var == null) {
				for (LaiVariable v : contents.variables.list_children) {
					if (v.identifier.identifier.equals(((LaiLexer.Identifier) expToken).value)) {
						var = v;
						break;
					}
				}
			}
			if (var == null) {
				Main.error(filename, expToken.lineNumber, expToken.charNumber,
						"Can not find variable '" + ((LaiLexer.Identifier) expToken).value + "'.");
				return null;
			}

			// We have now found the variable that this identifier references.
			if (var.type.type == Type.LaiTypeUnknown) {
				Main.error(filename, expToken.lineNumber, expToken.charNumber,
						"Type inferences can not rely on variables of unknown type.");
				return null;
			}

			return new LaiExpressionVariable(var);

		} else {

			LaiExpression exp = null;
			switch (expToken.type) {
			case IntegerLiteral:
				exp = new LaiExpressionIntLiteral(((LaiLexer.IntegerLiteral) expToken).value);
				break;
			case StringLiteral:
				exp = new LaiExpressionStringLiteral(((LaiLexer.StringLiteral) expToken).value);
				break;
			case CharLiteral:
				exp = new LaiExpressionCharLiteral(((LaiLexer.CharLiteral) expToken).value);
				break;
			default:
				Main.error(filename, expToken.lineNumber, expToken.charNumber, "Literal not supported?");
				break;
			}

			return exp;
		}
	}

	/**
	 * Parse an expression until first semicolon is found.
	 * 
	 * @param filename
	 * @param tokens
	 * @param contents
	 * @param params
	 * @param expressionTokenLocationStart
	 * @return
	 */
	private LaiExpression parseExpression(String filename, ArrayList<LaiLexer.Token> tokens, LaiContents contents,
			LaiList<LaiVariable> params, int expressionTokenLocationStart, int expressionTokenLocationEnd) {
		this.tokenCTR = expressionTokenLocationStart;
		this.tokens = tokens;
		this.filename = filename;

		token = tokens.get(tokenCTR);

		if (params == null) {
			// Prevent null exceptions
			params = new LaiList<LaiVariable>("LaiVariable");
		}

		// Find the length of the expression.
		int expressionLength = 1;
		if (expressionTokenLocationEnd == -1) {
			while (nextToken() == TokenContext.NORMAL) {
				expressionLength++;
			}

			// Go back to the beginning of the expression
			tokenCTR -= expressionLength;
		} else {
			expressionLength = expressionTokenLocationEnd - expressionTokenLocationStart;
		}
		token = tokens.get(tokenCTR);

		// If it is only one thing then we know we just need to parse this as either a
		// function call, variable reference, or literal.
		if (expressionLength == 1) {
			if (token.type == LaiLexer.TokenType.UnitializeValue) {
				return new AST.LaiExpressionUninit();
			}
			return parseSingleTokenExpression(filename, token, contents, params);
		} else {
			// Implementation of Shunting Yard Algorithm
			ArrayList<LaiLexer.TokenType> operators = new ArrayList<LaiLexer.TokenType>();
			ArrayList<Integer> precedence = new ArrayList<Integer>();

			operators.add(TokenType.OpMathPlus);
			precedence.add(0);

			operators.add(TokenType.OpMathMinus);
			precedence.add(0);

			operators.add(TokenType.OpMathDivide);
			precedence.add(1);

			operators.add(TokenType.OpMathMultiply);
			precedence.add(1);

			operators.add(TokenType.OpPow);
			precedence.add(2);

			Stack<Object> tokenPostfix = new Stack<Object>();

			Stack<LaiLexer.Token> operatorStack = new Stack<LaiLexer.Token>();
			Stack<LaiExpression> treeStack = new Stack<LaiExpression>();

			for (int i = expressionTokenLocationStart; i < expressionTokenLocationStart + expressionLength; i++) {
				LaiLexer.Token t = tokens.get(i);
				int prec = -1;
				int topStackPrec = -1;

				if (operatorStack.size() > 0) {
					if (operatorStack.peek().type != TokenType.OpOpenParenthesis)
						topStackPrec = precedence.get(operators.indexOf(operatorStack.peek().type));
				}

				if (operators.contains(t.type)) {
					prec = precedence.get(operators.indexOf(t.type));
				}

				if (t instanceof LaiLexer.ValuedToken) {// If the incoming symbol is an operand,
														// print it.
					tokenPostfix.push(t);
					treeStack.push(this.parseSingleTokenExpression(filename, t, contents, params));

					// Add this as a leaf node to the expression stack.
				} else if (t.type == TokenType.OpOpenParenthesis) {// If the incoming symbol is a left
																	// parenthesis, push it on the stack.
					operatorStack.push(t);
				} else if (t.type == TokenType.OpOpenParenthesis) {// If the incoming symbol is a right
																	// parenthesis: discard the right
																	// parenthesis, pop and print the stack
																	// symbols until you see a left parenthesis.
																	// Pop the left parenthesis and discard it.

					// Pop and Print the stack symbols until ( encountered.
					while (operatorStack.get(operatorStack.size() - 1).type != TokenType.OpOpenParenthesis) {

						LaiLexer.Token operatorToken = operatorStack.peek();
						LaiExpression expB = treeStack.pop();
						LaiExpression expA = treeStack.pop();
						LaiExpressionBasicMath expOp = null;

						switch (operatorToken.type) {

						case OpMathPlus:
							expOp = new LaiExpressionAddition(expA, expB);
							break;
						case OpMathMinus:
							expOp = new LaiExpressionMinus(expA, expB);
							break;
						case OpMathMultiply:
							expOp = new LaiExpressionMultiply(expA, expB);
							break;
						case OpMathDivide:
							expOp = new LaiExpressionDivide(expA, expB);
							break;
						default:
							System.err.println("unsupported op");
							break;
						}
						treeStack.push(expOp);

						tokenPostfix.push(operatorStack.pop());
					}
					// Pop and discard (.
					operatorStack.pop();

				} else if (operators.contains(t.type) && (operatorStack.size() == 0
						|| operatorStack.get(operatorStack.size() - 1).type == TokenType.OpOpenParenthesis)) {
					// If the incoming symbol is an operator and the stack is empty or contains a
					// left parenthesis on top, push the incoming operator onto the stack.
					operatorStack.push(t);

				} else if (operators.contains(t.type)
						&& (prec > topStackPrec || (prec == topStackPrec && t.type == TokenType.OpPow))) {
					// If the incoming symbol is an operator and has either higher precedence than
					// the operator on the top of the stack, or has the same precedence as the
					// operator on the top of the stack and is right associative -- push it on the
					// stack.
					operatorStack.push(t);
				} else if (operators.contains(t.type)
						&& (prec < topStackPrec || (prec == topStackPrec && t.type != TokenType.OpPow))) {
					// If the incoming symbol is an operator and has either lower precedence than
					// the operator on the top of the stack, or has the same precedence as the
					// operator on the top of the stack and is left associative -- continue to pop
					// the stack until this is not true. Then, push the incoming operator.
					while (operators.contains(t.type)
							&& (prec < topStackPrec || (prec == topStackPrec && t.type != TokenType.OpPow))) {
						if (operators.contains((LaiLexer.TokenType) (operatorStack.peek()).type)) {
							LaiLexer.Token operatorToken = operatorStack.peek();
							LaiExpression expB = treeStack.pop();
							LaiExpression expA = treeStack.pop();
							LaiExpressionBasicMath expOp = null;

							switch (operatorToken.type) {

							case OpMathPlus:
								expOp = new LaiExpressionAddition(expA, expB);
								break;
							case OpMathMinus:
								expOp = new LaiExpressionMinus(expA, expB);
								break;
							case OpMathMultiply:
								expOp = new LaiExpressionMultiply(expA, expB);
								break;
							case OpMathDivide:
								expOp = new LaiExpressionDivide(expA, expB);
								break;
							default:
								System.err.println("unsupported op");
								break;
							}
							treeStack.push(expOp);

							tokenPostfix.push(operatorStack.pop());
						} else
							operatorStack.pop();

						// Reset the topStackPrec so that we dont c r a s h
						if (operatorStack.size() > 0) {
							if (operatorStack.peek().type != TokenType.OpOpenParenthesis)
								topStackPrec = precedence.get(operators.indexOf(operatorStack.peek().type));
						} else {
							topStackPrec = -1;
						}
					}
					operatorStack.push(t);
				}
			}
			// At the end of the expression, pop and print all operators on the stack. (No
			// parentheses should remain.)
			while (operatorStack.size() > 0) {
				if (operatorStack.peek().type != TokenType.OpOpenParenthesis) {
					LaiLexer.Token operatorToken = operatorStack.peek();
					LaiExpression expB = treeStack.pop();
					LaiExpression expA = treeStack.pop();
					LaiExpressionBasicMath expOp = null;

					switch (operatorToken.type) {

					case OpMathPlus:
						expOp = new LaiExpressionAddition(expA, expB);
						break;
					case OpMathMinus:
						expOp = new LaiExpressionMinus(expA, expB);
						break;
					case OpMathMultiply:
						expOp = new LaiExpressionMultiply(expA, expB);
						break;
					case OpMathDivide:
						expOp = new LaiExpressionDivide(expA, expB);
						break;
					default:
						System.err.println("unsupported op");
						break;
					}
					treeStack.push(expOp);
					tokenPostfix.push(operatorStack.pop());
				} else
					operatorStack.pop();
			}

			/*
			 * System.out.print("ALG OUTPUT: "); for (Object o : tokenPostfix) { if (o
			 * instanceof LaiLexer.ValuedToken) { System.out.print(((LaiLexer.ValuedToken)
			 * o).value); } else if (o instanceof LaiLexer.Token) {
			 * System.out.print(((LaiLexer.Token) o).type.name); } else {
			 * System.out.print("that's really quite unfortunate"); } System.out.print(" ");
			 * } System.out.print("\n"); // System.out.println("TREE SIZE: " +
			 * treeStack.size());
			 */
			return treeStack.get(0);
		}

		// return new AST.LaiExpressionStringLiteral("idfk");

	}

	private LaiContents parseContent(String filename, ArrayList<LaiLexer.Token> tokens) {
		this.tokens = tokens;
		this.filename = filename;

		LaiContents contents = new LaiContents();

		this.parseVariablesAndFunctionsToContent(filename, tokens, contents);
		this.parseStatements(filename, tokens, contents, null);

		return contents;
	}

	public void assembleFile(String filename, ArrayList<LaiLexer.Token> tokens) {

		this.tokens = tokens;
		this.filename = filename;

		LaiFile file = new LaiFile(filename);
		files.add(file);

		file.addChild(this.parseContent(filename, tokens));

	}
}
