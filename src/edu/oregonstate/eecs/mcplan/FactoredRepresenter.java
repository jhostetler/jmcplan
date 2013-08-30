/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;

import weka.core.Attribute;

/**
 * Generates FactoredRepresentations.
 */
public interface FactoredRepresenter<S, F extends FactoredRepresenter<S, F>>
	extends Representer<S, F>
{
	@Override
	public abstract FactoredRepresentation<S, F> encode( final S s );
	
	// FIXME: Remove Weka dependency
	public abstract ArrayList<Attribute> attributes();
}
