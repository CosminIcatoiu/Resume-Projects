import java.io.*;

public class AssignmentNode extends OperatorNode {

	public AssignmentNode(Node left, Node right) {
		super(left, right);
	}

	public Boolean accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
