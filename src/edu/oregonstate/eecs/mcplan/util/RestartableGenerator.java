/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public abstract class RestartableGenerator<T> extends Generator<T>
{
	public abstract void restart();
}
