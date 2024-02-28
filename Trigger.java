
import java.util.Vector;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * Trigger is a named group of command lines processed by TextEngine and
 * enhanced by some properties.
 */

public class Trigger extends Holder
{
	public final Vector<String> onInvoke = new Vector<String>();
	public final TextDriverInterface.ContextMenu contextMenu;

	private boolean isPublic = false;
	// private String nameInList = null;
	private String doubleClick = null;

	public Trigger(String name)
	{
		super(name.replaceAll("[\\s ]+", " ").trim());
		contextMenu = (null == TextDriver.textDriver) ? null : 
			TextDriver.textDriver.newContextMenu(this.name);
	}


	public boolean isPublic()
	{
		return isPublic;
	}

	public void setPublic()
	{
		isPublic = true;
	}

	public void setPrivate()
	{
		isPublic = false;
	}


	/* Not this way…
	public String getNameInList()
	{
		return null == nameInList ? name : nameInList;
	}

	public void setNameInList(String name)
	{
		nameInList = name;
	}
	*/


	public String getDoubleClick()
	{
		return null == doubleClick ? name : doubleClick;
	}

	public void setDoubleClick(String doubleClick)
	{
		this.doubleClick = doubleClick;
	}


	public void copy(Object obj)
	{
		if (obj instanceof Trigger)
		{
			// Reserved
		}
	}
}
