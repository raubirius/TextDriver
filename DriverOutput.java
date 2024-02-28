
import static java.lang.System.err;

public class DriverOutput
{
	public static TextDriverInterface textDriver = null;

	public static void write(Object... arguments)
	{
		if (null != textDriver)
			textDriver.write(arguments);
	}

	public static void writeLine(Object... arguments)
	{
		if (null != textDriver)
			textDriver.writeLine(arguments);
	}

	public static void debugMessage(Object object,
		String action, Object... values)
	{
		if (null != Entity.engine)
			Entity.engine.debugMessage(object, action, values);
	}

	public static void printError(Exception e)
	{
		err.println("  Error: " + e.getMessage());
		if (e instanceof NullPointerException) e.printStackTrace();
	}
}
