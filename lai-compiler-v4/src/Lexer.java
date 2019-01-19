import java.util.ArrayList;

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
			// System.out.println("TOKEN: " + Lai.getDebugString(t));
		}
	}

	private ArrayList<LexerFile> files;

	public Lexer() {
		files = new ArrayList<LexerFile>();
	}

	private String getCarrotPointer(int offset) {
		String s = "";
		for (int i = 0; i < offset; ++i) {
			s += " ";
		}
		s += "^";
		return s;
	}

	public void tokenError(String filename, int lineNumber, int charNumber, String message, String offendingLine) {
		System.out.println("Error in file '" + filename + "(" + (lineNumber + 1) + ")" + "':");
		System.out.println("\t" + offendingLine);
		System.out.println("\t" + getCarrotPointer(charNumber));
		System.out.println(message);
		System.out.println("");
	}

	private static final String numbers = "0123456789";
	private static final String letters = "abcdefghjiklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
	private static final String alphanumeric = numbers + letters;

	private static final String whitespace = " \n\r\t";

	private static final String operators = "!@#$%^&*(){}[]:;<>,./\\=";

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

		// Standard Line by Line, Letter by Letter loop.
		for (int lineNumber = 0; lineNumber < fileContents.size(); ++lineNumber) {
			String line = fileContents.get(lineNumber);
			letterLoop: for (int charNumber = 0; charNumber < line.length(); ++charNumber) {
				char op = line.charAt(charNumber);

				// System.out.println("parsing (" + lineNumber + ", " + charNumber + ")=" + op);

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

					// System.out.println("Found string.");

					String literalValue = "";
					do {
						charNumber++;
						if (charNumber >= line.length()) {
							this.tokenError(filename, lineNumber, charNumber - 1,
									"Unexpected end of line while parsing string literal.", line);
							break;
						}
						op = line.charAt(charNumber);
						// System.out.println("string op = '" + op + "'");
						if (op != '\"')
							literalValue += op;
					} while (op != '"');
					lexerFile.addToken(new LaiLexer.StringLiteral(lineNumber, charNumber, literalValue));
					// Op ends on the second ".
					continue;

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
					continue;

				} else if (isLetter(op)) {

					// Find entire word to check if this is a keyword.
					char lookAheadChar = op;
					int lookAheadIndex = charNumber;
					String word = "";
					while (!isWhitespace(lookAheadChar)) {
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
					opLoop: do {
						// Find possible operators
						possibleOps.clear();
						for (LaiLexer.TokenType t : LaiLexer.OPERATOR_TOKENS) {
							if (t.name.length() > offset) {
								// String.substring(i, e). first index inclusive, second index exclusive.
								String check = t.name.substring(0, offset + 1);
//								System.out.println(
//										"Checking: '" + check + "' against '" + opSoFar + "' from '" + t.name + "'.");
								if (check.equals(opSoFar)) {
									// System.out.println("Found match: '" + t.name + "'.");
									possibleOps.add(t);
								}

							}
						}

						// dbg print possible ops
						for (int i = 0; i < possibleOps.size(); ++i) {
							// System.out.println(possibleOps.get(i).name);
						}

						offset++;
						charNumber++;

						// Double check we haven't reached end of line.
						if (charNumber >= line.length()) {
							break;
						}

						op = line.charAt(charNumber);
						// Skip whitespace.
						while (isWhitespace(op)) {
							// offset++; We don't increment the offset because the offset is used for
							// checking against the definition of the operator in the Lai.TokenType enum.
							charNumber++;

							// Double check we haven't reached end of line. If we have, this is the end of
							// the op.
							if (charNumber >= line.length()) {
								break opLoop;
							}
							op = line.charAt(charNumber);
						}
						if (!isOperator(op)) {
							// End of Operator!
							charNumber--;
							break;
						}

						opSoFar += op;
					} while (possibleOps.size() > 1);
					if (possibleOps.size() == 0) {
						tokenError(filename, lineNumber, charNumber, "Could not parse operator.", line);
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
						this.tokenError(filename, lineNumber, charNumber,
								"Unexpected end of line while parsing operator.", line);
					}
					// Now that we've dealt with edge cases, we can just add the operator at
					// possibleOps[0].
					lexerFile.addToken(new LaiLexer.Token(lineNumber, charNumber, possibleOps.get(0)));
					continue;
				} else {
					tokenError(filename, lineNumber, charNumber, "Could not parse.", line);
				}
			}
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
