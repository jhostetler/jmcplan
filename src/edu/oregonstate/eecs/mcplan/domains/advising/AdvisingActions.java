/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.advising;

import edu.oregonstate.eecs.mcplan.ActionGenerator;

/**
 * @author jhostetler
 *
 */
public class AdvisingActions extends ActionGenerator<AdvisingState, TakeCourseAction>
{
	int n = 0;
	final AdvisingParameters params;
	
	public AdvisingActions( final AdvisingParameters params )
	{
		this.params = params;
	}
	
	@Override
	public ActionGenerator<AdvisingState, TakeCourseAction> create()
	{
		return new AdvisingActions( params );
	}

	@Override
	public void setState( final AdvisingState s, final long t )
	{
		n = 0;
	}

	@Override
	public int size()
	{
		return params.Ncourses; // + 1;
	}

	@Override
	public boolean hasNext()
	{
		return n < size();
	}

	@Override
	public TakeCourseAction next()
	{
		final TakeCourseAction a;
//		if( n == params.Ncourses ) {
//			a = new TakeCourseAction( new int[] { } );
//		}
//		else {
			a = new TakeCourseAction( new int[] { n } );
//		}
		n += 1;
		return a;
	}
}
