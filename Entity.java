
/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * This is fundamental class in the class hierarchy of this project.
 * See Holder, Language.
 */

public class Entity implements Comparable<Entity>
{
	public static TextEngine engine = null;

	// The name is public final field.
	// (That means it is in fact constant.)
	public final String name;
	private final String hashName;

	public Entity(String name)
	{
		// Do not modify the name here‼
		this.name = name;
		this.hashName = name.toLowerCase();
	}

	public String getName() // This is useful in replaceProperties(…)
	{
		return name;
	}

	@Override public int compareTo(Entity entity)
	{
		return this.hashName.compareTo(entity.hashName);
	}

	@Override public boolean equals(Object obj)
	{
		if (obj instanceof Entity)
			return hashName.equals(((Entity)obj).hashName);
		if (obj instanceof String)
			return hashName.equals(((String)obj).toLowerCase());
		return hashName.equals(obj.toString().toLowerCase());
	}

	@Override public int hashCode()
	{
		// return super.hashCode();
		return hashName.hashCode();
	}

	@Override public String toString()
	{
		return name;
	}
}
