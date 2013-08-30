/**
 * 
 */
package edu.oregonstate.eecs.mcplan;

/**
 * An object that can generate hashable and equality-comparable "tokens"
 * representing its current state.
 * 
 * The 'Token' type must override hashCode() and equals().
 */
public interface Tokenizable<Token>
{
	public Token token();
}
