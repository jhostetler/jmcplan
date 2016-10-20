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

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;

/**
 * @author jhostetler
 *
 */
public class MulticlassRepresenter<S>
	implements Representer<FactoredRepresentation<S>, Representation<S>>
{
	private final Classifier classifier_;
	private final Instances headers_;
	
	public final int Nclasses;
	
	public MulticlassRepresenter( final Classifier classifier, final int Nclasses,
								  final FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr )
	{
		classifier_ = classifier;
		this.Nclasses = Nclasses;
		final ArrayList<Attribute> labeled = new ArrayList<Attribute>( base_repr.attributes() );
		labeled.add( WekaUtil.createNominalAttribute( "__label__", Nclasses ) );
		headers_ = new Instances( "dummy", labeled, 0 );
		headers_.setClassIndex( labeled.size() - 1 );
	}
	
	private MulticlassRepresenter( final MulticlassRepresenter<S> that )
	{
		classifier_ = that.classifier_;
		headers_ = new Instances( that.headers_, 0 );
		this.Nclasses = that.Nclasses;
	}
	
	@Override
	public MulticlassRepresenter<S> create()
	{
		return new MulticlassRepresenter<S>( this );
	}

	@Override
	public Representation<S> encode( final FactoredRepresentation<S> x )
	{
		try {
			final Instance i = WekaUtil.labeledInstanceFromUnlabeledFeatures( headers_, x.phi() );
			headers_.add( i );
			i.setDataset( headers_ );
			final int c = (int) classifier_.classifyInstance( i );
			headers_.remove( 0 );
			return new ClusterAbstraction<S>( c );
		}
		catch( final RuntimeException ex ) {
			throw ex;
		}
		catch( final Exception ex ) {
			throw new RuntimeException( ex );
		}
	}

}
