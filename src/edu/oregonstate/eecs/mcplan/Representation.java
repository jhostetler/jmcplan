/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * An abstract representation of a set S. Two Representation objects should
 * compare equal if and only if they are both the image of exactly the same
 * subset of S under the representation function.
 */
public abstract class Representation<S>
{
	public abstract Representation<S> copy();
	
	@Override
	public abstract boolean equals( final Object obj );
	
	@Override
	public abstract int hashCode();
}
