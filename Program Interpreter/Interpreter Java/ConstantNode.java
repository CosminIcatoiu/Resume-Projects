import java.io.*;

public class ConstantNode extends Node {

	private final int constant;

	ConstantNode(int constant) {
		this.constant = constant;
	}

	public int getConstant() {
		return constant;
	}

	public Integer accept(Visitor v, FileWriter w) {
		return v.visit(this, w);
	}
}
