import java.io.*;

public class AssertNode extends Node {

	private final Node condition;

	public AssertNode(Node condition) {
		this.condition = condition;
	}

	public Node getCondition() {
		return condition;
	}

	public Boolean accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
