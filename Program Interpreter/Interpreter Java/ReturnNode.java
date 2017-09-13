import java.io.*;

public class ReturnNode extends Node {

	private final Node returnValue;

	public ReturnNode(Node returnValue) {
		this.returnValue = returnValue;
	}

	public Node getReturn() {
		return returnValue;
	}

	public Integer accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}

}
