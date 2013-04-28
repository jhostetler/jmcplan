package edu.oregonstate.eecs.mcplan;

/**
 * A state represents a state of some domain. A state should override
 * toString(), equals() and hashCode().
 */
public abstract class State {
    /**
     * @return id of the agent that is next to take an action.
     */
    public abstract int getAgentTurn();
    
    @Override
    public abstract boolean equals( final Object that );
    
    @Override
    public abstract int hashCode();
    
    @Override
    public abstract String toString();
}