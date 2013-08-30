/**
 * 
 */
package edu.oregonstate.eecs.mcplan.experiments;

import java.io.PrintStream;

/**
 * @author jhostetler
 *
 */
public interface CsvEntry
{
	/**
	 * Writes the object as an entry in a .csv file. The representation must
	 * *not* contain commas (',') or newlines ('\n').
	 * @param out
	 */
	public abstract void writeEntry( final PrintStream out );
}
