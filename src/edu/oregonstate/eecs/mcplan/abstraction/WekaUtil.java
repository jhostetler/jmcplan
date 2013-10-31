/**
 * 
 */
package edu.oregonstate.eecs.mcplan.abstraction;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Instances;

/**
 * @author jhostetler
 *
 */
public class WekaUtil
{
	public static Instances createEmptyInstances( final String name, final ArrayList<Attribute> attributes )
	{
		final Instances instances = new Instances( name, attributes, 0 );
		instances.setClassIndex( attributes.size() - 1 );
		return instances;
	}
}
