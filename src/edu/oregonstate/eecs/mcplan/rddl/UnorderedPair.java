package edu.oregonstate.eecs.mcplan.rddl;



public class  UnorderedPair<K1 , K2 >  {

	public K1 _o1 = null;
	public K2 _o2 = null;
//	protected static Logger LOGGER = Logger.getLogger(Pair.class.getName());

	public UnorderedPair(final K1 o1, final K2 o2) {
		_o1 = o1;
		_o2 = o2;
	}

	public UnorderedPair() {
		_o1 = null;
		_o2 = null;
	}

	@Override
	public String toString() {
		return "<" + _o1.toString() + ", " + _o2.toString() + ">";
	}

	public void copy(final UnorderedPair<K1, K2> leafVals) {

		copy( leafVals._o1, leafVals._o2 );
		
	}

	public void copy(final K1 o1, final K2 o2) {
		
		_o1 = o1;
		_o2 = o2;
		
	}
}