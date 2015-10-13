/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.advising;

import java.io.File;
import java.util.ArrayList;

import rddl.RDDL.LCONST;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.RDDL.TYPE_NAME;
import edu.oregonstate.eecs.mcplan.rddl.RddlSpec;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author jhostetler
 *
 */
public class AdvisingRddlParser
{
	public static AdvisingParameters parse( final int max_grade, final int passing_grade,
											final File domain, final File instance )
	{
		final RddlSpec spec = new RddlSpec( domain, instance, 1, 42, "unused" );
		
		final int T = spec.rddl_instance._nHorizon;
		final TYPE_NAME course_type = new TYPE_NAME( "course" );
		final ArrayList<LCONST> objects = spec.rddl_nonfluents._hmObjects.get( course_type )._alObjects;
		final int Ncourses = objects.size();
		final TIntList req_list = new TIntArrayList();
		final ArrayList<TIntList> prereq_list = new ArrayList<TIntList>();
		for( int i = 0; i < Ncourses; ++i ) {
			prereq_list.add( new TIntArrayList() );
		}
		for( final PVAR_INST_DEF p : spec.rddl_nonfluents._alNonFluents ) {
//			System.out.println( p._sPredName._sPVarNameCanon );
			if( "program_requirement".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST req = p._alTerms.get( 0 );
//				System.out.println( "Requirement: " + req + " (" + objects.indexOf( req ) + ")" );
				req_list.add( objects.indexOf( req ) );
			}
			else if( "prereq".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST prereq = p._alTerms.get( 0 );
				final LCONST target = p._alTerms.get( 1 );
//				System.out.println( "" + prereq + " (" + objects.indexOf( prereq )
//									+ ") -> " + target + " (" + objects.indexOf( target ) + ")" );
				final TIntList prereq_target = prereq_list.get( objects.indexOf( target ) );
				prereq_target.add( objects.indexOf( prereq ) );
			}
			else {
				throw new IllegalArgumentException( "Non-fluent '" + p + "'" );
			}
		}
		final int[] requirements = req_list.toArray();
		final ArrayList<int[]> prereqs = new ArrayList<int[]>();
		for( final TIntList list : prereq_list ) {
			prereqs.add( list.toArray() );
		}
		
		return new AdvisingParameters( T, max_grade, passing_grade, Ncourses, requirements, prereqs );
	}
}
