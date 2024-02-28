
import java.io.IOException;

import java.util.Vector;

import bookcase.TextFile;

/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * Location is any place in the world.
 */

public class Location extends Container
{
	public final Vector<String> onReveal = new Vector<String>();
	public final Vector<String> onConceal = new Vector<String>();
	public final Vector<String> onEnter = new Vector<String>();
	public final Vector<String> onLeave = new Vector<String>();
	public final Vector<String> onExplore = new Vector<String>();

	private final Vector<Transition> transitions = new Vector<Transition>();

	private boolean visited = false, oldVisitation = false,
		defaultVisitation = false;

	public Location(String name)
	{
		super(name);
	}


	public boolean canTake(Entry entry)
	{
		if (0 == getTonnage() && 0 == getCapacity()) return true;
		return super.canTake(entry);
	}

	public boolean canBear(Entry entry)
	{
		if (0 == getTonnage()) return true;
		return super.canBear(entry);
	}

	public boolean canFit(Entry entry)
	{
		if (0 == getCapacity()) return true;
		return super.canFit(entry);
	}


	public Transition connectWith(Location target)
	{
		for (Transition search : transitions)
			if (search.target == target) return search;

		Transition transition = new Transition(target);
		transitions.add(transition);
		return transition;
	}

	public boolean isConnectedWith(Location target)
	{
		for (Transition search : transitions)
			if (search.target == target) return true;
		return false;
	}

	public Transition getTransition(Location target)
	{
		for (Transition search : transitions)
			if (search.target == target) return search;
		return null;
	}

	public Transition getTransition(String target)
	{
		for (Transition search : transitions)
			if (search.target.equals(target)) return search;
		return null;
	}

	public Vector<Transition> getTransitions()
	{
		return transitions;
	}


	public void reveal() { show(); }
	public void conceal() { hide(); }


	/*
	// This stuff is made on another place…
	@Override public void show()
	{
		// invokeScript(onReveal);
		super.show();
	}

	@Override public void hide()
	{
		// invokeScript(onConceal);
		super.hide();
	}
	*/

	// This method must be named „is“ Visited to be consistent with isVisible
	// method – both methods are used in TextEngine.replaceProperties…
	public boolean isVisited()
	{
		return visited;
	}

	public void setVisited()
	{
		visited = true;
	}

	public void clearVisited()
	{
		visited = false;
	}


	@Override public void backupProperties()
	{
		for (Transition transition : transitions)
			transition.backupProperties();
		oldVisitation = visited;
		super.backupProperties();
	}

	@Override public void restoreProperties()
	{
		for (Transition transition : transitions)
			transition.restoreProperties();

		if (visited != oldVisitation)
		{
			DriverOutput.debugMessage(this, "restore visited from",
				visited, "to", oldVisitation);
			visited = oldVisitation;
		}

		super.restoreProperties();
	}


	@Override public void saveDefaultProperties()
	{
		for (Transition transition : transitions)
			transition.saveDefaultProperties();
		defaultVisitation = visited;
		super.saveDefaultProperties();
	}

	@Override public void resetProperties()
	{
		for (Transition transition : transitions)
			transition.resetProperties();
		visited = defaultVisitation;
		super.resetProperties();
	}


	@Override public void writeProperties(TextFile file) throws IOException
	{
		super.writeProperties(file);

		if (visited != defaultVisitation)
		{
			file.write("    Visited: ");
			file.writeLine(String.valueOf(visited));
		}

		for (Transition transition : transitions)
		{
			file.writeLine();
			file.write("Transition: ");
			file.writeLine(originalName);
			transition.writeProperties(file);
		}
	}

	@Override public boolean processProperty(String line)
	{
		if (super.processProperty(line)) return true;

		if (Holder.startsWith(line, "Visited: "))
		{
			visited = Boolean.parseBoolean(Holder.getParamString());
			return true;
		}

		return false;
	}
}
