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

package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 * @deprecated
 *
 * @param <S>
 * @param <A>
 */
@Deprecated
public interface RefineableRepresenter<S extends State, A extends VirtualConstructor<A>>
{

	public abstract RefineableRepresenter<S, A> create();

	public abstract RefineableRepresenter<S, A> emptyInstance();

	public abstract Representation<S> encode( final S s );

	public abstract ArrayList<FsssAbstractStateNode<S, A>> refine(
			final FsssAbstractActionNode<S, A> an, final DataNode dn,
			final int attribute, final double split );

//	public abstract DataNode classify( final DataNode dt_root,
//			final FactoredRepresentation<S> x );

	/**
	 * Same as 'encode()', but creates a successor node if one does not
	 * already exist. Does NOT add a ground state node for the sample.
	 * @param an
	 * @param s
	 * @param x
	 * @return
	 */
	public abstract DataNode addTrainingSample(
			final FsssAbstractActionNode<S, A> an, final S s,
			final FactoredRepresentation<S> x );

	/**
	 * Same as 'encode()', but creates a successor node if one does not
	 * already exist. Does NOT add a ground state node for the sample.
	 * @param an
	 * @param sn
	 * @return
	 */
	public abstract DataNode addTrainingSampleAsExistingNode(
			final FsssAbstractActionNode<S, A> an, final FsssStateNode<S, A> sn );

}
