import java.io.*;

public interface Vistiable {

	public Object accept(Visitor v, FileWriter w) throws Exception;
}
