import java.util.*;

public class OperandSingletonFactory {

	private static final OperandSingletonFactory factory = new OperandSingletonFactory();

	HashMap<String, NodeType> nodes;

	private OperandSingletonFactory() {

		nodes = new HashMap<String, NodeType>();

		nodes.put("string", NodeType.Variable);
		nodes.put("int", NodeType.Constant);
	}

	private enum NodeType {
		Variable, Constant
	}

	public Node createNode(NodeType type, String s) {

		switch (type) {

		case Variable:
			return new VariableNode(s);

		case Constant:
			return new ConstantNode(Integer.parseInt(s));
		}

		throw new IllegalArgumentException("The node type " + type
				+ " is not recognised.");
	}

	public static OperandSingletonFactory getFactory() {
		return factory;
	}

	public NodeType getNodeType(String s) {
		return nodes.get(s);
	}
}
