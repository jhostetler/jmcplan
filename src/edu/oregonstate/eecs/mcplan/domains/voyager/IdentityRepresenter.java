/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;

/**
 * @author jhostetler
 *
 */
public class IdentityRepresenter implements FactoredRepresenter<VoyagerState, VoyagerStateToken>
{
	private final ArrayList<Attribute> attributes_;
	
	public IdentityRepresenter( final int Nplanets, final int max_eta )
	{
		this( VoyagerStateToken.attributes( Nplanets, max_eta ) );
	}
	
	public IdentityRepresenter( final ArrayList<Attribute> attributes )
	{
		attributes_ = attributes;
	}
	
	@Override
	public IdentityRepresenter create()
	{
		return new IdentityRepresenter( attributes_ );
	}
	
	@Override
	public VoyagerStateToken encode( final VoyagerState s )
	{
		return s.token();
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}

}

