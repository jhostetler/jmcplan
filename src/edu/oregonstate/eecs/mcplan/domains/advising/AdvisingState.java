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

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class AdvisingState implements State
{
	public static final byte NotTaken = -1;
	
	public final AdvisingParameters params;
	public final byte[] grade;
	public int t = 0;
	
	public AdvisingState( final AdvisingParameters params )
	{
		this.params = params;
		this.grade = Fn.repeat( NotTaken, params.Ncourses );
	}
	
	public AdvisingState( final AdvisingState that )
	{
		this.params = that.params;
		this.grade = Fn.copy( that.grade );
		this.t = that.t;
	}
	
	@Override
	public void close()
	{ }
	
	// See: academic_advising_mdp.rddl, lines 58-66
	public byte sampleGrade( final RandomGenerator rng, final int course, final byte[] prereq_grades )
	{
		final double pfail;
		if( prereq_grades.length == 0 ) {
			pfail = 1.0 - AdvisingParameters.prior_prob_pass_no_prereq;
		}
		else {
			int s = 0;
			for( final int g : prereq_grades ) {
				if( g != NotTaken ) {
					s += g;
				}
			}
			// Generalizes the probability calculation to numerical grades
			final double Z = params.max_grade * (prereq_grades.length + 1);
			final double ppass = AdvisingParameters.prior_prob_pass + (1.0 - AdvisingParameters.prior_prob_pass) * (s / Z);
			pfail = 1.0 - ppass;
		}
		
		// Generalizes grade assignment to numerical grades
		final double r = rng.nextDouble();
		final byte new_grade;
		if( r < pfail ) {
			new_grade = 0;
		}
		else {
			// Renormalize remainder of interval
			final double q = (r - pfail) / (1.0 - pfail);
			new_grade = (byte) (1 + (int) (q * params.max_grade));
		}
		assert( new_grade >= 0 );
		assert( new_grade <= params.max_grade );
		final byte result = (byte) Math.max( new_grade, grade[course] );
		return result;
	}

	@Override
	public boolean isTerminal()
	{
		if( t >= params.T ) {
			return true;
		}
		else {
			for( final int i : params.requirements ) {
				if( grade[i] < params.passing_grade ) {
					return false;
				}
			}
			return true;
		}
	}
	
	@Override
	public String toString()
	{
		return "[t: " + t + ", grade: " + Arrays.toString( grade ) + "]";
	}
}
