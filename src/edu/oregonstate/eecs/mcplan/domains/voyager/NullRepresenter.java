/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.voyager;

import java.util.ArrayList;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;

/**
 * Maps all states into the same representation.
 */
public class NullRepresenter implements FactoredRepresenter<VoyagerState, NullRepresenter>
{
	private static class NullRepresentation extends FactoredRepresentation<VoyagerState, NullRepresenter>
	{
		private static Object TheRepresentation = new Object();
		
		@Override
		public Representation<VoyagerState, NullRepresenter> copy()
		{
			return new NullRepresentation();
		}
		
		@Override
		public double[] phi()
		{
			return new double[0];
		}

		@Override
		public boolean equals( final Object obj )
		{
			return TheRepresentation == obj;
		}

		@Override
		public int hashCode()
		{
			return TheRepresentation.hashCode();
		}
	}
	
	@Override
	public FactoredRepresentation<VoyagerState, NullRepresenter> encode( final VoyagerState s )
	{
		return new NullRepresentation();
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return new ArrayList<Attribute>();
	}

}
