import java.io.*;

public class IfNode extends Node {

	private final Node condition;
	private final Node truecase;
	private final Node falsecase;

	public IfNode(Node condition, Node iftrue, Node iffalse) {
		this.condition = condition;
		this.truecase = iftrue;
		this.falsecase = iffalse;
	}

	public Node getCondition() {
		return condition;
	}

	public Node getTrueCase() {
		return truecase;
	}

	public Node getFalseCase() {
		return falsecase;
	}

	public Boolean accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}

}
