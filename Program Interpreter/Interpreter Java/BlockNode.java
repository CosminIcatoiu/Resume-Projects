import java.io.*;

public class BlockNode extends OperatorNode {

	public BlockNode(Node firstBlock, Node secondBlock) {
		super(firstBlock, secondBlock);
	}

	public Object accept(Visitor v, FileWriter w) throws Exception {
		return v.visit(this, w);
	}
}
