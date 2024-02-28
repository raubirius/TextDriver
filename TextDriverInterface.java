
import javax.swing.JMenuItem;

import javax.swing.text.SimpleAttributeSet;

public interface TextDriverInterface
{
	// World extension – e.g.: .TextWorld
	public String getWorldExtension();


	// Notifications from engine connected to success and failure messages
	public void clearMessages();
	public void resetMessages();


	// --- Menu and menu items ---

	// Context menu component
	public interface ContextMenu
	{
		public JMenuItem add(JMenuItem menuItem);
		public void addSeparator();
		public void show();
	}

	// Menu items definition
	public void clearMainMenu();
	public void insertMainMenuItem(String text);
	public void insertMainMenuItem(String text, int mnemonic);
	public void insertSeparator();
	public void insertOpenMenuItem();
	public void insertSaveMenuItem();
	public void insertExitMenuItem();
	public void insertAboutMenuItem();
	public void defineMenuItemAddMain();
	public void defineMenuItemAdd();
	public void defineMenuItemAddContext(Trigger trigger);
	public void defineMenuItemText(String string);
	public void defineMenuItemMnemonic(String string);
	public void defineMenuItemAccelerator(String string);
	public void defineMenuItemIcon(String string);
	public void defineMenuItemTrigger(String string);
	// public void defineMenuItemClear(); // not allowed

	// Execute default items
	public void aboutApplication();
	public void closeApplication();


	// -- Scroll lists ---

	// Scroll list component
	public interface ScrollList
	{
		public void setLabelText(String labelText);
		public String getLabelText();
		public void clear();
		public void add(Holder holder);
		public void setVisible(boolean aFlag);
	}

	// Get lists
	public ScrollList locationsList();
	public ScrollList itemsList();
	public ScrollList inventoryList();
	public ScrollList insertedList();

	// Reorder the pane
	public void reorderListsPane();

	// Autopopulate
	public boolean autopopulateEnabled();
	public void enableAutopopulate();
	public void disableAutopopulate();
	public void populate();


	// Other components
	public void setIcon(String iconName);
	public void setTitle(String title);
	public String getTitle();
	public void setCentralTextLabel(String label);
	public String getCentralTextLabel();
	public void setInputLineLabel(String label);
	public String getInputLineLabel();


	// Styles
	public void clearStyle();
	public SimpleAttributeSet newStyle(String styleName);
	public SimpleAttributeSet getStyle(String styleName);
	public SimpleAttributeSet getErrorStyle();
	public SimpleAttributeSet getSuccessStyle();
	public void setStyle(SimpleAttributeSet style);
	public void setStyle(String styleName);
	public int dumpStyles();

	// Reserved style name: none (means no style)
	// Mandatory styles: error, success
	// Recomended defaults: info, description, warning,
	//   command, note, teal, aqua, gold, wood.


	// Screen (buffer) manipulation
	public void saveMark();
	public void clearFromMark();
	public void clearMark();
	public void clearScreen();
	public String parseMessage(String templateName, Object... arguments);
	public void parseAndWriteWarning(String templateName, Object... arguments);
	public void write(Object... arguments);
	public void writeLine(Object... arguments);
	public void insertIcon(String filename);
}
