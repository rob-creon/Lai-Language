import java.util.ArrayList;

public class ASTAssembler {
	
	private class ASTFile {
		public String filename;
		private LaiAST.Node root;
		
		public ASTFile(String filename) {
			this.filename = filename;
			root = new LaiAST.Node();
		}
	}
	
	private ArrayList<ASTFile> files;
	
	public ASTAssembler() {
		files = new ArrayList<ASTFile>();
	}
	
	public void parseFile(String filename, ArrayList<LaiLexer.Token> tokens) {
		
	}
}
