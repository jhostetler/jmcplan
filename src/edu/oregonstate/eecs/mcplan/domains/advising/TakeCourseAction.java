/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.advising;

import java.util.Arrays;

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
	public void doAction( final AdvisingState s )
	{
		final int[] grades = new int[courses.length];
		for( int i = 0; i < courses.length; ++i ) {
			final int course = courses[i];
			final int[] prereqs = s.params.prereqs.get( course );
			final int[] prereq_grades = new int[prereqs.length];
			for( int j = 0; j < prereqs.length; ++j ) {
				final int pre = prereqs[j];
				prereq_grades[j] = s.grade[pre];
			}
			final int course_grade = s.sampleGrade( course, prereq_grades );
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
