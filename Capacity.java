
/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * This interface expresses maximum volume capacity of items or locations.
 */

public interface Capacity
{
	public void setCapacity(double value);
	public double getCapacity();

	public void addSpentCapacity(double value);
	public void removeSpentCapacity(double value);
	public void setSpentCapacity(double value);
	public double getSpentCapacity();
}
