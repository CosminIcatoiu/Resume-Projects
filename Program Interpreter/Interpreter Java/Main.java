import java.util.*;
import java.io.*;

public class Main {

	public static void main(String[] args) throws Exception {

		Scanner in = new Scanner(new FileReader(args[0]));

		FileWriter w = new FileWriter(args[1]);
		
		
		String s = in.nextLine();
		
		while(in.hasNextLine()){
			s += in.nextLine();
		}

		ArrayList<String> list = ASTCreator.parseExpression(s);
		
		Node root = ASTCreator.createAST(list);
		
		
		ASTEvaluator eval = new ASTEvaluator();

		try {
			Object result = root.accept(eval, w);
			if (eval.checkReturn() == 1) {
				if(eval.checkAssert() == 1)
					w.write("Assert failed");
				else
					w.write(((Integer) result) + "");
			} else {
				w.write("Missing return");
			}
		} catch (Exception e) {
		} finally {
			in.close();
			w.close();
		}

	}
}
