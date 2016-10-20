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
