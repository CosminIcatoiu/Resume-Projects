public abstract class OperatorNode extends Node {

	private final Node LeftChild;
	private final Node RightChild;

	public OperatorNode(Node left, Node right) {

		LeftChild = left;
		RightChild = right;

	}

	public Node getRight() {
		return RightChild;
	}

	public Node getLeft() {
		return LeftChild;
	}

}
