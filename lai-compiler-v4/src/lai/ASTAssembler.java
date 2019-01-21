package lai;

import java.util.ArrayList;

import lai.AST.*;

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

	private LaiContents parseContent(String filename, ArrayList<LaiLexer.Token> tokens) {
		this.tokens = tokens;
		this.filename = filename;

		LaiContents contents = new LaiContents();

		// First pass we are searching for functions and variables.
		tokenLoop: for (tokenCTR = 0; nextToken() != TokenContext.END;) {

			if (token.type == LaiLexer.TokenType.OpSemicolon) {
				continue;
			}

			if (token.type == LaiLexer.TokenType.Identifier) {
				// If it is an identifier, figure out what it's doing. Is it declaring a
				// variable or a function? Or is it referencing an existing var or function?
				LaiLexer.Identifier ident = (LaiLexer.Identifier) token;

				if (!safeNextToken())
					continue;

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
								parameters.addChild(new AST.LaiVariable(paramIdent, paramType));

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
								funcReturnType);
						contents.addChild(function);

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
						// This must be a function call.
						// TODO
					}

				} else {
					Main.error(filename, token.lineNumber, token.charNumber, "Identifier can not stand alone.");
					continue;
				}
			}
		}

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
