package lai;

import java.util.ArrayList;

import lai.LaiLexer.Token;

public class Lexer {

	private class LexerFile {

		private ArrayList<LaiLexer.Token> tokens;
		public String filename;

		public LexerFile(String filename) {
			this.filename = filename;
			tokens = new ArrayList<LaiLexer.Token>();
		}

		public void addToken(LaiLexer.Token t) {
			tokens.add(t);
		}
	}

	private ArrayList<LexerFile> files;

	public Lexer() {
		files = new ArrayList<LexerFile>();
		ArrayList<LaiLexer.TokenType> types = new ArrayList<LaiLexer.TokenType>();
		for (LaiLexer.TokenType t : LaiLexer.TokenType.values()) {
			types.add(t); // inits the token type so it is added to the proper indexing lists
			// System.out.println("Initing type: " + t.name);
		}
	}

	private static final String numbers = "0123456789";
	private static final String letters = "abcdefghjiklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
	private static final String alphanumeric = numbers + letters;

	private static final String whitespace = " \n\r\t";

	private static final String operators = "+-?!@#$%^&*(){}[]:;<>,./\\=";

	public static boolean isOperator(char c) {
		return operators.indexOf(c) != -1;
	}

	public static boolean isWhitespace(char c) {
		return whitespace.indexOf(c) != -1;
	}

	public static boolean isNumber(char c) {
		return numbers.indexOf(c) != -1;
	}

	public static boolean isLetter(char c) {
		return letters.indexOf(c) != -1;
	}

	public static boolean isAlphanumeric(char c) {
		return alphanumeric.indexOf(c) != -1;
	}

	public static boolean isAlphanumeric(String s) {
		for (int i = 0; i < s.length(); ++i) {
			if (!isAlphanumeric(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public void parseFile(String filename, ArrayList<String> fileContents) {
		LexerFile lexerFile = new LexerFile(filename);
		files.add(lexerFile);

		// add a buffer to beginning of file to prevent any off by 1 errors with parsing
		// wrong in the ast
		lexerFile.addToken(new LaiLexer.Token(0, 0, LaiLexer.TokenType.OpSemicolon));
		lexerFile.addToken(new LaiLexer.Token(0, 0, LaiLexer.TokenType.OpSemicolon));
		lexerFile.addToken(new LaiLexer.Token(0, 0, LaiLexer.TokenType.OpSemicolon));

		// Standard Line by Line, Letter by Letter loop.
		for (int lineNumber = 0; lineNumber < fileContents.size(); ++lineNumber) {
			String line = fileContents.get(lineNumber);
			letterLoop: for (int charNumber = 0; charNumber < line.length(); ++charNumber) {
				char op = line.charAt(charNumber);

				// Ignore Whitespace
				if (isWhitespace(op)) {
					continue letterLoop;
				}

				if (op == '/') {
					if (line.length() > charNumber + 1) {
						if (line.charAt(charNumber + 1) == '/') {
							// Line comment
							charNumber = line.length() - 1;
							continue letterLoop;
						}
					}
				}

				// Check if it is the beginning of a string literal
				if (op == '"') {

					String literalValue = "";
					do {
						charNumber++;
						if (charNumber >= line.length()) {
							Main.error(filename, lineNumber, charNumber - 1,
									"Unexpected end of line while parsing string literal.", line);
							break;
						}
						op = line.charAt(charNumber);
						if (op != '\"')
							literalValue += op;
					} while (op != '"');
					lexerFile.addToken(new LaiLexer.StringLiteral(lineNumber, charNumber, literalValue));
					// Op ends on the second ".
					continue;

				} else if (op == '\'') {
					// Char literal
					if (charNumber + 1 >= line.length()) {
						Main.error(filename, lineNumber, charNumber - 1,
								"Unexpected end of line while parsing char literal.", line);
						break;
					}
					charNumber++;
					op = line.charAt(charNumber);
					if (op == '\'') {
						Main.error(filename, lineNumber, charNumber - 1, "Can not init empty char!", line);
						break;
					}
					lexerFile.addToken(new LaiLexer.CharLiteral(lineNumber, charNumber, op));
					charNumber++;
					op = line.charAt(charNumber);
					if (op != '\'') {
						Main.error(filename, lineNumber, charNumber - 1, "Expected ' but got " + op, line);
						break;
					}
				} else if (isNumber(op)) {

					// If it is a number, then this is likely a digit.
					String literalValue = "";
					while (isNumber(op)) {
						literalValue += op;

						// Double check we haven't reached end of line.
						if (charNumber >= line.length() - 1) {
							break;
						}
						op = line.charAt(++charNumber);
					}

					int value = Integer.parseInt(literalValue);
					lexerFile.addToken(new LaiLexer.IntegerLiteral(lineNumber, charNumber, value));
					
					if(!isNumber(op)) {
						charNumber--;
					}
					continue;

				} else if (isLetter(op)) {

					// Find entire word to check if this is a keyword.
					char lookAheadChar = op;
					int lookAheadIndex = charNumber;
					String word = "";
					while (isAlphanumeric(lookAheadChar)) {
						word += lookAheadChar;

						lookAheadIndex++;
						// Check we haven't reached end of line
						if (lookAheadIndex >= line.length()) {
							break;
						}

						lookAheadChar = line.charAt(lookAheadIndex);
					}

					LaiLexer.TokenType typeCheck = LaiLexer.getTokenDirect(word);
					if (typeCheck != LaiLexer.TokenType.UnknownToken) {
						// If there is a directly tokenizable string, we can easily make the token now.
						lexerFile.addToken(new LaiLexer.Token(lineNumber, charNumber, typeCheck));
						charNumber = lookAheadIndex - 1;
						continue;
					} else {
						// If there is an unknown type, then this is an identifier.
						lookAheadChar = op; // We can reuse these variables.
						lookAheadIndex = charNumber;
						word = "";
						while (isAlphanumeric(lookAheadChar)) {
							word += lookAheadChar;
							lookAheadIndex++;
							// Check we haven't reached the end of line
							if (lookAheadIndex >= line.length()) {
								break;
							}
							lookAheadChar = line.charAt(lookAheadIndex);
						}

						lexerFile.addToken(new LaiLexer.Identifier(lineNumber, charNumber, word));
						charNumber = lookAheadIndex - 1;
						continue;
					}
				} else if (isOperator(op)) {
					// This is a non alphanumeric char.
					// We are now looking for special operators.
					// Basic idea is to keep going until we find an alphanumeric char, because then
					// we will have reached the end of the operator.

					// The problem is that we might have found a block of operators,
					// so we need to find the first one. This is complicated
					// because of things like = and ==, or + and ++.

					// Let's go char by char, and just eliminate operators char by char until there
					// is only one possible one.

					ArrayList<LaiLexer.TokenType> possibleOps = new ArrayList<LaiLexer.TokenType>();
					String opSoFar = "" + op;
					int offset = 0;
					ArrayList<LaiLexer.TokenType> oldPossibleOps;
					do {
						// Find possible operators
						oldPossibleOps = new ArrayList<>(possibleOps);
						possibleOps.clear();
						for (LaiLexer.TokenType t : LaiLexer.OPERATOR_TOKENS) {
							if (t.name.length() > offset) {
								// String.substring(i, e). first index inclusive, second index exclusive.
								String check = t.name.substring(0, offset + 1);

								if (check.equals(opSoFar)) {
									possibleOps.add(t);
								}
							}
						}
						if (possibleOps.size() == 0) {
							if (oldPossibleOps.size() == 1) {
								possibleOps = oldPossibleOps;
								opSoFar = opSoFar.substring(0, opSoFar.length() - 1);// remove last char
								charNumber--;
								// backup program ctr
								break;
							} else {
								Main.error(filename, lineNumber, charNumber,
										"Can't identify operator '" + opSoFar + "'.", line);
							}
						}
						offset++;
						charNumber++;

						// Double check we haven't reached end of line.
						if (charNumber >= line.length()) {
							break;
						}

						op = line.charAt(charNumber);

						if (!isOperator(op)) {
							// End of Operator!
							charNumber--;
							break;
						}

						opSoFar += op;
					} while (true);
					// while (possibleOps.size() > 1);
					if (possibleOps.size() == 0) {
						Main.error(filename, lineNumber, charNumber, "Could not parse operator.", line);
						continue;
					}
					if (possibleOps.size() > 1) {
						// If this happens, we broke out of the dowhile loop above, probably because of
						// a \n. Let's find the exact match operator, and if it doesn't exist, we can
						// throw an
						// error.
						for (LaiLexer.TokenType t : LaiLexer.OPERATOR_TOKENS) {
							if (t.name.equals(opSoFar)) {
								lexerFile.addToken(new LaiLexer.Token(lineNumber, charNumber, t));
								continue letterLoop;
							}
						}
						// If we reach here without continuing, there was no exact match.
						Main.error(filename, lineNumber, charNumber, "Unexpected end of line while parsing operator.",
								line);
					}
					// Now that we've dealt with edge cases, we can just add the operator at
					// possibleOps[0].

					// Double check they are equal. if they are not then we had a partial match.
					if (!possibleOps.get(0).name.equals(opSoFar)) {
						Main.error(filename, lineNumber, charNumber, "Incomplete or malformed op '" + opSoFar
								+ "'. Looks similar to: '" + possibleOps.get(0).name + "'.", line);
						continue;
					}

					lexerFile.addToken(new LaiLexer.Token(lineNumber, charNumber, possibleOps.get(0)));
					continue;
				} else {
					Main.error(filename, lineNumber, charNumber, "Could not parse char '" + op + "'.", line);
				}
			}
			// Insert a semicolon at the end of every line
			lexerFile.addToken(new LaiLexer.Token(lineNumber, line.length(), LaiLexer.TokenType.OpSemicolon));
		}

		// Syntax check tokens for matching () and {}.
		int openBraceCount = 0;
		int closeBraceCount = 0;

		int openParCount = 0;
		int closeParCount = 0;

		for (LaiLexer.Token t : lexerFile.tokens) {

			switch (t.type) {

			case OpOpenBrace:
				openBraceCount++;
				break;

			case OpCloseBrace:
				closeBraceCount++;
				break;

			case OpOpenParenthesis:
				openParCount++;
				break;

			case OpCloseParenthesis:
				closeParCount++;
				break;
			default:

			}

		}

		if (openBraceCount != closeBraceCount) {
			String msg;
			if (openBraceCount > closeBraceCount) {
				msg = (openBraceCount - closeBraceCount) + " too many open braces";
			} else {
				msg = (closeBraceCount - openBraceCount) + " too many close braces";
			}
			Main.error(filename, fileContents.size() - 1, fileContents.get(fileContents.size() - 1).length() - 1,
					"There are " + msg + " in the file.");
		}

		if (openParCount != closeParCount) {
			String msg;
			if (openParCount > closeParCount) {
				msg = (openParCount - closeParCount) + " too many open parenthesis";
			} else {
				msg = (closeParCount - openParCount) + " too many close parenthesis";
			}
			Main.error(filename, fileContents.size() - 1, fileContents.get(fileContents.size() - 1).length() - 1,
					"There are " + msg + " in the file.");
		}
	}

	public ArrayList<LaiLexer.Token> getFileTokens(String filename) {
		for (int i = 0; i < files.size(); ++i) {
			if (files.get(i).filename.contentEquals(filename)) {
				return files.get(i).tokens;
			}
		}
		System.out.println("File '" + filename + "' doesn't exist or hasn't been lexed.");
		return new ArrayList<LaiLexer.Token>();
	}
}
