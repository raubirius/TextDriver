
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * This is class implements Trasferable interface for Entity transfer. It
 * serves for Drag and Drop operation. See TextDriver.
 */

public class TransferableEntity implements Transferable
{
	// Supported data flavor

	public final static DataFlavor transferDataFlavor =
		new DataFlavor(Entity.class, Entity.class.getName());

	public final static DataFlavor[] transferDataFlavors =
		{transferDataFlavor};

	// The ecapsulated entity for transfer
	private Entity entity;

	/** Constructor for TransferableEntity. */
	public TransferableEntity(Entity entity)
	{ this.entity = entity; }

	/** Returns list of supported DataFlavors. */
	public DataFlavor[] getTransferDataFlavors()
	{ return transferDataFlavors; }

	/** Checks whether specified DataFlavor is available. */
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		if (flavor.equals(transferDataFlavor)) return true;
		return false;
	}

	/**
	 * Transfers the data. Receives specified DataFlavor and returns an
	 * Object appropriate for that flavor. Throws UnsupportedFlavorException
	 * if the requested flavor is not supported.
	 */
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException
	{
		if (flavor.equals(transferDataFlavor)) return entity;
		throw new UnsupportedFlavorException(flavor);
	}
}