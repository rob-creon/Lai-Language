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

		// Pass to lexer
		Lexer lexer = new Lexer();

		// Pass each file individually
		for (int i = 0; i < filenames.size(); ++i) {
			String filename = filenames.get(i);
			ArrayList<String> fileContents = filesContent.get(i);
			lexer.parseFile(filename, fileContents);
		}

		// Print the tokens for debugging
		System.out.println("\nTokens Generated: ");
		ArrayList<LaiLexer.Token> tokens = lexer.getFileTokens(filenames.get(0));
		for (int i = 0; i < tokens.size(); ++i) {

			LaiLexer.Token t = tokens.get(i);
		
			System.out.println(LaiLexer.getDebugString(t));
		}
	}
}