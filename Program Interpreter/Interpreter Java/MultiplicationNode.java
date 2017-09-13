import java.io.*;

public class MultiplicationNode extends OperatorNode {

	public MultiplicationNode(Node left, Node right) {
		super(left, right);
	}

	public Integer accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
