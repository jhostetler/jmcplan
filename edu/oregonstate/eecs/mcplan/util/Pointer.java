/**
 * 
 */
package edu.oregonstate.eecs.mcplan.util;

/**
 * @author jhostetler
 *
 */
public class Pointer<T>
{
	private T t_;
	
	public Pointer()
	{
		t_ = null;
	}
	
	public Pointer( final T t )
	{
		t_ = t;
	}
	
	public T get()
	{
		return t_;
	}
	
	public void set( final T t )
	{
		t_ = t;
	}
}
