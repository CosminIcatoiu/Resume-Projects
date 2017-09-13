import java.io.*;

public class ComparingNode extends OperatorNode {

	public ComparingNode(Node left, Node right) {
		super(left, right);
	}

	public Boolean accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
