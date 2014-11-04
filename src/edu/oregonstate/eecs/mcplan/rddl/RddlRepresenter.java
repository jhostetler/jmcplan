/**
 * 
 */
package edu.oregonstate.eecs.mcplan.rddl;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class RddlRepresenter implements FactoredRepresenter<RDDLState, FactoredRepresentation<RDDLState>>
{
	private final RddlSpec spec;
	private final ArrayList<Attribute> attributes;
	
	public RddlRepresenter( final RddlSpec spec )
	{
		this.spec = spec;
		attributes = new ArrayList<Attribute>();
		for( final String name : spec.state_var_names ) {
			attributes.add( new Attribute( name ) );
		}
	}
	
	private RddlRepresenter( final RddlRepresenter that )
	{
		this.spec = that.spec;
		this.attributes = that.attributes;
	}
	
	@Override
	public RddlRepresenter create()
	{
		return new RddlRepresenter( this );
	}

	@Override
	public RddlRepresentation encode( final RDDLState s )
	{
		return new RddlRepresentation( spec, s );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes;
	}
}
