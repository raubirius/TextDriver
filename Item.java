
import java.util.Vector;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * Item is any object (or animal, character, etc.) in the world.
 */

public class Item extends Entry
{
	public final Vector<String> onShow = new Vector<String>();
	public final Vector<String> onHide = new Vector<String>();
	public final Vector<String> onPick = new Vector<String>();
	public final Vector<String> onDrop = new Vector<String>();
	public final Vector<String> onThrow = new Vector<String>();

	public final Vector<String> onInsert = new Vector<String>();
	public final Vector<String> onAdmit = new Vector<String>();
	public final Vector<String> onRemove = new Vector<String>();
	public final Vector<String> onRelease = new Vector<String>();
	public final Vector<String> onExamine = new Vector<String>();
	public final Vector<String> onUse = new Vector<String>();
	public final Vector<String> onAction = new Vector<String>();

	public final Vector<String> onDrive = new Vector<String>();
	// ‼TODO‼ – custom scripts

	public Item(String name)
	{
		super(name);
	}


	/*
	// This stuff is made on another place…
	@Override public void show()
	{
		// invokeScript(onShow);
		super.show();
	}

	@Override public void hide()
	{
		// invokeScript(onHide);
		super.hide();
	}
	*/
}
