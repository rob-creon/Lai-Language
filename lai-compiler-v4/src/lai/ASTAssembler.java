package lai;

import java.util.ArrayList;

import lai.AST.*;

public class ASTAssembler {

	public ArrayList<LaiFile> files;

	private int tokenCTR = 0;
	private LaiLexer.Token token;

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

	public void assembleFile(String filename, ArrayList<LaiLexer.Token> tokens) {

		this.tokens = tokens;

		LaiFile file = new LaiFile(filename);
		files.add(file);

		LaiContents contents = new LaiContents();
		file.addChild(contents);

		// First pass we are searching for functions and variables.
		for (tokenCTR = 0; nextToken() != TokenContext.END;) {

			if (token.type == LaiLexer.TokenType.OpSemicolon) {
				continue;
			}

			if (token.type == LaiLexer.TokenType.Identifier) {
				// If it is an identifier, figure out what it's doing. Is it declaring a
				// variable or a function? Or is it referencing an existing var or function?
				LaiLexer.Identifier ident = (LaiLexer.Identifier) token;

				// ERROR: No identifier can stand on its own.
				if (nextToken() != TokenContext.NORMAL) {
					// End of line/file too early.
					Main.error(filename, token.lineNumber, token.charNumber,
							"Unexpected end of line, semicolon, or file.");
				}

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

					if (lookAheadToken.type == LaiLexer.TokenType.OpOpenBrace) {
						// Yep, this is a function definition.

						// Parse parameters
						LaiList<LaiVariable> parameters = new LaiList<LaiVariable>("LaiVariable");

						// First check if there are parameters
						if (nextToken() != TokenContext.NORMAL) {
							Main.error(filename, token.lineNumber, token.charNumber,
									"Unexpected token when parsing function declaration.");
						}

//						contents.addChild(new LaiFunction(new LaiIdentifier(ident.value),
//								new LaiType(LaiType.Type.LaiTypeUnknown)));
					} else {
						// This must be a function call.
					}

				}
			}
		}
	}
}
