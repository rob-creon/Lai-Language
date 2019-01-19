import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;

class Main {

	public static void main(String[] args) {

		/**************************************/
		/* Parse Command Line Arguments */
		/**************************************/

		// Verify that a file has been provided as a command line arg.
		// Other args are a WIP, for now we will just focus on compiling.

		if (args.length == 0) {
			System.out.println("You must specify a file or argument.");
			System.exit(0);
		}

		// Sort the arguments into flags and files.
		ArrayList<String> flags = new ArrayList<String>();
		ArrayList<String> filenames = new ArrayList<String>();

		// Flags are anything that begin with a '-'.
		for (String s : args) {
			if (s.charAt(0) == '-') {
				flags.add(s);
			} else {
				filenames.add(s);
			}
		}

		/*************************/
		/* Load All Source Files */
		/*************************/
		ArrayList<ArrayList<String>> filesContent = new ArrayList<>();
		for (String filename : filenames) {
			ArrayList<String> list = new ArrayList<String>();
			System.out.println("Loading file: " + filename + "...");
			try {
				Scanner s = new Scanner(new File(filename));
				while (s.hasNext()) {
					list.add(s.nextLine());
				}
				s.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}

			filesContent.add(list);
		}

		/******************************/
		/* Tokenize */
		/******************************/
		
		System.out.println("Tokenizing...");
		
		Lexer lexer = new Lexer();

		// Pass each file individually
		for (int i = 0; i < filenames.size(); ++i) {
			String filename = filenames.get(i);
			ArrayList<String> fileContents = filesContent.get(i);
			lexer.parseFile(filename, fileContents);
		}

		System.out.println("Tokenized.");
		// Print the tokens for debugging
		
		ArrayList<String> fileContents = filesContent.get(0);
		ArrayList<LaiLexer.Token> tokens = lexer.getFileTokens(filenames.get(0));

		if (flags.contains("-token")) {
			System.out.println("\nTokens: ");
			int lineNum = 0;
			int tokenIndex = 0;
			int futureindent = 0;
			int currentindent = 0;
			while (lineNum < fileContents.size()) {
				String line = "";
				while (tokens.get(tokenIndex).lineNumber == lineNum) {

					line += "[" + LaiLexer.getDebugString(tokens.get(tokenIndex)) + "]";

					if (LaiLexer.getConciseDebugString(tokens.get(tokenIndex)).equals("{")) {
						futureindent++;
					}
					if (LaiLexer.getConciseDebugString(tokens.get(tokenIndex)).equals("}")) {
						currentindent--;
						futureindent--;
					}
					tokenIndex++;
					if (tokenIndex >= tokens.size() - 1) {
						break;
					}
				}
				System.out.print("\n" + (lineNum + 1) + ":" + getIndents(currentindent) + line);
				currentindent = futureindent;
				// futureindent = 0;
				lineNum++;
			}
		}

		/*********************************/
		/* Assemble Abstract Syntax Tree */
		/*********************************/
		ASTAssembler ast = new ASTAssembler();
		for (int i = 0; i < filenames.size(); ++i) {
			String filename = filenames.get(i);
			ArrayList<LaiLexer.Token> fileTokens = lexer.getFileTokens(filename);
			ast.parseFile(filename, fileTokens);
		}

	}

	private static String getIndents(int n) {
		String out = "";
		for (int i = 0; i < n; i++) {
			out += "\t";
		}
		return out;
	}
}