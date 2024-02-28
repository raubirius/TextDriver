
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.lang.System.out;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FilenameFilter;

import java.net.URL;

import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

// import javax.swing.event.*;

import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.DefaultStyledDocument;

import bookcase.TextFile;

/*#
 * This is the implementation of interface containing predefined GUI for the
 * Text Driver project. Current version number is stored in the
 * applicationVersionString constant.
 * 
 * Project started: 27-Oct-2013.
 * Version 1.0 finished: ???
 */

/**
 * Brief Description comes later…
 */

public class TextDriver extends JFrame implements TextDriverInterface
{
	static final long serialVersionUID = 6540751082257579594L;

	public final static String applicationName = "Text Driver Engine";
	public final static String applicationVersionString = "1.0";
	public final static String defaultTitle =
		applicationName + " " + applicationVersionString;
	public final static String worldExtension = ".TextWorld";

	private final static String configFilename = "textdriver.cfg";
	private final static String applicationIconProperty = "applicationIcon";
	private final static String aboutApplicationProperty = "aboutApplication";
	private final static String languageProperty = "language";
	private final static String worldNameProperty = "worldName";
	private final static String windowWidthProperty = "windowWidth";
	private final static String windowHeightProperty = "windowHeight";
	private final static String windowXProperty = "windowX";
	private final static String windowYProperty = "windowY";
	private final static String savePathProperty = "savePath";
	private final static String lastSaveProperty = "lastSave";
	private final static String namespaceOS;

	private final TextFile config = new TextFile();

	private Language currentLanguage;
	private String applicationIcon = "default-icon.png";
	private String aboutApplication = null;
	private String worldName = "definition.def";
	private String language = "English";
	private String languageRead = "English";
	private String savePath = "";
	private String savePathRead = "";
	private String lastSave = "";
	private String lastSaveRead = "";
	private int initialWidth = 800;
	private int initialHeight = 600;
	private int initialX = 50;
	private int initialY = 50;

	static
	{
		out.println(defaultTitle);

		final String OS = getProperty("os.name");
		final String OSLo = OS.toLowerCase();
		out.println("Detected " + OS + ".");
		if (OSLo.indexOf("win") >= 0) namespaceOS = "Windows";
		else if (OSLo.indexOf("mac") >= 0) namespaceOS = "MacOS";
		else if (OSLo.indexOf("nix") >= 0 ||
			OSLo.indexOf("nux") >= 0 ||
			OSLo.indexOf("aix") >= 0) namespaceOS = "Unix";
		else if (OSLo.indexOf("sunos") >= 0) namespaceOS = "SunOS";
		else namespaceOS = "unknown";
		out.println("Configuration namespace " +
			namespaceOS + " choosen.");
	}


	// Connection between scroll lists and context menus

	private MouseEvent lastMouseEvent = null;
	private JList<Holder> lastContextList = null;

	private final ContextMenu locationsContextMenu =
		new ContextMenu("«empty»");
	private final ContextMenu itemsContextMenu =
		new ContextMenu("«empty»");
	private final ContextMenu inventoryContextMenu =
		new ContextMenu("«empty»");
	private final ContextMenu insertedContextMenu =
		new ContextMenu("«empty»");
	private final ContextMenu dragDropLocationContextMenu =
		new ContextMenu("«empty»");
	private final ContextMenu dragDropItemContextMenu =
		new ContextMenu("«empty»");

	private final Vector<MenuItem> locationsMenuItems =
		new Vector<MenuItem>();
	private final Vector<MenuItem> itemsMenuItems =
		new Vector<MenuItem>();
	private final Vector<MenuItem> inventoryMenuItems =
		new Vector<MenuItem>();
	private final Vector<MenuItem> insertedMenuItems =
		new Vector<MenuItem>();
	private final Vector<MenuItem> dragDropLocationMenuItems =
		new Vector<MenuItem>();
	private final Vector<MenuItem> dragDropItemMenuItems =
		new Vector<MenuItem>();


	// Main menu and context menu accessories

	private MenuItem selectedMenuItem = null;
	private MenuItem openMenuItem = null;
	private MenuItem saveMenuItem = null;
	private MenuItem exitMenuItem = null;
	private MenuItem aboutMenuItem = null;
	private JMenuBar mainMenu = null;
	private int menuMnemonic = KeyEvent.VK_M;
	private int openMnemonic = KeyEvent.VK_O;
	private int saveMnemonic = KeyEvent.VK_S;
	private int exitMnemonic = KeyEvent.VK_X;
	private int aboutMnemonic = KeyEvent.VK_B;

	private boolean mainMenuIsInOriginalState = true;
	private boolean menuItemOpenAdded = true;
	private boolean menuItemSaveAdded = true;
	private boolean menuItemExitAdded = true;
	private boolean menuItemAboutAdded = true;
	private int currentMenu = 0;
	private int currentMenuItem = 3;

	private static String replaceHTMLEntities(String text)
	{
		if (-1 != text.indexOf("<") ||
			-1 != text.indexOf(">") ||
			-1 != text.indexOf("&"))
		{
			text = text.replaceAll("&", "&amp;");
			text = text.replaceAll("<", "&lt;");
			text = text.replaceAll(">", "&gt;");
		}
		return text;
	}

	// Notifications from engine connected to success and failure messages

	public void clearMessages()
	{
		engine.successMessage = null;
		engine.failureMessage = null;
		engine.commandType = null;
		engine.reasonType = null;
	}

	public void resetMessages()
	{
		engine.successMessage = currentLanguage.defaultSuccess;
		engine.failureMessage = currentLanguage.defaultFailure;
		engine.commandType = null;
		engine.reasonType = null;
	}


	// Menu

	public static JMenu createSubmenu(String text, JMenuItem... items)
	{
		JMenu menu = new JMenu(text);
		for (JMenuItem item : items)
		{
			if (null == item) menu.addSeparator();
			else menu.add(item);
		}
		return menu;
	}

	public static JMenu createSubmenu(String text, Vector<JMenuItem> items)
	{
		JMenu menu = new JMenu(text);
		for (JMenuItem item : items)
		{
			if (null == item) menu.addSeparator();
			else menu.add(item);
		}
		return menu;
	}


	public class ContextMenu extends JPopupMenu implements
		TextDriverInterface.ContextMenu
	{
		static final long serialVersionUID = 1232814804281061792L;

		// Label…
		private String originalLabel = null;
		private final JLabel label;

		public ContextMenu()
		{
			super();
			this.label = null;
		}

		public ContextMenu(String label)
		{
			super();
			originalLabel = label;
			if (label.startsWith("<html>"))
				this.label = new JLabel(label);
			else
				this.label = new JLabel("<html><b>" +
					replaceHTMLEntities(label) + "</b></html>");
			this.label.setHorizontalAlignment(SwingConstants.CENTER);
			add(this.label);
			addSeparator();
		}


		public JMenuItem addItem(JMenuItem item)
		{ return add(item); }

		public MenuItem addItem(String text)
		{ return (MenuItem)add(text); }

		public JMenuItem addSubmenu(String text, JMenuItem... items)
		{ return add(createSubmenu(text, items)); }

		public JMenuItem addSubmenu(String text, Vector<JMenuItem> items)
		{ return add(createSubmenu(text, items)); }


		@SuppressWarnings("deprecation") public void show()
		{
			if (null == lastMouseEvent)
			{
				super.show(textDriver, 0, 0);
				return;
			}

			int x = lastMouseEvent.getX();
			int y = lastMouseEvent.getY();

			if (lastMouseEvent.getSource() instanceof Component)
				super.show((Component)lastMouseEvent.getSource(), x, y);
			else
				super.show(textDriver, x, y);
		}

		public void show(int x, int y)
		{ super.show(textDriver, x, y); }


		@Override public JMenuItem add(String text)
		{ return add(new MenuItem(text)); }

		@Override public void setLabel(String text)
		{
			if (null == label) return;
			originalLabel = text;

			if (text.startsWith("<html>"))
				label.setText(text);
			else
				label.setText("<html><b>" +
					replaceHTMLEntities(text) +
					"</b></html>");
		}

		@Override public String getLabel() { return originalLabel; }
	}


	public ContextMenu newContextMenu(String label)
	{
		if (null == label) return new ContextMenu();
		return new ContextMenu(label);
	}


	// Menu items listener – simpler stright version
	private final ActionListener onMenuItem = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			selectedMenuItem = (MenuItem)e.getSource();
			if (e.getSource() == openMenuItem)
				onOpen();
			else if (e.getSource() == saveMenuItem)
				onSave();
			else if (e.getSource() == exitMenuItem)
				onClose();
			else if (e.getSource() == aboutMenuItem)
				onAbout();
			else
				onMenuItem();
		}
	};


	public class MenuItem extends JMenuItem
	{
		static final long serialVersionUID = 3763191125749442256L;

		private String trigger = null;
		private Entity command = null;
		private Entity entity = null;
		private Entity operator = null;
		private Entity tool = null;


		/**
		 * Constructor that creates menu item with text.
		 *
		 *  @param text        text of menu item
		 */
		public MenuItem(String text)
		{
			super(text);
			addActionListener(onMenuItem);
		}

		/**
		 * Constructor that creates menu item with text and mnemonic.
		 *
		 *  @param text        text of menu item
		 *  @param mnemonic    mnemonic shortcut code
		 *                     (e. g.: {@code KeyEvent.VK_A})
		 */
		public MenuItem(String text, int mnemonic)
		{
			super(text);
			addActionListener(onMenuItem);
			if (0 != mnemonic) setMnemonic(mnemonic);
		}

		/**
		 * Constructor that creates menu item with text, mnemonic and
		 * accelerator.
		 *
		 *  @param text        text of menu item
		 *  @param mnemonic    mnemonic shortcut code
		 *                     (e. g.: {@code KeyEvent.VK_A})
		 *  @param accelerator accelerator code (e. g.: {@code KeyEvent.VK_B})
		 */
		public MenuItem(String text, int mnemonic, int accelerator)
		{
			super(text);
			addActionListener(onMenuItem);
			if (0 != accelerator)
				setAccelerator(KeyStroke.getKeyStroke(
					accelerator, Toolkit.getDefaultToolkit().
					getMenuShortcutKeyMask()));
			if (0 != mnemonic) setMnemonic(mnemonic);
		}


		/**
		 * Inserts this item into main menu and increases menu counters.
		 */
		public void insertToMainMenu()
		{
			if (null != mainMenu)
			{
				JMenu mainMenuItem = mainMenu.getMenu(currentMenu);
				if (null != mainMenuItem)
				{
					if (mainMenuIsInOriginalState)
					{
						mainMenuIsInOriginalState = false;
						mainMenuItem.insertSeparator(currentMenuItem);
					}

					mainMenuItem.insert(this, currentMenuItem++);
					if (!mainMenu.isVisible()) mainMenu.setVisible(true);
				}
			}
		}

		/* *
		 * Inserts this item into context menu of specified trigger and
		 * increases menu counters.
		 * /
		public void insertToContext(Trigger trigger)
		{
			// if (null != trigger.contextMenu)
			// {
				// if (mainMenuIsInOriginalState)
				// {
					// mainMenuIsInOriginalState = false;
					// trigger.contextMenu.
					// 	insertSeparator(currentMenuItem);
				// }

				trigger.contextMenu.add(this);
				// if (!mainMenu.isVisible())
				// 	mainMenu.setVisible(true);
			// }
		}*/

		/**
		 * Adds icon to menu item.
		 */
		public void icon(String filename)
		{
			try
			{
				setIcon(fileToIcon(filename));
			}
			catch (Exception e)
			{
				DriverOutput.printError(e);
				setStyle(errorStyle);
				writeLine(e.getMessage());
			}
		}


		/**
		 * If this item has trigger defined this method returns true.
		 */
		public boolean hasTrigger()
		{
			return null != trigger;
		}

		/**
		 * Defines custom trigger for this item.
		 */
		public void setTrigger(String triggerName)
		{
			trigger = triggerName;
		}

		/**
		 * Gets name of trigger to be executed on invoke.
		 */
		public String getTrigger()
		{
			if (null != trigger) return trigger;
			return getText();
		}


		/**
		 * If this item has minimum (required) command attributes defined
		 * this method returns true.
		 */
		public boolean hasCommand()
		{
			return null != command && null != entity;
		}

		/**
		 * Defines command (prefix).  
		 */
		// public void setCommand(String commandID)
		// {
		// 	this.command = engine.getCommandEntity(commandID);
		// }

		public void setCommand(Entity command)
		{
			this.command = command;
		}

		/**
		 * Gets current entity of command (prefix).
		 */
		public Entity getCommand()
		{
			return command;
		}

		/**
		 * Sets entity for command.
		 */
		public void setEntity(Entity entity)
		{
			this.entity = entity;
		}

		/**
		 * Gets current entity for command (to process).
		 */
		public Entity getEntity()
		{
			return entity;
		}

		/**
		 * Defines operator (infix).
		 */
		// public void setOperator(String operatorID)
		// {
		// 	this.operator = engine.getOperatorEntity(operatorID);
		// }

		public void setOperator(Entity operator)
		{
			this.operator = operator;
		}

		/**
		 * Gets current entity of operator.
		 */
		public Entity getOperator()
		{
			return operator;
		}

		/**
		 * Sets entity for operator.
		 */
		public void setTool(Entity tool)
		{
			this.tool = tool;
		}

		/**
		 * Gets current entity for operator (to process).
		 */
		public Entity getTool()
		{
			return tool;
		}

		/**
		 * Gets command line text to be executed on invoke.
		 */
		public String getCommandLine()
		{
			if (null != command && null != entity)
			{
				if (null != operator && null != tool)
					return command.toString() + entity.toString() +
						" " + operator.toString() + tool.toString();
				else
					return command.toString() + entity.toString();
			}

			return getText();
		}
	}


	/**
	 * Clears main menu.
	 */
	public void clearMainMenu()
	{
		if (null != mainMenu)
		{
			for (;;)
			{
				JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

				if (null != mainMenuItem)
					mainMenuItem.removeAll();

				if (currentMenu > 0)
				{
					mainMenu.remove(currentMenu);
					--currentMenu;
				}
				else break;
			}

			mainMenuIsInOriginalState = false;
			menuItemSaveAdded = false;
			menuItemOpenAdded = false;
			menuItemExitAdded = false;
			menuItemAboutAdded = false;

			currentMenuItem = 0;
			mainMenu.setVisible(false);
		}
	}

	/**
	 * Adds new main menu item.
	 * 
	 *  @param text     text of menu item
	 */
	public void insertMainMenuItem(String text)
	{
		if (null != mainMenu)
		{
			JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

			if (null != mainMenuItem)
			{
				mainMenu.setVisible(true);
				mainMenuIsInOriginalState = false;

				if (0 == mainMenuItem.getItemCount())
				{
					mainMenuItem.setText(text);
				}
				else
				{
					mainMenuItem = new JMenu(text);
					mainMenu.add(mainMenuItem);
					++currentMenu; currentMenuItem = 0;
				}
			}
		}
	}

	/**
	 * Adds new main menu item with mnemonic.
	 * 
	 *  @param text     text of menu item
	 *  @param mnemonic mnemonic shortcut code
	 *                  (e. g.: {@code KeyEvent.VK_A})
	 */
	public void insertMainMenuItem(String text, int mnemonic)
	{
		if (null != mainMenu)
		{
			JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

			if (null != mainMenuItem)
			{
				mainMenu.setVisible(true);
				mainMenuIsInOriginalState = false;

				if (0 == mainMenuItem.getItemCount())
				{
					mainMenuItem.setText(text);
					if (0 != mnemonic) mainMenuItem.setMnemonic(mnemonic);
				}
				else
				{
					mainMenuItem = new JMenu(text);
					if (0 != mnemonic) mainMenuItem.setMnemonic(mnemonic);
					mainMenu.add(mainMenuItem);
					++currentMenu; currentMenuItem = 0;
				}
			}
		}
	}

	/**
	 * Adds separator to main menu.
	 */
	public void insertSeparator()
	{
		if (null != mainMenu)
		{
			JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

			if (null != mainMenuItem)
			{
				mainMenuIsInOriginalState = false;
				mainMenuItem.insertSeparator(currentMenuItem++);
				if (!mainMenu.isVisible()) mainMenu.setVisible(true);
			}
		}
	}

	/**
	 * Inserts (restores) “Open” menu item (usable when main menu was
	 * cleared).
	 */
	public void insertOpenMenuItem()
	{
		// if (mainMenuIsInOriginalState) return;

		if (null != mainMenu && !menuItemOpenAdded)
		{
			JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

			if (null != mainMenuItem)
			{
				menuItemOpenAdded = true;
				mainMenuItem.insert(openMenuItem, currentMenuItem++);
				if (!mainMenu.isVisible()) mainMenu.setVisible(true);
			}
		}
	}

	/**
	 * Inserts (restores) “Save” menu item (usable when main menu was
	 * cleared).
	 */
	public void insertSaveMenuItem()
	{
		// if (mainMenuIsInOriginalState) return;

		if (null != mainMenu && !menuItemSaveAdded)
		{
			JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

			if (null != mainMenuItem)
			{
				menuItemSaveAdded = true;
				mainMenuItem.insert(saveMenuItem, currentMenuItem++);
				if (!mainMenu.isVisible()) mainMenu.setVisible(true);
			}
		}
	}

	/**
	 * Inserts (restores) “Exit” menu item (usable when main menu was
	 * cleared).
	 */
	public void insertExitMenuItem()
	{
		// if (mainMenuIsInOriginalState) return;

		if (null != mainMenu && !menuItemExitAdded)
		{
			JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

			if (null != mainMenuItem)
			{
				menuItemExitAdded = true;
				mainMenuItem.insert(exitMenuItem, currentMenuItem++);
				if (!mainMenu.isVisible()) mainMenu.setVisible(true);
			}
		}
	}

	/**
	 * Inserts (restores) “About” menu item (usable when main menu was
	 * cleared).
	 */
	public void insertAboutMenuItem()
	{
		// if (mainMenuIsInOriginalState) return;

		if (null != mainMenu && !menuItemAboutAdded)
		{
			JMenu mainMenuItem = mainMenu.getMenu(currentMenu);

			if (null != mainMenuItem)
			{
				menuItemAboutAdded = true;
				mainMenuItem.insert(aboutMenuItem, currentMenuItem++);
				if (!mainMenu.isVisible()) mainMenu.setVisible(true);
			}
		}
	}

	// public void addDefaultMenuSeparator()
	// {
	// 	JMenu mainMenuItem = mainMenu.getMenu(currentMenu);
	// 	if (null != mainMenuItem)
	// 		mainMenuItem.addSeparator();
	// }

	// public void addDefaultAboutItem()
	// {
	// 	JMenu mainMenuItem = mainMenu.getMenu(currentMenu);
	// 	if (null != mainMenuItem)
	// 		mainMenuItem.add(aboutMenuItem);
	// }

	// public void addDefaultExitItem()
	// {
	// 	JMenu mainMenuItem = mainMenu.getMenu(currentMenu);
	// 	if (null != mainMenuItem)
	// 		mainMenuItem.add(exitMenuItem);
	// }


	// Define menu items…

	private String defineMenuItemText = null;
	private String defineMenuItemMnemonic = null;
	private String defineMenuItemAccelerator = null;
	private String defineMenuItemIcon = null;
	private String defineMenuItemTrigger = null;

	public void defineMenuItemAddMain()
	{
		if (null != defineMenuItemText)
		{
			if (null != defineMenuItemMnemonic)
			{
				int mnemonic = parseKeyConstant(
					defineMenuItemMnemonic, 0);
				insertMainMenuItem(defineMenuItemText, mnemonic);
			}
			else
				insertMainMenuItem(defineMenuItemText);
		}

		defineMenuItemClear();
	}

	public void defineMenuItemAdd()
	{
		if (null != defineMenuItemText)
		{
			MenuItem menuItem;
			defineMenuItemText = defineMenuItemText.trim();

			if (null != defineMenuItemMnemonic &&
				null != defineMenuItemAccelerator)
			{
				int mnemonic = parseKeyConstant(
					defineMenuItemMnemonic, 0);
				int accelerator = parseKeyConstant(
					defineMenuItemAccelerator, 0);
				menuItem = new MenuItem(
					defineMenuItemText,
					mnemonic, accelerator);
			}
			else if (null != defineMenuItemAccelerator)
			{
				int accelerator = parseKeyConstant(
					defineMenuItemAccelerator, 0);
				menuItem = new MenuItem(
					defineMenuItemText, 0, accelerator);
			}
			else if (null != defineMenuItemMnemonic)
			{
				int mnemonic = parseKeyConstant(
					defineMenuItemMnemonic, 0);
				menuItem = new MenuItem(
					defineMenuItemText, mnemonic);
			}
			else
			{
				menuItem = new MenuItem(defineMenuItemText);
			}

			if (null != defineMenuItemIcon)
				menuItem.icon(defineMenuItemIcon);

			// if the name of trigger is equal to null the item returns its
			// text as trigger name
			if (null != defineMenuItemTrigger)
				menuItem.setTrigger(defineMenuItemTrigger);

			menuItem.insertToMainMenu();
		}

		defineMenuItemClear();
	}

	public void defineMenuItemAddContext(Trigger trigger)
	{
		if (null != defineMenuItemText && null != trigger.contextMenu)
		{
			MenuItem menuItem;
			defineMenuItemText = defineMenuItemText.trim();

			if (null != defineMenuItemMnemonic &&
				null != defineMenuItemAccelerator)
			{
				int mnemonic = parseKeyConstant(
					defineMenuItemMnemonic, 0);
				int accelerator = parseKeyConstant(
					defineMenuItemAccelerator, 0);
				menuItem = new MenuItem(
					defineMenuItemText,
					mnemonic, accelerator);
			}
			else if (null != defineMenuItemAccelerator)
			{
				int accelerator = parseKeyConstant(
					defineMenuItemAccelerator, 0);
				menuItem = new MenuItem(
					defineMenuItemText, 0, accelerator);
			}
			else if (null != defineMenuItemMnemonic)
			{
				int mnemonic = parseKeyConstant(
					defineMenuItemMnemonic, 0);
				menuItem = new MenuItem(
					defineMenuItemText, mnemonic);
			}
			else
			{
				menuItem = new MenuItem(defineMenuItemText);
			}

			if (null != defineMenuItemIcon)
				menuItem.icon(defineMenuItemIcon);

			// the context menu items are defined for specified triggers
			// the item property named “trigger” changes the meaning and
			// becomes to be a list of parameters for the real trigger
			if (null == defineMenuItemTrigger)
				menuItem.setTrigger(trigger.name);
			else
				menuItem.setTrigger(trigger.name + " " +
					defineMenuItemTrigger);

			// menuItem.insertToContext(trigger);
			trigger.contextMenu.add(menuItem);
		}

		defineMenuItemClear();
	}

	public void defineMenuItemText(String string)
	{ defineMenuItemText = string; }
	public void defineMenuItemMnemonic(String string)
	{ defineMenuItemMnemonic = string; }
	public void defineMenuItemAccelerator(String string)
	{ defineMenuItemAccelerator = string; }
	public void defineMenuItemIcon(String string)
	{ defineMenuItemIcon = string; }
	public void defineMenuItemTrigger(String string)
	{ defineMenuItemTrigger = string; }

	public void defineMenuItemClear()
	{
		defineMenuItemText = null;
		defineMenuItemMnemonic = null;
		defineMenuItemAccelerator = null;
		defineMenuItemIcon = null;
		defineMenuItemTrigger = null;
	}


	public void aboutApplication()
	{ onAbout(); }

	public void closeApplication()
	{ onClose(); }


	// Scroll lists class

	public class ScrollList extends JPanel implements
		TextDriverInterface.ScrollList
	{
		static final long serialVersionUID = 1194262576392754120L;

		private MouseAdapter mouseAdapter = new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
				{
					lastMouseEvent = e;
					lastContextList = ScrollList.this.list;

					lastContextList.setSelectedIndex(
						lastContextList.locationToIndex(e.getPoint()));

					onContextMenu();
				}
				else if (2 == e.getClickCount())
				{
					lastMouseEvent = e;
					lastContextList = ScrollList.this.list;
					onDoubleClick();
				}
			}
		};

		public final JLabel label;
		public final JScrollPane scroll;
		public final JList<Holder> list;
		public final DefaultListModel<Holder> model =
			new DefaultListModel<Holder>();

		// Default constructor
		public ScrollList()
		{
			super(new BorderLayout());

			list = new JList<Holder>(model);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setDragEnabled(true);
			list.setTransferHandler(new ScrollListDragDropHandler(list));
			scroll = new JScrollPane(list);

			label = new JLabel();
			label.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
			label.setVisible(false);

			add(label, BorderLayout.NORTH);
			add(scroll, BorderLayout.CENTER);

			list.addMouseListener(mouseAdapter);
		}

		// Constructor initialising label text
		public ScrollList(String labelText)
		{
			super(new BorderLayout());

			list = new JList<Holder>(model);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setDragEnabled(true);
			list.setTransferHandler(new ScrollListDragDropHandler(list));
			scroll = new JScrollPane(list);

			label = new JLabel(labelText);
			label.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));

			add(label, BorderLayout.NORTH);
			add(scroll, BorderLayout.CENTER);

			list.addMouseListener(mouseAdapter);
		}


		public void setLabelText(String labelText)
		{
			if (null == labelText)
				label.setVisible(false);
			else
			{
				label.setText(labelText);
				label.setVisible(true);
			}
		}

		public String getLabelText()
		{
			if (!label.isVisible()) return null;
			return label.getText();
		}


		public void clear()
		{
			model.clear();
		}

		public void add(Holder holder)
		{
			model.addElement(holder);
		}


		// These methods were prepared, but never used…

		// public Object getSelectedValue()
		// {
		// 	if (null == list.getSelectedValue())
		// 		throw new RuntimeException(currentLanguage.errorNoSelection);
		// 	return list.getSelectedValue(); // .toString();
		// }

		// public void setSelectedValue(Object value)
		// {
		// 	if (null != value)// && 0 != value.toString().length())
		// 		list.setSelectedValue(value, true);
		// }
	}


	// Drag and drop
	private Entity dragSourceEntity = null;
	private Entity dropTargetEntity = null;
	private int dragSourceIndex = -1;
	private int dropTargetIndex = -1;

	public class ScrollListDragDropHandler extends TransferHandler
		implements DragSourceListener, DragGestureListener
	{
		static final long serialVersionUID = 4711179014313583307L;

		private final JList<Holder> list;
		private final DragSource dragSource;


		public ScrollListDragDropHandler(JList<Holder> list)
		{
			this.list = list;
			this.dragSource = new DragSource();

			dragSource.createDefaultDragGestureRecognizer(
				list, DnDConstants.ACTION_MOVE, this);
		}

		public void dragGestureRecognized(DragGestureEvent dge)
		{
			dragSourceIndex = list.getSelectedIndex();
			dragSourceEntity = (Entity)list.getSelectedValue();
			TransferableEntity transferable =
				new TransferableEntity(dragSourceEntity);
			dragSource.startDrag(dge, DragSource.DefaultCopyDrop,
				transferable, this);
		}

		public void dragDropEnd(DragSourceDropEvent dsde)
		{
			if (dsde.getDropSuccess())
			{
				// out.println("Drag and Drop succeeded");
				// out.println("dragSourceEntity: " + dragSourceEntity);
				// out.println("dropTargetEntity: " + dropTargetEntity);
				// out.println("dragSourceIndex: " + dragSourceIndex);
				// out.println("dropTargetIndex: " + dropTargetIndex);
				onDragDrop();
			}
			dragSourceEntity = null;
			dropTargetEntity = null;
		}


		public void dragEnter(DragSourceDragEvent dsde) {}
		public void dragExit(DragSourceEvent dse) {}
		public void dragOver(DragSourceDragEvent dsde) {}
		public void dropActionChanged(DragSourceDragEvent dsde) {}


		public boolean canImport(TransferHandler.TransferSupport support)
		{
			if (!support.isDrop() || !support.isDataFlavorSupported
				(TransferableEntity.transferDataFlavor)) return false;
			return -1 != ((JList.DropLocation)
				support.getDropLocation()).getIndex();
		}


		public boolean importData(TransferHandler.TransferSupport support)
		{
			if (!canImport(support)) return false;

			// If this was an another class (in another application)
			// this is the way to get source entity…
			// try
			// {
			// 	dragSourceEntity = (Entity)support.getTransferable().
			// 		getTransferData(TransferableEntity.transferDataFlavor);
			// }
			// catch (Exception e)
			// {
			// 	return false;
			// }

			java.awt.Point pt = support.getDropLocation().getDropPoint();

			lastMouseEvent = new MouseEvent(
				list, MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(),
				MouseEvent.BUTTON3, pt.x, pt.y, 1, true);

			dropTargetIndex = ((JList.DropLocation)
				support.getDropLocation()).getIndex();
			dropTargetEntity = (Entity)list.getModel().
				getElementAt(dropTargetIndex);

			return true;
		}
	}


	// The engine instance
	public final TextEngine engine = new TextEngine(this);


	// Components
	private final JMenuBar inputLinePane = new JMenuBar();
	private final JPanel rightPane = new JPanel(new GridLayout(3, 1));
	private final JPanel centralTextPane = new JPanel(new BorderLayout());
	private final JTextPane centralTextArea = new JTextPane();
	private final DefaultStyledDocument centralTextDocument =
		(DefaultStyledDocument)centralTextArea.getStyledDocument();
	private final JTextField inputLine = new JTextField();
	private final JLabel centralTextLabel = new JLabel();
	private final JLabel inputLineLabel = new JLabel();
	private final ScrollList locationsList = new ScrollList();
	private final ScrollList itemsList = new ScrollList();
	private final ScrollList inventoryList = new ScrollList();
	private final ScrollList insertedList = new ScrollList();

	private final JScrollPane centralTextScrollPane =
		new JScrollPane(centralTextArea);

	// Command line history
	private Vector<String> historyCommandLines = new Vector<String>();
	private String historyEditedLine = null;
	private int historyIndex = -1;


	private final TreeMap<String, SimpleAttributeSet> styles =
		new TreeMap<String, SimpleAttributeSet>();
	private final SimpleAttributeSet infoStyle    = newStyle("info");
	private final SimpleAttributeSet errorStyle   = newStyle("error");
	private final SimpleAttributeSet warningStyle = newStyle("warning");
	private final SimpleAttributeSet commandStyle = newStyle("command");
	private final SimpleAttributeSet successStyle = newStyle("success");
	private SimpleAttributeSet currentStyle = null;

	// Save and read current state
	private final TextDriverSaveFilesFilter tdsff =
		new TextDriverSaveFilesFilter();
	private FileDialog openDialog = null, saveDialog = null;
	private String currentSave = null;

	private final TextFile state = new TextFile();


	// Constructor
	private TextDriver()
	{
		out.println("Application starts.");

		// Set system Look and Feel
		try
		{
			UIManager.setLookAndFeel(UIManager.
				getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}

		// Read configuration
		try
		{
			config.openForReading(configFilename);
			config.propertiesNamespace(namespaceOS);

			initialWidth = config.readProperty
				(windowWidthProperty, (long)initialWidth).intValue();
			initialHeight = config.readProperty
				(windowHeightProperty, (long)initialHeight).intValue();
			initialX = config.readProperty
				(windowXProperty, (long)initialX).intValue();
			initialY = config.readProperty
				(windowYProperty, (long)initialY).intValue();
			savePath = savePathRead = config.readProperty
				(savePathProperty, savePathRead);
			lastSave = lastSaveRead = config.readProperty
				(lastSaveProperty, lastSaveRead);

			// Properties common for all OS
			config.propertiesNamespace(null);
			applicationIcon = config.readProperty
				(applicationIconProperty, applicationIcon);
			aboutApplication = config.readProperty
				(aboutApplicationProperty, aboutApplication);
			worldName = config.readProperty
				(worldNameProperty, worldName);
			language = languageRead = config.readProperty
				(languageProperty, languageRead);

			config.close();
			out.println("Configuration read.");
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}

		// Read language and parse mnemonic keys for default menu items
		currentLanguage = new Language(language);
		if (null != currentLanguage.menuMnemonic)
			menuMnemonic = parseKeyConstant(
				currentLanguage.menuMnemonic, menuMnemonic);
		if (null != currentLanguage.openMnemonic)
			openMnemonic = parseKeyConstant(
				currentLanguage.openMnemonic, openMnemonic);
		if (null != currentLanguage.saveMnemonic)
			saveMnemonic = parseKeyConstant(
				currentLanguage.saveMnemonic, saveMnemonic);
		if (null != currentLanguage.exitMnemonic)
			exitMnemonic = parseKeyConstant(
				currentLanguage.exitMnemonic, exitMnemonic);
		if (null != currentLanguage.aboutMnemonic)
			aboutMnemonic = parseKeyConstant(
				currentLanguage.aboutMnemonic, aboutMnemonic);

		// Listener for main window
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent winEvt)
				{
					onClose();
				}
			});

		// Listener for central text area
		centralTextArea.addKeyListener(new KeyListener()
			{
				public void keyPressed(KeyEvent e)
				{
					inputLine.dispatchEvent(e);
				}

				public void keyReleased(KeyEvent e)
				{
					inputLine.dispatchEvent(e);
				}

				public void keyTyped(KeyEvent e)
				{
					inputLine.dispatchEvent(e);
					inputLine.requestFocus();
					beep();
				}
			});

		// Listener for input text line
		inputLine.addKeyListener(new KeyListener()
			{
				public void keyPressed(KeyEvent e)
				{
					// inputDump("Pressed", e);
					// out.println("VK_DOWN " + e.VK_DOWN);

					if (e.getKeyCode() == KeyEvent.VK_ENTER)
						onInputSubmit();
					else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
						onInputCancel();
					else if (e.getKeyCode() == KeyEvent.VK_UP)
					{
						if (e.isControlDown() || e.isMetaDown())
						{
							// out.println("First command");
							firstCommand();
						}
						else
						{
							// out.println("Previous command");
							previousCommand();
						}
						e.consume();
					}
					else if (e.getKeyCode() == KeyEvent.VK_DOWN)
					{
						if (e.isControlDown() || e.isMetaDown())
						{
							// out.println("Last command");
							lastCommand();
						}
						else
						{
							// out.println("Next command");
							nextCommand();
						}
						e.consume();
					}
				}

				public void keyReleased(KeyEvent e)
				{
					// inputDump("Released", e);
				}

				public void keyTyped(KeyEvent e)
				{
					// inputDump("Typed", e);
				}

				// private void inputDump(String prompt, KeyEvent e)
				// {
				// 	int keyCode = e.getKeyCode();
				// 	int keyChar = e.getKeyChar();
				// 	String keyText = KeyEvent.getKeyText(keyCode);
				// 	out.println(prompt + ": " + keyText + " code: " + keyCode +
				// 		" char: " + keyChar);
				// }
			});

		// Menu bar
		mainMenu = new JMenuBar();
		JMenu mainMenuItem = new JMenu(currentLanguage.menu);
		mainMenuItem.setMnemonic(menuMnemonic);
		mainMenu.add(mainMenuItem);

		// Default menu items
		openMenuItem = new MenuItem(currentLanguage.open,
			openMnemonic, KeyEvent.VK_O);
		mainMenuItem.add(openMenuItem);
		saveMenuItem = new MenuItem(currentLanguage.save,
			saveMnemonic, KeyEvent.VK_S);
		mainMenuItem.add(saveMenuItem);

		aboutMenuItem = new MenuItem(currentLanguage.about,
			aboutMnemonic, KeyEvent.VK_B);
		exitMenuItem = new MenuItem(currentLanguage.exit,
			exitMnemonic, KeyEvent.VK_W);

		mainMenuItem.addSeparator();
		mainMenuItem.add(aboutMenuItem);
		mainMenuItem.add(exitMenuItem);

		// Central text area
		centralTextArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)centralTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		centralTextPane.add(centralTextLabel, BorderLayout.NORTH);
		centralTextPane.add(centralTextScrollPane, BorderLayout.CENTER);
		centralTextLabel.setBorder(BorderFactory.
			createEmptyBorder(4, 2, 2, 2));
		centralTextLabel.setVisible(false);

		// Locations/items interactive lists
		rightPane.add(locationsList);
		rightPane.add(itemsList);
		rightPane.add(inventoryList);
		// rightPane.add(insertedList);
		insertedList.setVisible(false);

		// Input text line
		inputLineLabel.setBorder(BorderFactory.
			createEmptyBorder(0, 4, 0, 8));
		inputLinePane.add(inputLineLabel);
		inputLinePane.add(inputLine);
		inputLineLabel.setVisible(false);

		// Main layout (all together)
		setJMenuBar(mainMenu);
		setLayout(new BorderLayout());
		add(centralTextPane, BorderLayout.CENTER);
		add(rightPane, BorderLayout.EAST);
		add(inputLinePane, BorderLayout.SOUTH);

		// Setup styles
		setupDefaultStyles();

		// Main window setup
		setTitle(null); // this method is overriden; null means default title
		setSize(initialWidth, initialHeight);
		setLocation(initialX, initialY);

		// Start
		insertIcon("default-icon-small.png");
		setStyle(infoStyle);
		write(" Welcome in the ", defaultTitle,
			" made by Roman Horváth. ");
		insertIcon("default-icon-small.png");
		writeLine('\n');
	}


	// Private helpers for reading and writing the state

	private class TextDriverSaveFilesFilter implements FilenameFilter
	{
		@Override public boolean accept(File dir, String name)
		{
			if (!name.endsWith(".save")) return false;
			if (!name.endsWith("-" + worldName + ".save"))
				return false;
			return true;
		}
	}

	private void reformatSaveFilename()
	{
		if (!currentSave.endsWith("-" + worldName + ".save"))
		{
			if (currentSave.endsWith(".save"))
			{
				currentSave = currentSave.substring(0, currentSave.length() -
					5) + "-" + worldName + ".save";
			}
			else
			{
				currentSave = currentSave + "-" +
					worldName + ".save";
			}
		}
	}


	// Reading and writing the state

	public boolean readState()
	{
		boolean success = false;

		try
		{
			state.openForReading(currentSave);
			engine.resetAll();
			engine.readAll(state);
			out.println("Read.");

			setStyle(infoStyle);
			writeLine(parseMessage("readSuccess", currentSave));
			writeLine();

			success = true;
			onReadState();
		}
		catch (Exception e)
		{
			engine.resetAll();
			DriverOutput.printError(e);

			setStyle(errorStyle);
			writeLine(e.getMessage());
			writeLine();
			onStartup();
		}

		// The safe close of the file
		try { state.close(); } catch (Exception e)
		{ DriverOutput.printError(e); }

		return success;
	}

	public boolean writeState()
	{
		boolean success = false;

		try
		{
			state.openForWriting(currentSave);
			engine.writeAll(state);
			out.println("Saved.");

			setStyle(infoStyle);
			writeLine(parseMessage("writeSuccess", currentSave));
			writeLine();

			success = true;
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);

			setStyle(errorStyle);
			writeLine(e.getMessage());
			writeLine();
		}

		// The safe close of the file
		try { state.close(); } catch (Exception e)
		{ DriverOutput.printError(e); }

		return success;
	}

	public void autoSaveState()
	{
		if (null != lastSave && !lastSave.isEmpty())
		{
			out.println("Autosaving: " + lastSave);
			currentSave = lastSave;
			reformatSaveFilename();

			if (!writeState())
			{
				lastSave = "";
				currentSave = null;
			}
		}
	}

	public void autoLoadState()
	{
		if (null != lastSave && !lastSave.isEmpty())
		{
			out.println("Autoloading: " + lastSave);
			currentSave = lastSave;
			reformatSaveFilename();

			if (!readState())
			{
				lastSave = "";
				currentSave = null;
			}
		}
	}

	public String getWorldExtension()
	{ return worldExtension; }


	// Private “action handlers”

	private void onOpen()
	{
		out.println("Open invoked.");

		if (null == openDialog)
		{
			openDialog = new FileDialog(this,
				currentLanguage.openDialogTitle, FileDialog.LOAD);
			if (null != savePath && !savePath.isEmpty())
				openDialog.setDirectory(savePath);
			else
				openDialog.setDirectory(new File(".").getPath());
			openDialog.setFilenameFilter(tdsff);
		}

		openDialog.setVisible(true);

		if (null != openDialog.getDirectory())
			savePath = openDialog.getDirectory();

		if (null != openDialog.getFile())
		{
			currentSave = openDialog.getDirectory() + (openDialog.
				getDirectory().endsWith(File.separator) ? "" :
				File.separator) + openDialog.getFile();
			reformatSaveFilename();

			out.println("  Open: " + currentSave);

			if (readState())
				lastSave = currentSave;
		}
	}

	private void onSave()
	{
		out.println("Save invoked.");

		if (null == currentSave)
		{
			if (null == saveDialog)
			{
				saveDialog = new FileDialog(this,
					currentLanguage.saveDialogTitle, FileDialog.SAVE);
				if (null != savePath && !savePath.isEmpty())
					saveDialog.setDirectory(savePath);
				else
					saveDialog.setDirectory(new File(".").getPath());
				saveDialog.setFilenameFilter(tdsff);
			}

			saveDialog.setVisible(true);

			if (null != saveDialog.getDirectory())
				savePath = saveDialog.getDirectory();

			if (null != saveDialog.getFile())
			{
				currentSave = saveDialog.getDirectory() + (saveDialog.
					getDirectory().endsWith(File.separator) ? "" :
					File.separator) + saveDialog.getFile();
				reformatSaveFilename();

				out.println("  Fresh save: " + currentSave);
			}
		}
		else
			out.println("  Save: " + currentSave);

		if (null != currentSave)
		{
			if (writeState())
				lastSave = currentSave;
		}
	}

	private void onClose()
	{
		autoSaveState();

		boolean saveConfig = false; // reserved for some other future ways
		                            // of detecting the changes in
		                            // the configuration

		if (initialWidth != getWidth() || initialHeight != getHeight() ||
			initialX != getLocation().x || initialY != getLocation().y ||
			!languageRead.equals(language) ||
			!savePathRead.equals(savePath) || !lastSaveRead.equals(lastSave))
			saveConfig = true;

		if (saveConfig) try
		{
			config.openForWriting(configFilename);
			config.propertiesNamespace(namespaceOS);

			config.writeProperty(windowWidthProperty, getWidth());
			config.writeProperty(windowHeightProperty, getHeight());
			config.writeProperty(windowXProperty, getLocation().x);
			config.writeProperty(windowYProperty, getLocation().y);
			config.writeProperty(savePathProperty, savePath);
			config.writeProperty(lastSaveProperty, lastSave);

			// Properties common for all OS
			config.propertiesNamespace(null);
			// config.writeProperty(applicationIconProperty, applicationIcon);
			// config.writeProperty(aboutApplicationProperty, aboutApplication);
			// config.writeProperty(worldNameProperty, worldName);
			config.writeProperty(languageProperty, language);

			config.close();
			out.println("Configuration saved.");
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}

		out.println("Application ends.");
		exit(0);
	}

	private void onAbout()
	{
		setStyle(infoStyle);
		writeLine("About:\nThis application is made by the ", defaultTitle,
			" tool made by Roman Horváth.");

		setStyle(infoStyle);
		if (null == aboutApplication)
			writeLine("More details should be provided " +
				"by the creator of this application.");
		else writeLine(aboutApplication);
	}

	private void onMenuItem()
	{
		if (null != selectedMenuItem)
		{
			if (selectedMenuItem.hasCommand())
			{
				executeCommand(selectedMenuItem.getCommandLine());
			}
			else
			{
				clearMessages();
				engine.backupAll();

				try
				{
					if (engine.invokeTrigger(
						selectedMenuItem.getTrigger(), false))
					{
						if (null != engine.successMessage)
						{
							setStyle(successStyle);
							writeLine(engine.successMessage);
							writeLine();
						}
						populate();
					}
					else
					{
						engine.restoreAll();
						if (null != engine.failureMessage)
						{
							setStyle(errorStyle);
							writeLine(engine.failureMessage);
							writeLine();
						}
						else beep();
					}
				}
				catch (Exception e)
				{
					if (e instanceof NullPointerException) e.printStackTrace();

					if (engine.runAvatarCommonFailure())
					{
						if (0 != e.getMessage().length())
						{
							setStyle(errorStyle);
							writeLine(e.getMessage());
							writeLine();
						}
					}
					else writeLine();

					engine.restoreAll();
				}

				cutHugeCentralText();
			}
		}
	}

	private void onContextMenu()
	{
		if (null == lastContextList)
		{
			err.println("  Error: there is no context list active!");
			beep();
			return;
		}

		Object selectedValue = lastContextList.getSelectedValue();
		Location location = selectedValue instanceof Location ?
			(Location)selectedValue : null;
		Item item = selectedValue instanceof Item ?
			(Item)selectedValue : null;
		Trigger trigger = selectedValue instanceof Trigger ?
			(Trigger)selectedValue : null;

		if (null != location)
		{
			locationsContextMenu.setLabel(selectedValue.toString());
			for (MenuItem menuItem : locationsMenuItems)
				menuItem.setEntity(location);
			locationsContextMenu.show();
		}
		else if (null != item)
		{
			if (item.getPlacement() == engine.avatar)
			{
				inventoryContextMenu.setLabel(selectedValue.toString());
				for (MenuItem menuItem : inventoryMenuItems)
					menuItem.setEntity(item);
				inventoryContextMenu.show();
			}
			else if (item.getPlacement() instanceof Location)
			{
				itemsContextMenu.setLabel(selectedValue.toString());
				for (MenuItem menuItem : itemsMenuItems)
					menuItem.setEntity(item);
				itemsContextMenu.show();
			}
			else if (item.getPlacement() instanceof Item)
			{
				insertedContextMenu.setLabel(selectedValue.toString());
				for (MenuItem menuItem : insertedMenuItems)
				{
					menuItem.setEntity(item);
					menuItem.setTool(item.getPlacement());
				}
				insertedContextMenu.show();
			}
			else beep();
		}
		else if (null != trigger && null != trigger.contextMenu)
		{
			trigger.contextMenu.show();
			// beep();
		}
		else beep();
	}

	private void onDoubleClick()
	{
		if (null == lastContextList)
		{
			err.println("  Error: there is no context list active!");
			beep();
			return;
		}

		Object selectedValue = lastContextList.getSelectedValue();

		Location location = selectedValue instanceof Location ?
			(Location)selectedValue : null;
		Item item = selectedValue instanceof Item ?
			(Item)selectedValue : null;
		Trigger trigger = selectedValue instanceof Trigger ?
			(Trigger)selectedValue : null;

		Entity commandEntity, operatorEntity;

		if (null != location)
		{
			commandEntity = engine.getCommandEntity("walk");
			executeCommand(commandEntity.toString() + location.toString());
		}
		else if (null != item)
		{
			if (engine.avatar == item.getPlacement())
			{
				commandEntity = engine.getCommandEntity("drop");
				executeCommand(commandEntity.toString() + item.toString());
			}
			else if (item.getPlacement() instanceof Location)
			{
				commandEntity = engine.getCommandEntity("pick");
				executeCommand(commandEntity.toString() + item.toString());
			}
			else if (item.getPlacement() instanceof Item)
			{
				commandEntity = engine.getCommandEntity("remove");
				operatorEntity = engine.getOperatorEntity("remove");
				executeCommand(commandEntity.toString() + item.toString() +
					" " + operatorEntity.toString() +
					item.getPlacement().toString());
			}
			else
				beep();
		}
		else if (null != trigger)
		{
			executeCommand(trigger.getDoubleClick()); // was: toString());
		}
		else beep();
	}

	private void onDragDrop()
	{
		if (null == dragSourceEntity || null == dropTargetEntity)
		{
			err.println("  Error: dragSource and/or dropTarget is missing!");
			beep();
			return;
		}

		if (dragSourceEntity instanceof Location)
		{
			dragDropLocationContextMenu.setLabel(dragSourceEntity.toString() +
				" – " + dropTargetEntity.toString());
			for (MenuItem menuItem : dragDropLocationMenuItems)
			{
				menuItem.setEntity(dragSourceEntity);
				menuItem.setTool(dropTargetEntity);
			}
			dragDropLocationContextMenu.show();
		}
		else if (dropTargetEntity instanceof Location)
		{
			dragDropLocationContextMenu.setLabel(dropTargetEntity.toString() +
				" – " + dragSourceEntity.toString());
			for (MenuItem menuItem : dragDropLocationMenuItems)
			{
				menuItem.setEntity(dropTargetEntity);
				menuItem.setTool(dragSourceEntity);
			}
			dragDropLocationContextMenu.show();
		}
		else
		{
			dragDropItemContextMenu.setLabel(dragSourceEntity.toString() +
				" – " + dropTargetEntity.toString());
			for (MenuItem menuItem : dragDropItemMenuItems)
			{
				menuItem.setEntity(dragSourceEntity);
				menuItem.setTool(dropTargetEntity);
			}
			dragDropItemContextMenu.show();
		}
	}

	private void onStartup(String[] args)
	{
		clearMessages();
		engine.backupAll();

		try
		{
			if (engine.runStartup(args))
			{
				if (null != engine.successMessage)
				{
					setStyle(successStyle);
					writeLine(engine.successMessage);
				}
				populate();
			}
			else
			{
				engine.restoreAll();
				if (null != engine.failureMessage)
				{
					setStyle(errorStyle);
					writeLine(engine.failureMessage);
					writeLine();
				}
				else beep();
			}
		}
		catch (Exception e)
		{
			if (e instanceof NullPointerException) e.printStackTrace();

			if (engine.runAvatarCommonFailure())
			{
				if (0 != e.getMessage().length())
				{
					setStyle(errorStyle);
					writeLine(e.getMessage());
					writeLine();
				}
			}
			else writeLine();

			engine.restoreAll();
		}

		cutHugeCentralText();
	}

	private void onStartup()
	{
		clearMessages();
		engine.backupAll();

		try
		{
			if (engine.runStartup())
			{
				if (null != engine.successMessage)
				{
					setStyle(successStyle);
					writeLine(engine.successMessage);
				}
				populate();
			}
			else
			{
				engine.restoreAll();
				if (null != engine.failureMessage)
				{
					setStyle(errorStyle);
					writeLine(engine.failureMessage);
					writeLine();
				}
				else beep();
			}
		}
		catch (Exception e)
		{
			if (e instanceof NullPointerException) e.printStackTrace();

			if (engine.runAvatarCommonFailure())
			{
				if (0 != e.getMessage().length())
				{
					setStyle(errorStyle);
					writeLine(e.getMessage());
					writeLine();
				}
			}
			else writeLine();

			engine.restoreAll();
		}

		cutHugeCentralText();
	}

	private void onReadState()
	{
		clearMessages();
		engine.backupAll();

		try
		{
			if (engine.runReadState())
			{
				if (null != engine.successMessage)
				{
					setStyle(successStyle);
					writeLine(engine.successMessage);
				}
				populate();
			}
			else
			{
				engine.restoreAll();
				if (null != engine.failureMessage)
				{
					setStyle(errorStyle);
					writeLine(engine.failureMessage);
					writeLine();
				}
				else beep();
			}
		}
		catch (Exception e)
		{
			if (e instanceof NullPointerException) e.printStackTrace();

			if (engine.runAvatarCommonFailure())
			{
				if (0 != e.getMessage().length())
				{
					setStyle(errorStyle);
					writeLine(e.getMessage());
					writeLine();
				}
			}
			else writeLine();

			engine.restoreAll();
		}

		cutHugeCentralText();
	}

	private void onInputSubmit()
	{
		if (engine.queryMode())
			executeQuery(inputLine.getText());
		else
			executeCommand(inputLine.getText());

		inputLine.setText("");
	}

	private void onInputCancel()
	{
		// Just clear the input line
		inputLine.setText("");
	}


	// Two alternatives of actions taken after command line submit

	private void executeQuery(String queryLine)
	{
		historyPutLine(queryLine);

		clearMessages();
		engine.backupAll();

		try
		{
			if (engine.executeQuery(queryLine))
			{
				if (null != engine.successMessage)
				{
					setStyle(successStyle);
					writeLine(engine.successMessage);
				}
				populate();
			}
			else
			{
				engine.restoreAll();
				if (null != engine.failureMessage)
				{
					setStyle(errorStyle);
					writeLine(engine.failureMessage);
				}
			}
		}
		catch (Exception e)
		{
			if (e instanceof NullPointerException) e.printStackTrace();

			if (!engine.runAvatarCommonFailure())
			{
				setStyle(errorStyle);
				writeLine(e.getMessage());
			}

			engine.restoreAll();
		}

		cutHugeCentralText();
		writeLine();
	}

	private void executeCommand(String commandLine)
	{
		if (!commandLine.isEmpty())
		{
			historyPutLine(commandLine);

			setStyle(commandStyle);
			writeLine(commandLine);

			resetMessages();
			engine.backupAll();

			try
			{
				if (engine.executeCommand(commandLine))
				{
					if (null != engine.successMessage)
					{
						setStyle(successStyle);
						writeLine(engine.successMessage);
					}
					populate();
				}
				else
				{
					engine.restoreAll();
					if (null != engine.failureMessage)
					{
						setStyle(errorStyle);
						writeLine(engine.failureMessage);
					}
				}
			}
			catch (Exception e)
			{
				if (e instanceof NullPointerException) e.printStackTrace();

				if (!engine.runAvatarCommonFailure())
				{
					setStyle(errorStyle);
					writeLine(e.getMessage());
				}

				engine.restoreAll();
			}

			cutHugeCentralText();
			writeLine();
		}
	}


	// Command line history

	private void historyPutLine(String commandLine)
	{
		if (engine.queryMode()) return;

		historyIndex = -1;
		historyEditedLine = null;

		for (String historyCommandLine : historyCommandLines)
		{
			if (historyCommandLine.equals(commandLine))
			{
				historyCommandLines.remove(historyCommandLine);
				historyCommandLines.insertElementAt(historyCommandLine, 0);
				return;
			}
		}
		historyCommandLines.insertElementAt(commandLine, 0);
	}

	private void previousCommand()
	{
		if (engine.queryMode()) return;

		if (historyIndex >= historyCommandLines.size() - 1)
		{
			beep();
			return;
		}

		if (historyIndex < 0)
			historyEditedLine = inputLine.getText();

		++historyIndex;
		inputLine.setText(historyCommandLines.elementAt(historyIndex));
	}

	private void nextCommand()
	{
		if (engine.queryMode()) return;

		if (historyIndex < 0)
		{
			beep();
			return;
		}

		--historyIndex;

		if (historyIndex < 0)
		{
			inputLine.setText(historyEditedLine);
			historyEditedLine = null;
			return;
		}
		else
		{
			inputLine.setText(historyCommandLines.elementAt(historyIndex));
		}
	}

	private void firstCommand()
	{
		if (engine.queryMode()) return;

		if (historyIndex >= historyCommandLines.size() - 1)
		{
			beep();
			return;
		}

		if (historyIndex < 0)
			historyEditedLine = inputLine.getText();

		historyIndex = historyCommandLines.size() - 1;
		inputLine.setText(historyCommandLines.elementAt(historyIndex));
	}

	private void lastCommand()
	{
		if (engine.queryMode()) return;

		if (historyIndex < 0)
		{
			beep();
			return;
		}

		historyIndex = -1;
		inputLine.setText(historyEditedLine);
		historyEditedLine = null;
	}


	// Miscellaneous

	public void parseDefinition()
	{
		try
		{
			engine.parseDefinition(worldName);
			engine.saveDefaultAll();
		}
		catch (Exception e)
		{
			if (e instanceof NullPointerException) e.printStackTrace();
			setStyle(errorStyle);
			writeLine(e.getMessage());
			writeLine();
		}
	}

	public void initializeContextMenus()
	{
		MenuItem item; Entity commandEntity, operatorEntity;


		// Locations (common context menu)

		commandEntity = engine.getCommandEntity("walk");
		item = locationsContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		locationsMenuItems.add(item);

		commandEntity = engine.getCommandEntity("explore");
		item = locationsContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		locationsMenuItems.add(item);


		// Items lying free at some location

		commandEntity = engine.getCommandEntity("pick");
		item = itemsContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		itemsMenuItems.add(item);

		commandEntity = engine.getCommandEntity("examine");
		item = itemsContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		itemsMenuItems.add(item);

		commandEntity = engine.getCommandEntity("use");
		item = itemsContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		itemsMenuItems.add(item);

		if (engine.isCommandDefined("action"))
		{
			commandEntity = engine.getCommandEntity("action");
			item = itemsContextMenu.addItem(commandEntity.toString());
			item.setCommand(commandEntity);
			itemsMenuItems.add(item);
		}


		// Items in inventory

		commandEntity = engine.getCommandEntity("drop");
		item = inventoryContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		inventoryMenuItems.add(item);

		commandEntity = engine.getCommandEntity("throw");
		item = inventoryContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		inventoryMenuItems.add(item);

		commandEntity = engine.getCommandEntity("examine");
		item = inventoryContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		inventoryMenuItems.add(item);

		commandEntity = engine.getCommandEntity("use");
		item = inventoryContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		inventoryMenuItems.add(item);

		if (engine.isCommandDefined("action"))
		{
			commandEntity = engine.getCommandEntity("action");
			item = inventoryContextMenu.addItem(commandEntity.toString());
			item.setCommand(commandEntity);
			inventoryMenuItems.add(item);
		}


		// Items inserted in some other item

		commandEntity = engine.getCommandEntity("remove");
		operatorEntity = engine.getOperatorEntity("remove");
		item = insertedContextMenu.addItem(
			commandEntity.toString()
			// + "… " + operatorEntity.toString() + "…"
			);
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		insertedMenuItems.add(item);

		commandEntity = engine.getCommandEntity("examine");
		item = insertedContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		insertedMenuItems.add(item);

		commandEntity = engine.getCommandEntity("use");
		item = insertedContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		insertedMenuItems.add(item);

		if (engine.isCommandDefined("action"))
		{
			commandEntity = engine.getCommandEntity("action");
			item = insertedContextMenu.addItem(commandEntity.toString());
			item.setCommand(commandEntity);
			insertedMenuItems.add(item);
		}

		/*
		commandEntity = engine.getCommandEntity("remove");
		item = insertedContextMenu.addItem(commandEntity.toString());
		item.setCommand(commandEntity);
		insertedMenuItems.add(item);
		*/


		// Drag and Drop item on location and vice versa

		commandEntity = engine.getCommandEntity("walk");
		operatorEntity = engine.getOperatorEntity("walk");
		item = dragDropLocationContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropLocationMenuItems.add(item);

		commandEntity = engine.getCommandEntity("explore");
		operatorEntity = engine.getOperatorEntity("explore");
		item = dragDropLocationContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropLocationMenuItems.add(item);


		// Drag and Drop item on item

		commandEntity = engine.getCommandEntity("pick");
		operatorEntity = engine.getOperatorEntity("pick");
		item = dragDropItemContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropItemMenuItems.add(item);

		commandEntity = engine.getCommandEntity("drop");
		operatorEntity = engine.getOperatorEntity("drop");
		item = dragDropItemContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropItemMenuItems.add(item);

		dragDropItemContextMenu.addSeparator();

		commandEntity = engine.getCommandEntity("insert");
		operatorEntity = engine.getOperatorEntity("insert");
		item = dragDropItemContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropItemMenuItems.add(item);

		dragDropItemContextMenu.addSeparator();

		commandEntity = engine.getCommandEntity("examine");
		operatorEntity = engine.getOperatorEntity("examine");
		item = dragDropItemContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropItemMenuItems.add(item);

		commandEntity = engine.getCommandEntity("use");
		operatorEntity = engine.getOperatorEntity("use");
		item = dragDropItemContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropItemMenuItems.add(item);

		if (engine.isCommandDefined("action"))
		{
			commandEntity = engine.getCommandEntity("action");
			operatorEntity = engine.getOperatorEntity("action");
			item = dragDropItemContextMenu.addItem(
				commandEntity.toString() + "… " +
				operatorEntity.toString() + "…");
			item.setCommand(commandEntity);
			item.setOperator(operatorEntity);
			dragDropItemMenuItems.add(item);
		}

		dragDropItemContextMenu.addSeparator();

		commandEntity = engine.getCommandEntity("throw");
		operatorEntity = engine.getOperatorEntity("throw");
		item = dragDropItemContextMenu.addItem(
			commandEntity.toString() + "… " +
			operatorEntity.toString() + "…");
		item.setCommand(commandEntity);
		item.setOperator(operatorEntity);
		dragDropItemMenuItems.add(item);
	}


	// Access to lists

	public ScrollList locationsList()
	{
		return locationsList;
	}

	public ScrollList itemsList()
	{
		return itemsList;
	}

	public ScrollList inventoryList()
	{
		return inventoryList;
	}

	public ScrollList insertedList()
	{
		return insertedList;
	}

	public void reorderListsPane()
	{
		GridLayout grid = (GridLayout)rightPane.getLayout();
		rightPane.removeAll();

		int rows = (locationsList.isVisible() ? 1 : 0) +
			(itemsList.isVisible() ? 1 : 0) +
			(inventoryList.isVisible() ? 1 : 0) +
			(insertedList.isVisible() ? 1 : 0);

		if (0 == rows) rightPane.setVisible(false); else
		{
			grid.setRows(rows);
			rightPane.setVisible(true);
			if (locationsList.isVisible())
				rightPane.add(locationsList);
			if (itemsList.isVisible())
				rightPane.add(itemsList);
			if (inventoryList.isVisible())
				rightPane.add(inventoryList);
			if (insertedList.isVisible())
				rightPane.add(insertedList);
		}
	}


	// Autopopulate property and method

	private boolean autopopulate = true;

	public boolean autopopulateEnabled()
	{
		return autopopulate;
	}

	public void enableAutopopulate()
	{
		autopopulate = true;
	}

	public void disableAutopopulate()
	{
		autopopulate = false;
	}

	public void populate()
	{
		if (autopopulate)
		{
			// if (locationsList.isVisible())
			engine.populateLocations(locationsList);

			// if (itemsList.isVisible())
			engine.populateItems(itemsList);

			// if (inventoryList.isVisible())
			engine.populateInventory(inventoryList);

			// if (insertedList.isVisible())
			// engine.populateInserted(insertedList);
		}
		else
		{
			locationsList.model.clear();
			itemsList.model.clear();
			inventoryList.model.clear();
			// insertedList.model.clear();
		}

		engine.runPopulate();
	}


	// Access to some controls

	public void setIcon(String iconName)
	{
		try
		{
			// #disabled# // if (isMacOS)
			// #disabled# // 	macAdapter.setIcon(iconName);
			// #disabled# // else
				this.setIconImage(fileToImage(iconName));
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}
	}

	@Override public void setTitle(String title)
	{
		if (null == title)
			super.setTitle(defaultTitle);
		else
			super.setTitle(title);
	}

	@Override public String getTitle()
	{
		String title = super.getTitle();
		if (defaultTitle.equals(title)) return null;
		return title;
	}

	public void setCentralTextLabel(String label)
	{
		if (null == label)
			centralTextLabel.setVisible(false);
		else
		{
			centralTextLabel.setText(label);
			centralTextLabel.setVisible(true);
		}
	}

	public String getCentralTextLabel()
	{
		if (!centralTextLabel.isVisible()) return null;
		return centralTextLabel.getText();
	}


	public void setInputLineLabel(String label)
	{
		if (null == label)
			inputLineLabel.setVisible(false);
		else
		{
			inputLineLabel.setText(label);
			inputLineLabel.setVisible(true);
		}
	}

	public String getInputLineLabel()
	{
		if (!inputLineLabel.isVisible()) return null;
		return inputLineLabel.getText();
	}


	// Styles kit

	private void setupDefaultStyles()
	{
		StyleConstants.setForeground(infoStyle, new Color(0, 0, 220));
		StyleConstants.setForeground(errorStyle, new Color(200, 0, 0));
		StyleConstants.setForeground(warningStyle, new Color(240, 160, 0));
		StyleConstants.setForeground(commandStyle, new Color(120, 0, 120));
		StyleConstants.setForeground(successStyle, new Color(0, 100, 0));

		StyleConstants.setForeground(newStyle("note"),
			new Color(160, 160, 160));
		StyleConstants.setForeground(newStyle("teal"),
			new Color(0, 128, 128));
		StyleConstants.setForeground(newStyle("aqua"),
			new Color(72, 209, 204));
		StyleConstants.setForeground(newStyle("gold"),
			new Color(210, 160, 50));
		StyleConstants.setForeground(newStyle("wood"),
			new Color(100, 50, 0));
		StyleConstants.setForeground(newStyle("description"),
			new Color(60, 120, 250));
	}

	public void clearStyle()
	{
		currentStyle = null;
	}

	public SimpleAttributeSet newStyle(String styleName)
	{
		if (styleName.equals("none")) return null;
		SimpleAttributeSet newStyle = styles.get(styleName);
		if (null == newStyle)
		{
			newStyle = new SimpleAttributeSet();
			styles.put(styleName, newStyle);
		}
		return newStyle;
	}

	public SimpleAttributeSet getStyle(String styleName)
	{
		if (styleName.equals("none")) return null;
		return styles.get(styleName);
	}

	public SimpleAttributeSet getErrorStyle()
	{ return errorStyle; }

	public SimpleAttributeSet getSuccessStyle()
	{ return successStyle; }

	public void setStyle(SimpleAttributeSet style)
	{
		currentStyle = style;
	}

	public void setStyle(String styleName)
	{
		if (styleName.equals("none")) currentStyle = null;
		else currentStyle = styles.get(styleName);
	}

	public int dumpStyles()
	{
		boolean first = true; int count = 0;
		for (String styleName : styles.keySet())
		{
			if (first) first = false; else write(", ");
			write(styleName); ++count;
		}
		writeLine(".");
		return count;
	}


	// Writing texts

	public void write(Object... arguments)
	{
		for (Object argument : arguments)
			try
			{
				// centralTextArea.append(argument.toString());
				if (null != argument)
					centralTextDocument.insertString(
						centralTextDocument.getLength(),
						argument.toString(), currentStyle);
			}
			catch (Exception e)
			{
				DriverOutput.printError(e);
			}
	}

	private final static int cutTextSize = 2000000;
	public void cutHugeCentralText()
	{
		// Here we have a good oportunity to flush debug texts…
		engine.debugFlush();

		if (centralTextDocument.getLength() > 2 * cutTextSize)
			try
			{
				int cut = centralTextDocument.getLength() - cutTextSize;
				String text = centralTextDocument.getText(cut, cutTextSize);

				int i;
				for (i = 0; i < text.length(); ++i)
					if ('\n' == text.charAt(i)) break; else ++cut;
				for (; i < text.length(); ++i)
					if ('\n' != text.charAt(i)) break; else ++cut;
				centralTextDocument.remove(0, cut);

				DefaultCaret caret = (DefaultCaret)centralTextArea.getCaret();
				caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			}
			catch (Exception e)
			{
				DriverOutput.printError(e);
			}
	}

	private int textDocumentMark = -1;

	public void saveMark()
	{
		textDocumentMark = centralTextDocument.getLength();
	}

	public void clearMark()
	{
		textDocumentMark = -1;
	}

	public void clearFromMark()
	{
		if (-1 != textDocumentMark)
		{
			try
			{
				centralTextDocument.remove(textDocumentMark,
					centralTextDocument.getLength() - textDocumentMark);
				textDocumentMark = -1;
			}
			catch (Exception e)
			{
				DriverOutput.printError(e);
			}
		}
	}

	public void clearScreen()
	{
		try
		{
			centralTextDocument.remove(0, centralTextDocument.getLength());
			DefaultCaret caret = (DefaultCaret)centralTextArea.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}
	}

	public void insertIcon(String filename)
	{
		try
		{
			centralTextArea.insertIcon(fileToIcon(filename));
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
			setStyle(errorStyle);
			writeLine(e.getMessage());
		}
	}

	public String parseMessage(String templateName, Object... arguments)
	{ return currentLanguage.parseMessage(templateName, arguments); }

	public void parseAndWriteWarning(String templateName, Object... arguments)
	{
		setStyle(warningStyle);
		writeLine(parseMessage(templateName, arguments));
	}

	public void writeLine(Object... arguments)
	{
		// try { if (null != writeLineTrace)
		// throw new Exception(writeLineTrace); }
		// catch (Exception e) { e.printStackTrace();
		// writeLineTrace = null; for (Object argument : arguments)
		// out.print(argument.toString()); out.println(); }

		write(arguments);
		try
		{
			// centralTextArea.append("\n");
			centralTextDocument.insertString(
				centralTextDocument.getLength(), "\n", null);

			// New line cancels the selection:
			centralTextArea.setCaretPosition(
				centralTextArea.getDocument().getLength());
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}
		clearStyle();
	}


	//// ∙∙∙
	// The static stuff…
	//// ∙∙∙

	private static String OS = getProperty("os.name").toLowerCase();
	// -old-privatestaticboolean isMacOS = getProperty("mrj.version") != null;
	// #disabled# // private static boolean isMacOS = OS.indexOf("mac") >= 0;
	// private static String imagesPath = "";
	private final static Vector<String>
		listOfImageFileNames = new Vector<String>();
	private final static Vector<Image> listOfImages = new Vector<Image>();
	private final static Vector<Icon> listOfIcons = new Vector<Icon>();

	public static Icon fileToIcon(String filename)
	{
		for (int i = 0; i < 2; ++i)
		{
			int indexOf;
			if (-1 != (indexOf = listOfImageFileNames.indexOf(filename)))
			{
				if (null == listOfIcons.elementAt(indexOf))
				{
					listOfIcons.setElementAt(
						new ImageIcon(listOfImages.
							elementAt(indexOf)), indexOf);
				}
				return listOfIcons.elementAt(indexOf);
			}

			fileToImage(filename);
		}
		return null;
	}

	public static Image fileToImage(String filename)
	{
		// Search the image in the internal list
		int indexOf;
		if (-1 != (indexOf = listOfImageFileNames.indexOf(filename)))
			return listOfImages.elementAt(indexOf);

		// If it is not in the list, read it from the file
		URL url = null;

		try
		{
			File imageFile = new File(/*imagesPath + */filename);
			if (imageFile.canRead()) url = imageFile.toURI().toURL();
		}
		// catch (MalformedURLException e)
		catch (Exception e) { DriverOutput.printError(e); }

		if (null == url)
		{
			// Read it from URL
			try { url = new URL(filename); }
			catch (Exception e) { DriverOutput.printError(e); }
		}

		// Check it in the .jar file
		if (null == url) url = TextDriver.class.getResource(
			(/*imagesPath + */filename).replace('\\', '/'));
		if (null == url)
		{
			if (null == textDriver || null == textDriver.engine)
				throw new RuntimeException("Image “" + filename +
					"” was not not found!" );
			else
				textDriver.engine.throwErrorMessage(
					"errorImageNotFound", filename);
		}

		BufferedImage image = null;

		try { image = ImageIO.read(url); }
		catch (Exception e) { DriverOutput.printError(e); return null; }

		listOfImageFileNames.add(filename);
		listOfImages.add(image);
		listOfIcons.add(null);

		return image;
	}


	public static int parseKeyConstant(String mnemonic, int defaultValue)
	{
		try
		{
			mnemonic = mnemonic.trim().toUpperCase();
			if (!mnemonic.startsWith("VK_")) mnemonic = "VK_" + mnemonic;
			return KeyEvent.class.getDeclaredField(mnemonic).getInt(null);
		}
		catch (Exception e)
		{
			DriverOutput.printError(e);
		}

		return defaultValue;
	}

	public static void beep()
	{ Toolkit.getDefaultToolkit().beep(); }

	public static TextDriver textDriver;
	// #disabled# // public static DriverApplicationAdapter macAdapter;
	// public static String writeLineTrace = null;

	public static void main(String[] args)
	{
		boolean enableDebug = false;

		for (String arg : args)
		{
			arg = arg.toLowerCase();
			if (arg.equals("-help"))
			{
				out.println("Help request detected.");
				out.println();
				out.println("Command line arguments:");
				out.println("  -help    – this help");
				out.println("  -debug   – enable debug mode");
				out.println("  -param:* – the * means any word that will be passed to startup script");
				out.println();
				out.println("In debug mode following special commands are available:");
				out.println("  locations – write list of locations defined");
				out.println("  items     – write list of items defined");
				out.println("  triggers  – write list of triggers defined");
				out.println("  tokens    – write all defined tokens");
				out.println("  aliases   – write all defined aliases");
				out.println("  styles    – write list of styles defined");
				out.println("  dump      – write details about specified location, item, trigger, and/or style");
				out.println("  tokenize  – tokenize following string and write the result");
				out.println();
				exit(0);
			}
			else if (arg.equals("-debug")) enableDebug = true;
		}

		// #disabled# // if (isMacOS)
		// #disabled# // {
		// #disabled# // 	macAdapter = new DriverApplicationAdapter();
		// #disabled# // 	textDriver = new TextDriver();
		// #disabled# // 	macAdapter.setTextDriverClass(textDriver);
		// #disabled# // }
		// #disabled# // // else if (namespaceOS.equals("Windows"))
		// #disabled# // // {
		// #disabled# // // 	textDriver = new TextDriver();
		// #disabled# // // }
		// #disabled# // else
		{
			textDriver = new TextDriver();
		}

		DriverOutput.textDriver = textDriver;

		// textDriver.addDefaultMenuSeparator();
		// textDriver.addDefaultAboutItem();
		// textDriver.addDefaultExitItem();

		if (enableDebug)
		{
			out.println("Debug mode enabled.");
			textDriver.engine.enableDebugMode();
		}

		textDriver.setIcon(textDriver.applicationIcon);
		textDriver.parseDefinition();
		textDriver.initializeContextMenus();
		// writeLineTrace = "Before startup";
		textDriver.onStartup(args);
		// writeLineTrace = "After startup";
		textDriver.autoLoadState();
		textDriver.setVisible(true);
		textDriver.inputLine.requestFocus();
	}
}
