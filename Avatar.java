
import java.util.Vector;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * Avatar impersonates the user in the world. This class allows the
 * programer/configurer to define some useful handlers like common examine,
 * drop success, etc.
 */

public class Avatar extends Entry
{
	// public Location currentLocation = null; // see Entry.placement
	public final Vector<String> onCommonSuccess = new Vector<String>();
	public final Vector<String> onCommonFailure = new Vector<String>();

	public final Vector<String> onCommonExamine = new Vector<String>();

	public final Vector<String> onWalkSuccess = new Vector<String>();
	public final Vector<String> onWalkFailure = new Vector<String>();
	public final Vector<String> onReturnSuccess = new Vector<String>();
	public final Vector<String> onReturnFailure = new Vector<String>();
	public final Vector<String> onExploreSuccess = new Vector<String>();
	public final Vector<String> onExploreFailure = new Vector<String>();
	public final Vector<String> onPickSuccess = new Vector<String>();
	public final Vector<String> onPickFailure = new Vector<String>();
	public final Vector<String> onDropSuccess = new Vector<String>();
	public final Vector<String> onDropFailure = new Vector<String>();
	public final Vector<String> onThrowSuccess = new Vector<String>();
	public final Vector<String> onThrowFailure = new Vector<String>();
	public final Vector<String> onInsertSuccess = new Vector<String>();
	public final Vector<String> onInsertFailure = new Vector<String>();
	public final Vector<String> onRemoveSuccess = new Vector<String>();
	public final Vector<String> onRemoveFailure = new Vector<String>();
	public final Vector<String> onExamineSuccess = new Vector<String>();
	public final Vector<String> onExamineFailure = new Vector<String>();
	public final Vector<String> onUseSuccess = new Vector<String>();
	public final Vector<String> onUseFailure = new Vector<String>();

	public final Vector<String> onActionSuccess = new Vector<String>();
	public final Vector<String> onActionFailure = new Vector<String>();

	public Avatar()
	{
		super("Avatar");
	}
}
