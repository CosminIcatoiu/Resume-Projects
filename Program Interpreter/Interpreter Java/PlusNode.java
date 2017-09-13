import java.io.*;

public class PlusNode extends OperatorNode {

	PlusNode(Node left, Node right) {
		super(left, right);
	}

	public Integer accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
