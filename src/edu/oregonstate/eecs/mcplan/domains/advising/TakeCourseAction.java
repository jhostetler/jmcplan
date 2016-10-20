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

import edu.oregonstate.eecs.mcplan.Action;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class TakeCourseAction implements Action<AdvisingState>, VirtualConstructor<TakeCourseAction>
{
	public final int[] courses;
	
	public TakeCourseAction( final int[] courses )
	{
		this.courses = courses;
	}
	
	@Override
	public TakeCourseAction create()
	{
		return new TakeCourseAction( courses );
	}
	
	public double reward( final AdvisingState s )
	{
		// See: academic_advising_mdp.rddl, lines 38-39
		double r = 0;
		for( final int course : courses ) {
			r += (s.grade[course] == AdvisingState.NotTaken ? -1 : -2);
		}
		return r;
	}
	
	@Override
	public void doAction( final RandomGenerator rng, final AdvisingState s )
	{
		final byte[] grades = new byte[courses.length];
		for( int i = 0; i < courses.length; ++i ) {
			final int course = courses[i];
			final int[] prereqs = s.params.prereqs.get( course );
			final byte[] prereq_grades = new byte[prereqs.length];
			for( int j = 0; j < prereqs.length; ++j ) {
				final int pre = prereqs[j];
				prereq_grades[j] = s.grade[pre];
			}
			final byte course_grade = s.sampleGrade( rng, course, prereq_grades );
			grades[i] = course_grade;
		}
		
		for( int i = 0; i < courses.length; ++i ) {
			s.grade[courses[i]] = grades[i];
		}
		
		s.t += 1;
	}

	@Override
	public boolean equals( final Object obj )
	{
		if( !(obj instanceof TakeCourseAction) ) {
			return false;
		}
		final TakeCourseAction that = (TakeCourseAction) obj;
		return Arrays.equals( courses, that.courses );
	}
	
	@Override
	public int hashCode()
	{
		return 3 + 5 * Arrays.hashCode( courses );
	}
	
	@Override
	public String toString()
	{
		return "TakeCourse" + Arrays.toString( courses );
	}
}
