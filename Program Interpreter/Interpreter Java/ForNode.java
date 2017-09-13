import java.io.*;

public class ForNode extends Node {

	private final Node init;
	private final Node condition;
	private final Node increment;
	private final Node body;

	public ForNode(Node init, Node condition, Node increment, Node body) {
		this.init = init;
		this.condition = condition;
		this.increment = increment;
		this.body = body;
	}

	public Node getInit() {
		return init;
	}

	public Node getCondition() {
		return condition;
	}

	public Node getIncrement() {
		return increment;
	}

	public Node getBody() {
		return body;
	}

	public Boolean accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
