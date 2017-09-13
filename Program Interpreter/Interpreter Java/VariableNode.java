import java.io.*;

public class VariableNode extends Node {

	private final String variable;

	public VariableNode(String variable) {
		this.variable = variable;
	}

	public String getVariable() {
		return variable;
	}

	public Integer accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
