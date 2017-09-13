import java.io.*;

public class CheckEqualNode extends OperatorNode {

	public CheckEqualNode(Node left, Node right) {
		super(left, right);
	}

	public Boolean accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
