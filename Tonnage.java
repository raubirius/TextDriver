
/*#
 * This is part of the Text Driver project. See TextDriver.java for version
 * information and changes.
 */

/**
 * This interface expresses maximum tonnage of items or locations.
 */

public interface Tonnage
{
	public void setTonnage(double value);
	public double getTonnage();

	public void addSpentTonnage(double value);
	public void removeSpentTonnage(double value);
	public void setSpentTonnage(double value);
	public double getSpentTonnage();
}
