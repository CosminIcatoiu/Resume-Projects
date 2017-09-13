import java.util.*;

public class OperatorSingletonFactory {

	private static final OperatorSingletonFactory factory = new OperatorSingletonFactory();

	private HashMap<String, NodeType> nodes;

	private OperatorSingletonFactory() {

		nodes = new HashMap<String, NodeType>();

		nodes.put("+", NodeType.PlusNode);
		nodes.put("=", NodeType.AssignementNode);
		nodes.put("*", NodeType.MultiplicationNode);
		nodes.put("==", NodeType.CheckNode);
		nodes.put("<", NodeType.ComparingNode);
		nodes.put(";", NodeType.BlockNode);
	}

	private enum NodeType {
		PlusNode, AssignementNode, MultiplicationNode, CheckNode, ComparingNode, BlockNode
	}

	public Node createNode(NodeType type, Node left, Node right) {

		switch (type) {

		case PlusNode:
			return new PlusNode(left, right);

		case MultiplicationNode:
			return new MultiplicationNode(left, right);

		case CheckNode:
			return new CheckEqualNode(left, right);

		case AssignementNode:
			return new AssignmentNode(left, right);

		case ComparingNode:
			return new ComparingNode(left, right);

		case BlockNode:
			return new BlockNode(left, right);
		}

		throw new IllegalArgumentException("The node type" + type
				+ "is not recongised.");

	}

	public static OperatorSingletonFactory getFactory() {
		return factory;
	}

	public NodeType getNodeType(String s) {
		return nodes.get(s);
	}
}
