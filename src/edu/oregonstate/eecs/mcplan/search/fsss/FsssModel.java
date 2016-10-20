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
package edu.oregonstate.eecs.mcplan.search.fsss;

import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.FactoredRepresenter;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.Representer;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * Provides the domain-specific functions that we need for FSSS.
 * <p>
 * FIXME: Should separate out the "dynamics" parts, since we want to be able
 * to run them with different RNGs. There's a lot of places in the code where
 * we pass around an FsssModel because we need access to ie. base_repr, and
 * it's not obvious whether it matters which RNG is associated with the model
 * we pass to these methods.
 * <p>
 * TODO: should we remove R(s, a) and instead have sampleTransition return a
 * (reward, next state) tuple? In some domains (e.g. Tetris), calculating the
 * reward for an action basically requires simulating a transition. R(s, a) is
 * used only by the constructor of FsssActionNode, and the value will be
 * overwritten before it's used when GAN.sample() is called. Note that for the
 * time being we wrote Tetris to have only state rewards.
 * 
 * @param <S>
 * @param <A>
 */
public abstract class FsssModel<S extends State, A extends VirtualConstructor<A>>
{
	/**
	 * Create a new instance of this model using a different RandomGenerator.
	 * <p>
	 * Parameters should be copied, but internal state should be reset.
	 * 
	 * @param rng
	 * @return
	 */
	public abstract FsssModel<S, A> create( final RandomGenerator rng );
	
	/**
	 * Must include R(s).
	 * @param s
	 * @return
	 */
	public abstract double Vmin( final S s );
	
	/**
	 * Must include R(s).
	 * @param s
	 * @return
	 */
	public abstract double Vmax( final S s );
	
	/**
	 * Initial value of L(s, a). Must include the reward R(s, a). Must *not*
	 * include R(s). Typically, this will be something like:
	 * 		Vmin( s, a ) = reward( s, a ) + min_{s' \in succ(s, a)} Vmin( s' )
	 * @param s
	 * @param a
	 * @return
	 */
	public abstract double Vmin( final S s, final A a );
	
	/**
	 * Initial value of U(s, a). Must include the reward R(s, a). Must *not*
	 * include R(s). Typically, this will be something like:
	 * 		Vmax( s, a ) = reward( s, a ) + max_{s' \in succ(s, a)} Vmax( s' )
	 * @param s
	 * @param a
	 * @return
	 */
	public abstract double Vmax( final S s, final A a );
	
	public abstract double discount();
	
	/**
	 * Heuristic should *not* include R(s)!
	 * @param s
	 * @return
	 */
	public abstract double heuristic( final S s );
	
	/**
	 * RandomGenerator used by this instance.
	 * @return
	 */
	public abstract RandomGenerator rng();
	
	/**
	 * Ground state representation.
	 * @return
	 */
	public abstract FactoredRepresenter<S, ? extends FactoredRepresentation<S>> base_repr();
	
	/**
	 * This representer must map states that have different legal action sets
	 * to different Representations.
	 * @return
	 */
	public abstract Representer<S, ? extends Representation<S>> action_repr();
	
	/**
	 * Return the action set for a collection of states. Default implementation
	 * returns the action set for the first state in the collection.
	 * @param states
	 * @return
	 */
	public Iterable<? extends A> actions( final FsssAbstractStateNode<S, A> asn )
	{
		final S s = asn.states().iterator().next().s();
		return actions( s );
	}
	
	/**
	 * Returns an initial state.
	 * <p>
	 * The FsssModel implementation is allowed to store state for efficiency
	 * reasons. Thus, the state returned by initialState() is required to be
	 * valid *only until the next call to initialState()*.
	 * @return
	 */
	public abstract S initialState();
	
	/**
	 * Action set.
	 * @param s
	 * @return
	 */
	public abstract Iterable<A> actions( final S s );
	
	/**
	 * @param s
	 * @param a
	 * @return
	 */
	public abstract S sampleTransition( final S s, final A a );
	
	/**
	 * Reward for being in 's'.
	 * @param s
	 * @return
	 */
	public abstract double reward( final S s );
	
	/**
	 * Reward for doing 'a' in 's'. Should *not* include R(s).
	 * @param s
	 * @param a
	 * @return
	 */
	public abstract double reward( final S s, final A a );
	
	// TODO: These probably don't belong here
	public abstract int sampleCount();
	public abstract void resetSampleCount();
}
