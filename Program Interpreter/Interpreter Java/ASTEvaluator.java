import java.util.*;
import java.io.*;

public class ASTEvaluator implements Visitor {

	/**
	 * The map in which i will keep the variables found in the program.
	 */
	private HashMap<String, Integer> variables = new HashMap<String, Integer>();

	/**
	 * A variable which tells me if i met a return statement in the program.
	 */
	private int returnMet = 0;

	/**
	* A variable which tells me if there was an assert error in the program.
	*/
	private int assertFailed = 0;

	public int checkReturn() {
		return returnMet;
	}

	public int checkAssert() {
		return assertFailed;
	}

	/**
	 * For the constants the visit method only return the value.
	 */
	public int visit(ConstantNode node, FileWriter w) {
		return node.getConstant();
	}

	/**
	 * The function checks if the variable wasn`t declared in the program before
	 * and thwrows an OutofScopeError if this happens.
	 */
	public int visit(VariableNode node, FileWriter w) throws Exception {
		if (!variables.containsKey(node.getVariable())) {
			w.write("Check failed");
			throw new OutofScopeException();
		}

		return variables.get(node.getVariable());
	}

	/**
	 * Returns the sum of the children
	 */
	public int visit(PlusNode node, FileWriter w) throws Exception {

		return (Integer) node.getLeft().accept(this, w)
				+ (Integer) node.getRight().accept(this, w);

	}

	/**
	 * Returns the product of the two children
	 */
	public int visit(MultiplicationNode node, FileWriter w) throws Exception {

		return (Integer) node.getLeft().accept(this, w)
				* (Integer) node.getRight().accept(this, w);
	}

	/**
	 * Checks if the two children have the same value
	 */
	public boolean visit(CheckEqualNode node, FileWriter w) throws Exception {

		return (Integer) node.getLeft().accept(this, w) == (Integer) node
				.getRight().accept(this, w);
	}

	/**
	 * Checks if the first children has a smaller value than the second one.
	 */
	public boolean visit(ComparingNode node, FileWriter w) throws Exception {
		return (Integer) node.getLeft().accept(this, w) < (Integer) node
				.getRight().accept(this, w);
	}

	/**
	 * Modifies the returnMet value and returns the value of the son.
	 */
	public int visit(ReturnNode node, FileWriter w) throws Exception {

		returnMet = 1;
		return (Integer) node.getReturn().accept(this, w);
	}

	/**
	 * A function which tests a condition and makes the assert variable false 
	 * so that i know at the end of the program if there was an assert failed
	 */
	public boolean visit(AssertNode node, FileWriter w) throws Exception {
		if (!(Boolean) node.getCondition().accept(this, w)) {
			assertFailed = 1;
		}
		
		return true;
	}

	/**
	 * The method adds a pair variable-value in the map.
	 */
	public boolean visit(AssignmentNode node, FileWriter w) throws Exception {
		
		variables.put(((VariableNode) node.getLeft()).getVariable(),
					(Integer) node.getRight().accept(this, w));
		return true;
	}

	/**
	 * The function checks the condition and then executes the true branch.
	 */
	public boolean visit(IfNode node, FileWriter w) throws Exception {

		if (((Boolean) node.getCondition().accept(this, w))) {

			node.getTrueCase().accept(this, w);

		} else {

			node.getFalseCase().accept(this, w);
		}

		return true;
	}

	/**
	 * Simulates a for expression in a program by taking an initial condition
	 * and executing the body as long as the condition is not met.
	 */
	public boolean visit(ForNode node, FileWriter w) throws Exception {

		node.getInit().accept(this, w);

		while (true) {
			if (!(Boolean) node.getCondition().accept(this, w))
				break;

			node.getBody().accept(this, w);

			node.getIncrement().accept(this, w);
		}

		return true;
	}

	/**
	 * The block node which executes the subprograms and returns the value returned
	 * by the second one because return is always the last instruction in a program
	 * so it is the most right one in the tree
	 */
	public Object visit(BlockNode node, FileWriter w) throws Exception {

		node.getLeft().accept(this, w);

		return node.getRight().accept(this, w);

	}
}
