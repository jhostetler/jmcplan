/**
 * 
 */
package edu.oregonstate.eecs.mcplan.graph;

/**
 * @author jhostetler
 *
 */
public class Edge<V>
{
	private final V src_;
	private final V target_;
	
	public Edge( final V src, final V target )
	{
		src_ = src;
		target_ = target;
	}
	
	public V source()
	{
		return src_;
	}
	
	public V target()
	{
		return target_;
	}
	
	@Override
	public int hashCode()
	{
		int h = 23;
		final int k = 43;
		h = h*k + src_.hashCode();
		h = h*k + target_.hashCode();
		return h;
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		if( obj == null || !(obj instanceof Edge<?>) ) {
			return false;
		}
		final Edge<?> that = (Edge<?>) obj;
		return src_.equals( that.src_ ) && target_.equals( that.target_ );
	}
	
	@Override
	public String toString()
	{
		return "--(" + src_.toString() + ", " + target_.toString() + ")->";
	}
}
