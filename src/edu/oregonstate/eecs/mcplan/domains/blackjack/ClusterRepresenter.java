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
package edu.oregonstate.eecs.mcplan.domains.blackjack;

import org.apache.commons.math3.linear.ArrayRealVector;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.abstraction.ClusterAbstraction;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public class ClusterRepresenter implements Representer<BlackjackState, ClusterAbstraction<BlackjackState>>
{
	private final VoronoiClassifier classifier_;
	private final Representer<BlackjackState, ? extends FactoredRepresentation<BlackjackState>> repr_;
	private int neg_idx_ = -1;
	
	public ClusterRepresenter( final VoronoiClassifier classifier,
							   final Representer<BlackjackState, ? extends FactoredRepresentation<BlackjackState>> repr )
	{
		classifier_ = classifier;
		repr_ = repr;
	}
	
	@Override
	public ClusterRepresenter create()
	{
		return new ClusterRepresenter( classifier_, repr_.create() );
	}

	@Override
	public ClusterAbstraction<BlackjackState> encode( final BlackjackState s )
	{
		// FIXME: This is a horrible hack! See comments in UctSearch.visit()
		if( s.passed( 0 ) ) {
			return new ClusterAbstraction<BlackjackState>( neg_idx_-- );
		}
		final int label = classifier_.classify( new ArrayRealVector( Fn.vcopy_as_double( repr_.encode( s ).phi() ) ) );
		return new ClusterAbstraction<BlackjackState>( label );
	}

}
