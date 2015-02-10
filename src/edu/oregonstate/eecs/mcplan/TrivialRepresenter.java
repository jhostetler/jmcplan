/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;

import weka.core.Attribute;

/**
 * @author jhostetler
 *
 */
public class TrivialRepresenter<S> implements FactoredRepresenter<S, TrivialRepresentation<S>>
{
	private static final ArrayList<Attribute> attributes;
	
	static {
		attributes = new ArrayList<Attribute>();
		attributes.add( new Attribute( "trivial" ) );
	}
	
	@Override
	public TrivialRepresenter<S> create()
	{
		return new TrivialRepresenter<S>();
	}

	@Override
	public TrivialRepresentation<S> encode( final S s )
	{
		return new TrivialRepresentation<S>();
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
