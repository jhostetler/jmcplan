/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;

import weka.core.Attribute;

/**
 * Generates FactoredRepresentations.
 */
public interface FactoredRepresenter<S, X extends FactoredRepresentation<S>>
	extends Representer<S, X>
{
	@Override
	public abstract FactoredRepresenter<S, X> create();
	
	@Override
	public abstract X encode( final S s );
	
	// FIXME: Remove Weka dependency
	public abstract ArrayList<Attribute> attributes();
}
