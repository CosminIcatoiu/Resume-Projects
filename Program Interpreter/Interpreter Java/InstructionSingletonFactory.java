public class InstructionSingletonFactory {

	private static final InstructionSingletonFactory factory = new InstructionSingletonFactory();

	private InstructionSingletonFactory() {
	}

	public Node createForNode(Node init, Node condition, Node increment,
			Node body) {
		return new ForNode(init, condition, increment, body);
	}

	public Node createIfNode(Node condition, Node iftrue, Node iffalse) {
		return new IfNode(condition, iftrue, iffalse);
	}

	public Node createAssertNode(Node condition) {
		return new AssertNode(condition);
	}

	public Node createReturnNode(Node returnValue) {
		return new ReturnNode(returnValue);
	}

	public static InstructionSingletonFactory getFactory() {
		return factory;
	}
}
