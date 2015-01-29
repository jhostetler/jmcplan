package edu.oregonstate.eecs.mcplan.search.fsss;

import java.util.ArrayList;

import edu.oregonstate.eecs.mcplan.FactoredRepresentation;
import edu.oregonstate.eecs.mcplan.Representation;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;
import edu.oregonstate.eecs.mcplan.search.fsss.RefineablePartitionTreeRepresenter.DataNode;

public interface RefineableRepresenter<S extends State, A extends VirtualConstructor<A>>
{

	public abstract RefineableRepresenter<S, A> create();

	public abstract RefineableRepresenter<S, A> emptyInstance();

	public abstract Representation<S> encode( final S s );

	public abstract ArrayList<FsssAbstractStateNode<S, A>> refine(
			final FsssAbstractActionNode<S, A> an, final DataNode dn,
			final int attribute, final double split );

	public abstract DataNode classify( final DataNode dt_root,
			final FactoredRepresentation<S> x );

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
