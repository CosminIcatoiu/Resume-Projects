import java.io.*;

public interface Visitor {

	public int visit(ConstantNode node, FileWriter w);

	public int visit(VariableNode node, FileWriter w) throws Exception;

	public int visit(PlusNode node, FileWriter w) throws Exception;

	public int visit(MultiplicationNode node, FileWriter w) throws Exception;

	public boolean visit(ComparingNode node, FileWriter w) throws Exception;

	public boolean visit(CheckEqualNode node, FileWriter w) throws Exception;

	public boolean visit(AssignmentNode node, FileWriter w) throws Exception;

	public boolean visit(ForNode node, FileWriter w) throws Exception;

	public boolean visit(IfNode node, FileWriter w) throws Exception;

	public Object visit(BlockNode node, FileWriter w) throws Exception;

	public int visit(ReturnNode node, FileWriter w) throws Exception;

	public boolean visit(AssertNode node, FileWriter w) throws Exception;

}
