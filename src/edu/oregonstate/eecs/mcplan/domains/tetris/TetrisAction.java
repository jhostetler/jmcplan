/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.tetris;

import java.lang.reflect.Type;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.oregonstate.eecs.mcplan.Action;
import edu.oregonstate.eecs.mcplan.VirtualConstructor;

/**
 * @author jhostetler
 *
 */
public class TetrisAction implements Action<TetrisState>, VirtualConstructor<TetrisAction>
{
	public static final class GsonSerializer implements JsonSerializer<TetrisAction>
	{
		@Override
		public JsonElement serialize( final TetrisAction a, final Type t, final JsonSerializationContext ctx )
		{
			final JsonObject root = new JsonObject();
			root.add( "p", new JsonPrimitive( a.position ) );
			root.add( "r", new JsonPrimitive( a.rotation ) );
			return root;
		}
	}
	
	// -----------------------------------------------------------------------
	
	public final int position;
	public final int rotation;
	
	public TetrisAction( final int position, final int rotation )
	{
		this.position = position;
		this.rotation = rotation;
	}
	
	@Override
	public int hashCode()
	{
		return 17 * (13 + position * (23 + rotation));
	}
	
	@Override
	public boolean equals( final Object obj )
	{
		final TetrisAction that = (TetrisAction) obj;
		return position == that.position && rotation == that.rotation;
	}
	
	@Override
	public String toString()
	{
		return "TetrisAction[" + position + ", " + rotation + "]";
	}
	
	@Override
	public TetrisAction create()
	{
		return new TetrisAction( position, rotation );
	}

	@Override
	public void doAction( final RandomGenerator rng, final TetrisState s )
	{
		final Tetromino tetro = s.getCurrentTetromino();
		tetro.setRotation( rotation );
		tetro.setPosition( position, s.params.Nrows - 1 - tetro.getBoundingBox().top );
		s.createTetromino( tetro );
		s.t += 1;
	}

}
