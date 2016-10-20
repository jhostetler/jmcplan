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
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;

import weka.core.Attribute;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.ml.VoronoiClassifier;

/**
 * @author jhostetler
 *
 */
public class ClusterRepresenter<S extends State> implements FactoredRepresenter<S, ClusterAbstraction<S>>
{
	private final VoronoiClassifier classifier_;
	private final Representer<S, ? extends FactoredRepresentation<S>> repr_;
	private int neg_idx_ = -1;
	
	private final ArrayList<Attribute> attributes_ = new ArrayList<Attribute>();
	
	public ClusterRepresenter( final VoronoiClassifier classifier,
							   final Representer<S, ? extends FactoredRepresentation<S>> repr )
	{
		classifier_ = classifier;
		repr_ = repr;
		
		attributes_.add( new Attribute( "__cluster__" ) );
	}
	
	@Override
	public ClusterRepresenter<S> create()
	{
		return new ClusterRepresenter<S>( classifier_, repr_.create() );
	}

	@Override
	public ClusterAbstraction<S> encode( final S s )
	{
		// FIXME: This is a horrible hack! See comments in UctSearch.visit()
		if( s.isTerminal() ) {
			return new ClusterAbstraction<S>( neg_idx_-- );
		}
		final int label = classifier_.classify( new ArrayRealVector( repr_.encode( s ).phi() ) );
		return new ClusterAbstraction<S>( label );
	}

	@Override
	public ArrayList<Attribute> attributes()
	{
		return attributes_;
	}
}
