/* LICENSE
Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
