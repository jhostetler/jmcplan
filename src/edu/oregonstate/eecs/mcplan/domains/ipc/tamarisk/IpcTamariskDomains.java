/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.ipc.tamarisk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rddl.RDDL.LCONST;
import rddl.RDDL.PVAR_INST_DEF;
import rddl.RDDL.TYPE_NAME;
import edu.oregonstate.eecs.mcplan.rddl.RddlSpec;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author jhostetler
 *
 */
public class IpcTamariskDomains
{
//	public static IpcTamariskState load( final File f ) throws FileNotFoundException
//	{
//		final String s;
//	    try( final Scanner scanner = new Scanner( f ) ) {
//	    	final StringBuilder sb = new StringBuilder( (int) f.length() );
//	        while( scanner.hasNextLine() ) {
//	            sb.append( scanner.nextLine() );
//	        }
//	        // Remove all whitespace
//	        s = sb.toString().replaceAll( "\\s+", "" );
//	    }
//
//	    // Now, s is one big string of characters. We'll search for some key
//	    // phrases
//
//
//	}
	
	public static IpcTamariskState parse( final File domain, final File instance )
	{
		final RddlSpec spec = new RddlSpec( domain, instance, 1, 42, "unused" );
		
		final int T = spec.rddl_instance._nHorizon;
		final TYPE_NAME slotT = new TYPE_NAME( "slot" );
		final TYPE_NAME reachT = new TYPE_NAME( "reach" );
		final ArrayList<LCONST> slots = spec.rddl_nonfluents._hmObjects.get( slotT )._alObjects;
		final ArrayList<LCONST> reaches = spec.rddl_nonfluents._hmObjects.get( reachT )._alObjects;
		final int Nreaches = reaches.size();
		final int cells_per_reach = slots.size() / Nreaches;
		assert( cells_per_reach * Nreaches == slots.size() );
		
		final ArrayList<TIntList> reach_slots = new ArrayList<TIntList>();
		final Map<LCONST, ArrayList<LCONST>> upstream_neighbors = new HashMap<>();
		final Map<LCONST, ArrayList<LCONST>> downstream_neighbors = new HashMap<>();
		for( final LCONST r : reaches ) {
			reach_slots.add( new TIntArrayList() );
			upstream_neighbors.put( r, new ArrayList<LCONST>() );
			downstream_neighbors.put( r, new ArrayList<LCONST>() );
		}
		
		for( final PVAR_INST_DEF p : spec.rddl_nonfluents._alNonFluents ) {
			System.out.println( "non-fluent: " + p._sPredName._sPVarNameCanon );
			if( "slot_at_reach".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST s = p._alTerms.get( 0 );
				final LCONST r = p._alTerms.get( 1 );
				final int si = slots.indexOf( s );
				final int ri = reaches.indexOf( r );
				reach_slots.get( ri ).add( si );
				System.out.println( "slot_at_reach( " + s + "=" + si + ", " + r + "=" + ri + " )" );
			}
			else if( "downstream_reach".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST down = p._alTerms.get( 0 );
				final LCONST up = p._alTerms.get( 1 );
				System.out.println( "downstream_reach: " + up + " -> " + down );
				upstream_neighbors.get( down ).add( up );
				downstream_neighbors.get( up ).add( down );
			}
			else {
				throw new IllegalArgumentException( "Non-fluent '" + p + "'" );
			}
		}
		
		// slot -> (reach-index, slot-index-in-reach)
		final Map<LCONST, int[]> slot_idx = new HashMap<LCONST, int[]>();
		for( int i = 0; i < Nreaches; ++i ) {
			final TIntList r = reach_slots.get( i );
			r.sort();
			for( int j = 0; j < r.size(); ++j ) {
				slot_idx.put( slots.get( r.get( j ) ), new int[] { i, j } );
			}
		}
		
		// Re-organize neighbor maps into format needed by Parameters
		final int[][] upstream = new int[Nreaches][];
		final int[][] downstream = new int[Nreaches][];
		for( int i = 0; i < Nreaches; ++i ) {
			final LCONST reach = reaches.get( i );
			
			final ArrayList<LCONST> up = upstream_neighbors.get( reach );
			upstream[i] = new int[up.size()];
			for( int j = 0; j < up.size(); ++j ) {
				final LCONST u = up.get( j );
				upstream[i][j] = reaches.indexOf( u );
			}
			Arrays.sort( upstream[i] );
			
			final ArrayList<LCONST> down = downstream_neighbors.get( reach );
			downstream[i] = new int[down.size()];
			for( int j = 0; j < down.size(); ++j ) {
				final LCONST d = down.get( j );
				downstream[i][j] = reaches.indexOf( d );
			}
			Arrays.sort( downstream[i] );
		}
		
		final IpcTamariskParameters params = new IpcTamariskParameters(
			T, Nreaches, cells_per_reach, upstream, downstream );
		final IpcTamariskState s0 = new IpcTamariskState( params );
		
		for( final PVAR_INST_DEF p : spec.rddl_instance._alInitState ) {
			System.out.println( "init-state: " + p._sPredName._sPVarNameCanon );
			
			if( "tamarisk_at".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST slot = p._alTerms.get( 0 );
				final int[] si = slot_idx.get( slot );
				s0.reaches[si[0]][si[1]] |= IpcTamariskState.Tamarisk;
				System.out.println( "tamarisk_at( " + slot + " ) " + Arrays.toString( si ) );
			}
			else if( "native_at".equals( p._sPredName._sPVarNameCanon ) ) {
				final LCONST slot = p._alTerms.get( 0 );
				final int[] si = slot_idx.get( slot );
				s0.reaches[si[0]][si[1]] |= IpcTamariskState.Native;
				System.out.println( "native_at( " + slot + " ) " + Arrays.toString( si ) );
			}
			else {
				throw new IllegalArgumentException( "init-state: '" + p + "'" );
			}
		}
		
		return s0;
	}
}
