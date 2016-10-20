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
package edu.oregonstate.eecs.mcplan.util;

import java.util.List;
import java.util.ListIterator;

/**
 * A ListIterator that goes in a circle.
 * 
 * @author jhostetler
 */
public class CircularListIterator<T> implements ListIterator<T>
{
	private final List<T> list_;
	private ListIterator<T> itr_ = null;
	
	/**
	 * 
	 */
	public CircularListIterator( final List<T> list )
	{
		this( list, 0 );
	}
	
	public CircularListIterator( final List<T> list, final int idx )
	{
		list_ = list;
		itr_ = list_.listIterator( idx );
	}
	
	public CircularListIterator( final CircularListIterator<T> that )
	{
		list_ = that.list_;
		itr_ = list_.listIterator( that.nextIndex() );
	}

	@Override
	public boolean hasNext()
	{
		return !list_.isEmpty();
	}

	@Override
	public T next()
	{
		if( !itr_.hasNext() ) {
			itr_ = list_.listIterator();
		}
		// This will still throw for an empty list.
		return itr_.next();
	}

	@Override
	public boolean hasPrevious()
	{
		return !list_.isEmpty();
	}

	@Override
	public T previous()
	{
		if( !itr_.hasPrevious() ) {
			itr_ = list_.listIterator( list_.size() );
		}
		// This will throw for an empty list.
		return itr_.previous();
	}

	@Override
	public int nextIndex()
	{
		if( !itr_.hasNext() ) {
			return 0;
		}
		else {
			return itr_.nextIndex();
		}
	}

	@Override
	public int previousIndex()
	{
		if( !itr_.hasPrevious() ) {
			return list_.size() - 1;
		}
		else {
			return itr_.previousIndex();
		}
	}

	@Override
	public void remove()
	{
		itr_.remove();
	}

	@Override
	public void set( final T e )
	{
		itr_.set( e );
	}

	@Override
	public void add( final T e )
	{
		itr_.add( e );
	}
}
