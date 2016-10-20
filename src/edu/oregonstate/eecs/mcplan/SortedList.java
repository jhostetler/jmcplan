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

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * A queue that stores elements in sorted order. Removing the first element
 * is O(1). Inserting is O(n) comparisons. This can be reduced to O(log n)
 * using binary search if that turns out to be necessary.
 * 
 * We require use of a Comparator instance for efficiency reasons, so that
 * we don't have to check whether the one has been supplied before every
 * operation.
 * 
 * Note that the need to maintain the ordering invariant means that operations
 * that certain operations are not implemented. Specifically, add(int, T)
 * and set(int, T) throw UnsupportedOperationException. remove(int) is also
 * not implemented, but only because I'm lazy.
 */
public class SortedList<T> extends AbstractQueue<T>
{
	private final LinkedList<T> list_ = new LinkedList<T>();
	private final Comparator<T> comp_;
	
	public SortedList( final Comparator<T> comp )
	{
		comp_ = comp;
	}
	
	@Override
	public int size()
	{
		return list_.size();
	}

	@Override
	public Iterator<T> iterator()
	{
		return list_.iterator();
	}

	@Override
	public boolean offer( final T t )
	{
		// TODO: This could use binary search; dubious benefit for Voyager.
		// Java wants me to use TreeSet, but it has O(log n) remove operations,
		// and I would prefer the O(1) remove we can get this way.
		if( t == null ) {
			throw new NullPointerException();
		}
		final ListIterator<T> itr = list_.listIterator();
		while( itr.hasNext() ) {
			final T candidate = itr.next();
			final int c = comp_.compare( candidate, t );
			if( c > 0 ) {
				itr.previous();
				itr.add( t );
				return true;
			}
		}
		itr.add( t );
		return true;
	}

	@Override
	public T peek()
	{
		return list_.peekFirst();
	}

	@Override
	public T poll()
	{
		return list_.pollFirst();
	}
}
