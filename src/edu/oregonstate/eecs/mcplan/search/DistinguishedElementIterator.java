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

package edu.oregonstate.eecs.mcplan.search;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator that yields an element supplied at construction-time first,
 * and then all elements returned by a base iterator that are *not*
 * equal to the special element (using equals()).
 * @author jhostetler
 *
 * @param <T>
 */
public class DistinguishedElementIterator<T> implements Iterator<T>
{
	private final Iterator<T> base_;
	private final T special_element_;
	private boolean used_ = false;
	
	private T next_ = null;
	private boolean has_next_ = false;
	
	public DistinguishedElementIterator( final Iterator<T> base, final T special_element )
	{
		base_ = base;
		assert( special_element != null );
		special_element_ = special_element;
		
		advance();
	}
	
	private void advance()
	{
		has_next_ = false;
		while( base_.hasNext() ) {
			next_ = base_.next();
			if( !special_element_.equals( next_ ) ) {
				has_next_ = true;
				break;
			}
		}
	}
	
	@Override
	public boolean hasNext()
	{
		if( !used_ ) {
			return true;
		}
		else {
			return has_next_;
		}
	}

	@Override
	public T next()
	{
		if( !used_ ) {
			used_ = true;
			return special_element_;
		}
		else if( has_next_ ) {
			advance();
			return next_;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
