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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author jhostetler
 *
 */
public class AdvisingParameters
{
	// See: academic_advising_mdp.rddl, line 31
	public static final double prior_prob_pass_no_prereq = 0.8;
	// See: academic_advising_mdp.rddl, line 32
	public static final double prior_prob_pass = 0.2;
	
	public static final double missing_requirement_reward = -5;
	
	public final int T;
	public final int max_grade;
	public final int passing_grade;
	public final int Ncourses;
	public final int[] requirements;
	public final ArrayList<int[]> prereqs;
	
	public AdvisingParameters( final int T,
							   final int max_grade, final int passing_grade,
							   final int Ncourses,
							   final int[] requirements, final ArrayList<int[]> prereqs )
	{
		this.T = T;
		this.max_grade = max_grade;
		this.passing_grade = passing_grade;
		this.Ncourses = Ncourses;
		this.requirements = requirements;
		this.prereqs = prereqs;
		
		assert( max_grade > 0 );
		assert( max_grade <= Byte.MAX_VALUE );
		assert( passing_grade >= 0 );
		assert( passing_grade <= max_grade );
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "T: " ).append( T );
		sb.append( ", Ncourses: " ).append( Ncourses );
		sb.append( ", requirements: " ).append( Arrays.toString( requirements ) );
		sb.append( ", prereqs: " ).append( "[" );
		for( int i = 0; i < prereqs.size(); ++i ) {
			if( i > 0 ) {
				sb.append( ", " );
			}
			sb.append( Arrays.toString( prereqs.get( i ) ) );
		}
		sb.append( "]" );
		return sb.toString();
	}
}
