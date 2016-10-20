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
package edu.oregonstate.eecs.mcplan;

import java.util.ArrayList;
import java.util.List;

/**
 * Given ActionGenerators for all agents in a multi-agent domain, implements
 * an ActionGenerator over JointActions that generates the Cartesian product
 * of the individual action sets.
 */
public class ProductActionGenerator<S, A extends VirtualConstructor<A>>
	extends ActionGenerator<S, JointAction<A>>
{
	public static <S, A extends VirtualConstructor<A>>
	ProductActionGenerator<S, A> create( final List<? extends ActionGenerator<S, ? extends A>> gs )
	{
		return new ProductActionGenerator<S, A>( gs );
	}
	
	private final List<? extends ActionGenerator<S, ? extends A>> gs_;
	
	private S s_ = null;
	private long t_ = 0L;
	private int turn_ = 0;
	private int size_ = 0;
	private final ArrayList<A> partial_;
	private final int pidx_ = 0;
	
	private JointAction<A> next_ = null;
	
	public ProductActionGenerator( final List<? extends ActionGenerator<S, ? extends A>> gs )
	{
		gs_ = gs;
		partial_ = new ArrayList<A>( gs_.size() );
		for( int i = 0; i < gs_.size(); ++i ) {
			partial_.add( null );
		}
	}
	
	/**
	 * Generates the next JointAction, or 'null' if all individual
	 * ActionGenerators have been exhausted.
	 * @return
	 */
	private JointAction<A> advance()
	{
		// Seek backwards to the last agent index that has more actions
		int i = partial_.size() - 1;
		while( i >= 0 && !gs_.get( i ).hasNext() ) {
			--i;
		}
		if( i < 0 ) {
			// No more actions
			return null;
		}
		
		partial_.set( i, gs_.get( i ).next() );
		resetPartial( i + 1 );
		return createJointAction();
	}
	
	/**
	 * Resets all individual ActionGenerators with indices equal to or
	 * greater than 'begin', and populates 'partial_' with their first
	 * generated actions.
	 * @param begin
	 */
	private void resetPartial( final int begin )
	{
		for( int j = begin; j < gs_.size(); ++j ) {
			final ActionGenerator<S, ? extends A> gj = gs_.get( j );
			gj.setState( s_, t_, turn_ );
			assert( gj.hasNext() );
			partial_.set( j, gj.next() );
		}
	}
	
	/**
	 * Creates a JointAction from the individual actions stored in 'partial_'.
	 * @return
	 */
	private JointAction<A> createJointAction()
	{
		final JointAction.Builder<A> jb = new JointAction.Builder<A>( gs_.size() );
		for( int j = 0; j < gs_.size(); ++j ) {
			jb.a( j, partial_.get( j ).create() );
		}
		return jb.finish();
	}
	
	@Override
	public boolean hasNext()
	{
		return next_ != null;
	}

	@Override
	public JointAction<A> next()
	{
		final JointAction<A> a = next_;
		next_ = advance();
		return a;
	}

	@Override
	public ActionGenerator<S, JointAction<A>> create()
	{
		final ArrayList<ActionGenerator<S, ? extends A>> cp = new ArrayList<ActionGenerator<S, ? extends A>>();
		for( final ActionGenerator<S, ? extends A> g : gs_ ) {
			cp.add( g.create() );
		}
		return new ProductActionGenerator<S, A>( cp );
	}

	@Override
	public void setState( final S s, final long t, final int turn )
	{
		s_ = s;
		t_ = t;
		turn_ = turn;
		int size = 1;
		for( final ActionGenerator<S, ? extends A> g : gs_ ) {
			g.setState( s, t, turn );
			size *= g.size();
		}
		size_ = size;
		resetPartial( 0 );
		next_ = createJointAction();
	}

	@Override
	public int size()
	{
		return size_;
	}
}
