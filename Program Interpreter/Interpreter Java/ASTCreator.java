import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ASTCreator {

	/**
	 * This method has the only purpose to split the program in variables and
	 * operators by removing any parenthesis and space. It does this by keeping
	 * from the string representing the expression only the parts that match
	 * some specific regular expressions
	 * 
	 * @param expression
	 *            - the program
	 * @return - the components of the program( variables and operations )
	 */
	public static ArrayList<String> parseExpression(String expression) {

		ArrayList<String> components = new ArrayList<String>();

		Matcher m = Pattern.compile("[0-9]+|[a-zA-Z]+|\\+|\\*|[=]+|\\<|\\;")
				.matcher(expression);

		while (m.find()) {
			components.add(m.group());
		}

		return components;
	}

	/**
	 * This function creates the Abstract Syntax Tree from the components. It
	 * goes through the components in reverse order ( same as for the reverse
	 * polish notations ) because i needed to analyze the variables before the
	 * operations. It follows some simple rules and uses an auxiliary stack. If
	 * we get a variable or a constant from the expression we construct the node
	 * for them ( we differentiate them by checking the first character). For
	 * operations, we have to extract the two components and create a new node
	 * which therefore push on the stack. We do this for every type of
	 * instruction extracting the specific number of parameters for each one and
	 * pushing the resulting node on the stack. In the end we return the last
	 * thing remaining on the stack, namely the root of the resulting tree.
	 * 
	 * @param components
	 *            - the parts of the program
	 * @return - the AST of the program.
	 */
	public static Node createAST(ArrayList<String> components) {

		OperatorSingletonFactory OperatorFactory = OperatorSingletonFactory
				.getFactory();

		OperandSingletonFactory OperandFactory = OperandSingletonFactory
				.getFactory();

		InstructionSingletonFactory InstructionFactory = InstructionSingletonFactory
				.getFactory();

		Stack<Node> stack = new Stack<Node>();

		String operations = "+;*<==";

		for (int i = components.size() - 1; i >= 0; i--) {

			if (operations.contains(components.get(i))) {

				Node aux, left, right;

				left = stack.pop();

				right = stack.pop();

				aux = OperatorFactory.createNode(
						OperatorFactory.getNodeType(components.get(i)), left,
						right);

				stack.push(aux);
			} else {

				switch (components.get(i)) {

				case "if": {

					Node condition, truebody, falsebody, aux;

					condition = stack.pop();
					truebody = stack.pop();
					falsebody = stack.pop();

					aux = InstructionFactory.createIfNode(condition, truebody,
							falsebody);

					stack.push(aux);
					break;
				}

				case "assert": {

					Node aux;
					Node condition = stack.pop();

					aux = InstructionFactory.createAssertNode(condition);
					stack.push(aux);
					break;
				}

				case "return": {

					Node aux, returnValue = stack.pop();

					aux = InstructionFactory.createReturnNode(returnValue);
					stack.push(aux);
					break;
				}

				case "for": {
					Node aux, init, body, increment, condition;

					init = stack.pop();
					condition = stack.pop();
					increment = stack.pop();
					body = stack.pop();

					aux = InstructionFactory.createForNode(init, condition,
							increment, body);

					stack.push(aux);
					break;
				}

				default: {

					if (Character.isLetter(components.get(i).charAt(0))) {

						Node aux = OperandFactory.createNode(
								OperandFactory.getNodeType("string"),
								components.get(i));

						stack.push(aux);
					} else {

						Node aux = OperandFactory.createNode(
								OperandFactory.getNodeType("int"),
								components.get(i));

						stack.push(aux);
					}

				}

				}

			}
		}

		return stack.pop();
	}

}
