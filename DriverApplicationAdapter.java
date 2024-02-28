
public class DriverApplicationAdapter {}

// #disabled# // import static java.lang.System.out;
// #disabled# // 
// #disabled# // // This source code contains repaired deprecations.
// #disabled# // 
// #disabled# // import com.apple.eawt.AboutHandler;
// #disabled# // import com.apple.eawt.AppEvent;
// #disabled# // import com.apple.eawt.Application;
// #disabled# // import com.apple.eawt.QuitHandler;
// #disabled# // import com.apple.eawt.QuitResponse;
// #disabled# // // import com.apple.eawt.ApplicationAdapter;
// #disabled# // // import com.apple.eawt.ApplicationEvent;
// #disabled# // 
// #disabled# // /*#
// #disabled# //  * This is part of the Text Driver project. See TextDriver.java for version
// #disabled# //  * information and changes.
// #disabled# //  */
// #disabled# // 
// #disabled# // /**
// #disabled# //  * This is an adapter class specific for Mac OS. It needs Apple EAWT package
// #disabled# //  * which is part of each JRE distribution for Mac. It can be found in
// #disabled# //  * «Java_Home»/jre/lib/ floder, e.g.:
// #disabled# //  * /Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/jre/lib/rt.jar
// #disabled# //  * (The library contains more than the package.)
// #disabled# //  *
// #disabled# //  * To get my projects compilable also on Windows, I am always making a copy of
// #disabled# //  * the file and putting it into the classpath of each project, e.g:
// #disabled# //  * «my_path»/rt-jre1.8.0_66.jar
// #disabled# //  *
// #disabled# //  * (The EAWT package does not work on Windows, of course, but the project is
// #disabled# //  * compilable and the initialisation is always branched depending on current
// #disabled# //  * OS.)
// #disabled# //  */
// #disabled# // public class DriverApplicationAdapter // extends // ApplicationAdapter
// #disabled# // 	implements QuitHandler, AboutHandler
// #disabled# // {
// #disabled# // 	private TextDriver textDriverClass = null;
// #disabled# // 	private Application macApplication = null;
// #disabled# // 
// #disabled# // 	public DriverApplicationAdapter()
// #disabled# // 	{
// #disabled# // 		// System.setProperty("apple.laf.useScreenMenuBar", "true");
// #disabled# // 
// #disabled# // 		/*System.setProperty(
// #disabled# // 			"com.apple.mrj.application.apple.menu.about.name",
// #disabled# // 			"About text");*/
// #disabled# // 
// #disabled# // 		out.println("Setting Max OS specific preferences.");
// #disabled# // 
// #disabled# // 		macApplication = Application.getApplication();
// #disabled# // 		// macApplication.addApplicationListener(this);
// #disabled# // 		macApplication.setQuitHandler(this);
// #disabled# // 
// #disabled# // 		// Repaired deprecations:
// #disabled# // 
// #disabled# // 		// macApplication.setEnabledPreferencesMenu(false);
// #disabled# // 		// macApplication.removePreferencesMenuItem();
// #disabled# // 		// macApplication.setPreferencesHandler(null);
// #disabled# // 
// #disabled# // 		// macApplication.setEnabledAboutMenu(false);
// #disabled# // 		// macApplication.removeAboutMenuItem();
// #disabled# // 		macApplication.setAboutHandler(this);
// #disabled# // 	}
// #disabled# // 
// #disabled# // 	public void setTextDriverClass(TextDriver textDriverClass)
// #disabled# // 	{
// #disabled# // 		this.textDriverClass = textDriverClass;
// #disabled# // 	}
// #disabled# // 
// #disabled# // 	public void setIcon(String iconName)
// #disabled# // 	{
// #disabled# // 		macApplication.setDockIconImage(
// #disabled# // 			TextDriver.fileToImage(iconName));
// #disabled# // 	}
// #disabled# // 
// #disabled# // 	// @Override public void handleQuit(ApplicationEvent e)
// #disabled# // 	@Override public void handleQuitRequestWith(
// #disabled# // 		AppEvent.QuitEvent e, QuitResponse response)
// #disabled# // 	{
// #disabled# // 		response.cancelQuit();
// #disabled# // 		// e.setHandled(true);
// #disabled# // 		textDriverClass.closeApplication();
// #disabled# // 		// response.performQuit();
// #disabled# // 	}
// #disabled# // 
// #disabled# // 	@Override public void handleAbout(AppEvent.AboutEvent e)
// #disabled# // 	{
// #disabled# // 		textDriverClass.aboutApplication();
// #disabled# // 	}
// #disabled# // 
// #disabled# // 	// @Override public void handleAbout(ApplicationEvent e)
// #disabled# // 	// {
// #disabled# // 	// 	e.setHandled(true);
// #disabled# // 	// 	// textDriverClass.about();
// #disabled# // 	// }
// #disabled# // 
// #disabled# // 	// @Override public void handlePreferences(ApplicationEvent e)
// #disabled# // 	// {
// #disabled# // 	// 	e.setHandled(true);
// #disabled# // 	// 	// textDriverClass.preferences();
// #disabled# // 	// }
// #disabled# // }
