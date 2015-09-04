/**
 * 
 */
package edu.oregonstate.eecs.mcplan.domains.firegirl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import edu.oregonstate.eecs.mcplan.LoggerManager;
import edu.oregonstate.eecs.mcplan.State;
import edu.oregonstate.eecs.mcplan.util.Fn;

/**
 * @author jhostetler
 *
 */
public final class FireGirlState implements State
{
	private static final ch.qos.logback.classic.Logger Log = LoggerManager.getLogger( "log.domain" );
	
	private final FireGirlParameters params;
	
	public final short[][] stand_age;
	public final short[][] fuel_load;
	
	public int year = 0;
	
	public int ignite_date = -1;
	public int[] ignite_loc = null;
	public double ignite_wind = 0.0;
	public double ignite_temp = 0.0;
	
	public double r = 0.0;
	
	public FireGirlState( final FireGirlParameters params )
	{
		this.params = params;
		
		stand_age = new short[params.width][params.height];
		fuel_load = new short[params.width][params.height];
	}
	
	public FireGirlState( final FireGirlState that )
	{
		this( that.params );
		Fn.memcpy( this.stand_age, that.stand_age );
		Fn.memcpy( this.fuel_load, that.fuel_load );
		this.year = that.year;
		this.ignite_date = that.ignite_date;
		this.ignite_loc = Fn.copy( that.ignite_loc );
		this.ignite_wind = that.ignite_wind;
		this.ignite_temp = that.ignite_temp;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "[r: " ).append( r ).append( ", year: " ).append( year )
		  .append( ", date: " ).append( ignite_date )
		  .append( ", loc: " ).append( Arrays.toString( ignite_loc ) )
		  .append( ", wind: " ).append( ignite_wind )
		  .append( ", temp: " ).append( ignite_temp ).append( "]" );
		return sb.toString();
	}
	
	public double getValueAverage( final int x, final int y, final int margin )
	{
		double avg = 0.0;
		for( int i = x - margin; i <= x + margin; ++i ) {
			for( int j = y - margin; j <= y + margin; ++j ) {
				if( i == x && j == y ) {
					continue;
				}
				avg += getPresentTimberValue( i, j );
			}
		}
		return avg / (margin*margin - 1);
	}
	
	public double getFuelAverage( final int x, final int y, final int margin )
	{
		double avg = 0.0;
		for( int i = x - margin; i <= x + margin; ++i ) {
			for( int j = y - margin; j <= y + margin; ++j ) {
				if( i == x && j == y ) {
					continue;
				}
				avg += fuel_load[i][j];
			}
		}
		return avg / (margin*margin - 1);
	}
	
	public double totalValue()
	{
		double v = 0.0;
		for( int i = 0; i < params.width; ++i ) {
			for( int j = 0; j < params.height; ++j ) {
				v += getPresentTimberValue( i, j );
			}
		}
		return v;
	}
	
	@Override
	public boolean isTerminal()
	{
		// Always return false if discounting
		return params.discount == 1.0 && year >= params.T;
	}
	
	public void setRandomInitialState( final RandomGenerator episode_rng )
	{
		final FireGirlDiamondSquareAlgorithm alg = new FireGirlDiamondSquareAlgorithm( episode_rng, params );
		alg.run();
		Fn.memcpy( stand_age, alg.getStandAge() );
		Fn.memcpy( fuel_load, alg.getFuelLoad() );
		doIgnition( episode_rng );
	}
	
	private int drawIgnitionDay( final RandomGenerator rng )
	{
		if( rng.nextDouble() < params.ignition_prob ) {
			return rng.nextInt( 365 );
		}
		else {
			return -1;
		}
	}
	
	private int[] drawLocation( final RandomGenerator rng )
	{
		final int[] loc = new int[] {
			2 + rng.nextInt( params.width - 4 ),
			2 + rng.nextInt( params.height - 4 )
		};
		return loc;
	}
	
	private double drawWindSpeed( final RandomGenerator rng, final int date )
	{
		final RealDistribution wind_pdf = new ExponentialDistribution( rng, params.wind_mean );
		return wind_pdf.sample();
	}
	
	private double drawEndOfFire( final RandomGenerator rng )
	{
		final RealDistribution end_pdf = new ExponentialDistribution( rng, params.fire_average_end_day );
		return 1 + end_pdf.sample();
	}
	
	private double tempMean( final int date )
	{
		final double radian_day = 2*date*Math.PI / 365.0;
		final double hotness = (-Math.cos( radian_day ) + 1) / 2;
		return params.temp_winter_low + hotness*(params.temp_summer_high - params.temp_winter_low);
	}
	
	private double drawTemperature( final RandomGenerator rng, final int date )
	{
		// Re-scale standard normal
		return rng.nextGaussian()*Math.sqrt( params.temp_var ) + tempMean( date );
	}
	
	private void doIgnition( final RandomGenerator rng )
	{
		ignite_date = drawIgnitionDay( rng );
		ignite_loc = drawLocation( rng );
		
		if( ignite_date >= 0 ) {
			assert( ignite_date < 365 );
			
			ignite_wind = drawWindSpeed( rng, ignite_date );
			ignite_temp = drawTemperature( rng, ignite_date );
		}
		else {
			ignite_wind = 0;
			ignite_temp = 0;
		}
		
		Log.info( "Ignition: [date: {}, loc: {}, wind: {}, temp: {}]",
				  ignite_date, Arrays.toString( ignite_loc ), ignite_wind, ignite_temp );
	}
	
	private double doGrowth()
	{
		double total_growth = 0.0;
		for( int i = 0; i < params.width; ++i ) {
			for( int j = 0; j < params.height; ++j ) {
				final int age = stand_age[i][j];
				if( age < params.max_age ) {
					final double old_val = params.growth_model[age];
					final double new_val = params.growth_model[age + 1];
					stand_age[i][j] += 1;
					total_growth += (new_val - old_val);
				}
				fuel_load[i][j] += params.growth_fuel_accumulation;
			}
		}
		return total_growth;
	}
	
	private double doLogging( final RandomGenerator rng, final double growth )
	{
		final double max_cut = growth * params.logging_percentOfIncrement;
		
		double total_cut = 0;
		
		for( int i = 0; i < params.logging_max_cuts; ++i ) {
			if( total_cut >= max_cut ) {
				break;
			}
			final int x = rng.nextInt( params.width - params.logging_block_width + 1 );
			final int y = rng.nextInt( params.height - params.logging_block_width + 1 );
			total_cut += doLogging_one_block( x, y, (max_cut - total_cut) );
		}
		
		return total_cut;
	}
	
	private double doLogging_one_block( final int x, final int y, final double timber_limit )
	{
		assert( timber_limit > 0 );
		double total_cut = 0;
		
		outer:
		for( int i = x; i < x + params.logging_block_width; ++i ) {
			for( int j = y; j < y + params.logging_block_width; ++j ) {
				final double value = getPresentTimberValue( i, j );
				if( value >= params.logging_min_value ) {
					// NOTE: Deviation from Python
					stand_age[i][j] = 0;
					
					fuel_load[i][j] = (byte) params.logging_slash_remaining;
					total_cut += value;
					
					if( total_cut >= timber_limit ) {
						break outer;
					}
				}
			}
		}
		
		return total_cut;
	}
	
	public static class YearResult
	{
		public final FireResult fire;
		public final double growth;
		public final double logging;
		
		public YearResult( final FireResult fire, final double growth, final double logging )
		{
			this.fire = fire;
			this.growth = growth;
			this.logging = logging;
		}
	}
	
	public YearResult doOneYear( final RandomGenerator rng, final FireGirlAction a )
	{
		final FireResult fire = doFire( rng, a.suppress );
		Log.info( "{}", fire );
		
		// TODO: Calculate things from fire
		
		final double growth = doGrowth();
		Log.info( "growth: {}", growth  );
		final double logging = doLogging( rng, growth );
		Log.info( "logging: {}", logging );
		
		year += 1;
		
		doIgnition( rng );
		
		return new YearResult( fire, growth, logging );
	}
	
	public double calcFireSpreadRate( final double wind, final double temp, final int fuel )
	{
		if( wind + temp < params.min_spread_windtemp ) {
		    return 0;
		}
		if( fuel < params.min_spread_fuel ) {
		    return 0;
		}
		
		final double out_scale = params.fire_param_outputscale;
		final double in_scale = params.fire_param_inputscale;
		final double zero_adj = params.fire_param_zeroadjust;
		final double smooth = params.fire_param_smoothness;
		
		final double exponent = (-1 * smooth * (   ((wind + temp + fuel) / in_scale) - zero_adj   ));
		final double fspread = out_scale / (1 + Math.exp(exponent));
		return fspread;
	}
	
	public double calcCrownFireRisk( final int fuel )
	{
        //This function takes the current fuel load in a cell and calculates the
        //   probabilty that a fire will spread to it's crown. It is based entirely
        //   on fuel_load. The shape of the logistic is arbitrary, and gives
        //   a crownfire model that should provide good tradeoffs between low-fuel
        //   fires and high-fuel fires.
        
        //Parameters that shape the logistic function:
        final double out_scale = params.crownfire_param_outputscale;
        final double in_scale = params.crownfire_param_inputscale;
        final double zero_adj = params.crownfire_param_zeroadjust;
        final double smooth = params.crownfire_param_smoothness;
        
        //Calculating the logistic
        final double exponent = (   -1 * smooth *     (   (fuel / in_scale) - zero_adj   )       );
        final double cf_risk = out_scale / (1 + Math.exp(exponent));
        
        return cf_risk;
	}
	
	private static class PrioritizedLocation implements Comparable<PrioritizedLocation>
	{
		public final double priority;
		public final int[] location;
		
		public PrioritizedLocation( final double priority, final int[] location )
		{
			this.priority = priority;
			this.location = location;
		}
		
		@Override
		public int compareTo( final PrioritizedLocation that )
		{
			return (int) Math.signum( this.priority - that.priority );
		}
		
		@Override
		public boolean equals( final Object obj )
		{
			final PrioritizedLocation that = (PrioritizedLocation) obj;
			return priority == that.priority && Arrays.equals( location, that.location );
		}
		
		@Override
		public int hashCode() { throw new UnsupportedOperationException(); }
	}
	
	private FireResult doFire( final RandomGenerator rng, final boolean suppress )
	{
		final int reach = params.fire_param_reach;
		
		final double end_time = drawEndOfFire( rng );
        double current_time = 0;
        
        // Construct the priority queue and add the first cell to it with time = 0
        final PriorityQueue<PrioritizedLocation> pqueue = new PriorityQueue<PrioritizedLocation>();
        pqueue.add( new PrioritizedLocation( 0, ignite_loc ) );
        
        // setting a variable that will hold the lowest of all the ingition times in the queue.
//        final int next_ign = 1000;

        final boolean[][] burned = new boolean[params.width][params.height];
        final boolean[][] crown_burned = new boolean[params.width][params.height];
        
        // start the queue loop
        int iter_count = 0;
        while( true ) {
            
                
            //check to make sure that there is at least one queued arrival
            if( pqueue.isEmpty() ) {
                //no queued arrivals, so there's no fire, so we're done
                //print("Priority Queue Exiting: No more queued ignitions")
                break;
            }
            
            //look through all the queued ignitions and find the earliest ignition
            //  time.
            final PrioritizedLocation next_ign = pqueue.poll();
            //now check to see if the soonest arrival happens before the time is up.
            if( next_ign.priority >= end_time ) {
                //no fire arrivals (ignitions) are in queue within the alloted time
                //   so the firespread has stopped.
                //print("Priority Queue Exiting: Remaining queued ignitions are past the time limit")
                break;
            }
            
            //moving current time up to this ignition
            current_time = next_ign.priority;
            final int xloc = next_ign.location[0];
            final int yloc = next_ign.location[1];
            
            if( burned[xloc][yloc] ) {
            	continue;
            }
            
            //we haven't left the loop, so the next arrival is valid, so look at
            //  it and add its neighbors to the queue
            
            //failsafe exit
            iter_count += 1;
            if( iter_count > params.fire_iter_cap ) {
                Log.warn( "! Stopping fire early. time: {}", current_time );
                break;
            }
            
            //setting this cell to burned
            burned[xloc][yloc] = true;
            
            
            //Calculating this cell's fire spreadrate, which needs it's fuel load, too
            final int fuel_ld = fuel_load[xloc][yloc];
            double spreadrate = calcFireSpreadRate(ignite_wind, ignite_temp, fuel_ld);

            //add the effects of suppression
            if( suppress ) {
                spreadrate *= params.fire_suppression_rate;
            }
            
            // Check if the crown will burn (if the spreadrate is > 0)
            // Timber loss is a probabalistic function based on the
            //   calcCrownFireRisk() function.  This function will return
            //   a probability of crownfire, and we'll roll a uniform
            //   number against it.
            
            // NOTE: Deviation from Python
            if( rng.nextDouble() < calcCrownFireRisk( fuel_ld ) ) {
            	crown_burned[xloc][yloc] = true;
            }

            //if the fire spreadrate of this fire is 0, then don't bother checking
            //   for neighbors and calculating arrival times... there won't be any
            //   spread, and for that matter, we'll get a divide-by-zero error.
            if( spreadrate == 0 ) {
                //no spreadrate, so we can't calculate arrival times, etc...
                //pqueue.remove([current_time,[xloc,yloc]])
                
            	// Note: Already removed the element
                continue;
            }
            
            //recording information in the Logbook item
            //function signature is:  FireGirlfireLog.addIgnitionEvent(time, location, spread_rate, crown_burned):
//            fire_log_item.addIgnitionEvent(current_time, [xloc,yloc], spreadrate, crown_burned[xloc][yloc])
            
            //setting iteration final ranges
            final int x_low = Math.max( xloc - reach, 0 );
            final int x_high = Math.min( xloc+reach+1, params.width - 1 );
            final int y_low = Math.max( yloc-reach, 0 );
            final int y_high = Math.min( yloc+reach+1, params.height - 1 );

//            #checking bounds
//            if (x_low < 0): x_low = 0
//            if (y_low < 0): y_low = 0
//            if (x_high >= self.width): x_high = self.width - 1
//            if (y_high >= self.height): y_high = self.height - 1
            
            
            // FIXME: I think this indexing is incorrect (one short) due to
            // how x/y_high are capped above
            // Resolved: Changed '<' to '<='
            for( int i = x_low; i <= x_high; ++i ) { //i in range(x_low, x_high):
                for( int j = y_low; j <= y_high; ++j ) { //for j in range(y_low, y_high):

//                    #don't calculate time to the current cell
                    if( ! ((xloc == i) && (yloc == j)) ) {
                        
//                        #we're checking each neighbor within the reach range, so
//                        #  first, we need to check whether it's already been
//                        #  burned over
                        
                        if( !burned[i][j] ) {
                            
//                            #this neighbor hasn't burned over yet, so:
//                            # 1) calculate a new time-till arrival
//                            # 2) check to see if this neighbor is already in the queue
//                            # 2a) if it is, then check to see if this arrival time is sooner
//                            #       and if so, update it. Otherwise, just move on.
//                            # 2b) if it isn't in the queue, then add it as a new queue item
                            
//                            # 1) final arrival time for this neighbor
                            final double dist = Math.sqrt(   (xloc - i)*(xloc - i) + (yloc-j)*(yloc-j)    );
                            final double arrival_time = (dist/spreadrate) + current_time;
                            
                            // Just add it again; we filter duplicates by checking if they're already burned.
                            pqueue.add( new PrioritizedLocation( arrival_time, new int[] { i, j } ) );
                            
////                            # 2) checking to see if this neighbor is already queued
//                            boolean found_in_q = false;
//                            final Iterator<PrioritizedLocation> itr = pqueue.iterator();
//                            while( itr.hasNext() ) {
//                            	final PrioritizedLocation ign = itr.next();
//                                if( ign.location[0] == i && ign.location[1] == j ) {
////                                    #this neighbor IS in the queue already, so check its arrival time
////                                    #print("   neighbor found in queue... updating...")
//                                    found_in_q = true;
//
////                                    #updating it's arrival time if need be
//                                    if( arrival_time < ign.priority ) {
//                                    	itr.remove();
////                                        #the new arrival time is sooner, so update this queue item
//                                    	pqueue.add( new PrioritizedLocation( arrival_time, ign.location ) );
//                                    }
//                                    break;
//                                }
//                            }
//
//
////                            #check to see if we ever found this neighbor
//                            if( !found_in_q ) {
////                                #we never found it, so it wasn't in the queue, and it's not burned, so add it
//                                pqueue.add( new PrioritizedLocation( arrival_time, new int[] { i, j } ) );
//                            }
                        }
                    }
                }
            }
        
//            # we've now finished checking the neighbors, so it's time to remove this item from the queue
//            pqueue.remove([current_time,[xloc,yloc]])
        } // priority queue empty
        
//        # and now we've exited the priority queue as well
        
//        #look through the burned cells and update the actual grid values
//        #Also, record losses
        double timber_loss = 0;
        int cells_burned = 0;
        int cells_crowned = 0;
        
        for( int i = 0; i < params.width; ++i ) {
            for( int j = 0; j < params.height; ++j ) {
                if( burned[i][j] ) {
                    cells_burned += 1;
                                        
//                    #this cell was burned, so set the fuel_load to zero, and apply
//                    #  the crown-burning model to the timber_value
                    fuel_load[i][j] = 0;
                    
                    
                    
//                    #adding up timber loss
                    if( crown_burned[i][j] ) { //this was set when spreadrate was calculated earlier
//                        #the crown burned, so record the loss and set it to zero
                        
//                        #SPEED
//                        ####BOTH Lines are modified for self.getPresentTimberValue(i,j)###########
                        timber_loss += getPresentTimberValue(i,j);  //self.timber_value[i][j]
//                        #self.timber_value[i][j] = 0
//                        ####################
                        
                        cells_crowned += 1;
                        
//                        #and reset the age so that self.year + self.stand_age = 0
//                        stand_age[i][j] = -1 * year;
                        // NOTE: Deviation from Python code
                        stand_age[i][j] = 0;
                    }
                }
            }
        }
        
        
//        #Adding the final results to the fire_log_item
//        fire_log_item.updateResults(timber_loss, cells_burned, cells_crowned)

//        #Adding the lists (final maps) as well
//        fire_log_item.map_burned = burned
//        fire_log_item.map_crowned = crown_burned
        
//        #add the FireLog item to the pathway's list (it's just an ordinary list)
//        self.FireLog.append(fire_log_item)


//        #add up suppression cost and record it
        int sup_cost = 0;
        if( suppress ) {
            sup_cost +=  cells_burned * params.fire_suppression_cost_per_cell;
            sup_cost +=      end_time * params.fire_suppression_cost_per_day;
        }

//        self.yearly_suppression_costs.append(sup_cost)
        
//        #and finally, return the loss data
        return new FireResult( timber_loss, cells_burned, cells_crowned, sup_cost, end_time );
//        return [timber_loss, cells_burned, sup_cost, end_time, cells_crowned]
	}
	
	public static class FireResult
	{
		final double timber_loss;
		final int cells_burned;
		final int cells_crowned;
		final int sup_cost;
		final double end_time;
		
		public FireResult( final double timber_loss, final int cells_burned, final int cells_crowned,
						   final int sup_cost, final double end_time )
		{
			this.timber_loss = timber_loss;
			this.cells_burned = cells_burned;
			this.cells_crowned = cells_crowned;
			this.sup_cost = sup_cost;
			this.end_time = end_time;
		}
		
		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( "FireResult: [timber_loss: " ).append( timber_loss )
			  .append( ", cells_burned: " ).append( cells_burned )
			  .append( ", cells_crowned: " ).append( cells_crowned )
			  .append( ", sup_cost: " ).append( sup_cost )
			  .append( ", end_time: " ).append( end_time )
			  .append( "]" );
			return sb.toString();
		}
	}
	
	public double getPresentTimberValue( final int x, final int y )
	{
		return params.growth_model[stand_age[x][y]];
	}
	
	public void writePng( final File f ) throws IOException
	{
		final int s = 10;
		final BufferedImage img = new BufferedImage( params.width*s, 2 * params.height*s, BufferedImage.TYPE_INT_RGB );
		for( int i = 0; i < params.width; ++i ) {
			final int x = i;
			for( int j = 0; j < params.height; ++j ) {
				final int y = params.height - j - 1;
				int rgb = 0;
				final short age = stand_age[i][j];
				final int bage = (int) (255*age / ((double) params.max_age) );
				for( int k = 0; k < 3; ++k ) {
					rgb |= ( (bage & 0xff) << k*8 );
				}
				for( int ix = 0; ix < s; ++ix ) {
					for( int iy = 0; iy < s; ++iy ) {
						img.setRGB( s*x + ix, s*y + iy, rgb );
					}
				}
			}
		}
		for( int i = 0; i < params.width; ++i ) {
			final int x = i;
			for( int j = 0; j < params.height; ++j ) {
				final int y = 2*params.height - j - 1;
				int rgb = 0;
				final short fuel = fuel_load[i][j];
				final int bfuel = (int) (255*fuel / 1024.0);
				for( int k = 0; k < 3; ++k ) {
					rgb |= ( (bfuel & 0xff) << k*8 );
				}
				for( int ix = 0; ix < s; ++ix ) {
					for( int iy = 0; iy < s; ++iy ) {
						img.setRGB( s*x + ix, s*y + iy, rgb );
					}
				}
			}
		}
		ImageIO.write( img, "png", f );
	}

//	public String svg()
//	{
//		final int s = 10;
//		final StringBuilder sb = new StringBuilder();
//		sb.append( "<svg width=\"" ).append( s*params.width )
//		  .append( "\" height=\"" ).append( 2*s*params.height ).append( "\">" );
//		int y = 0;
//		for( int i = 0; i < params.width; ++i ) {
//			for( int j = 0; j < params.height; ++j ) {
//				final short age = stand_age[i][j];
//				sb.append( "<rect x=\"" ).append( i*s ).append( "\" y=\"" ).append( j*s + y )
//				  .append( "\" width=\"" ).append( s ).append( "\" height=\"" ).append( s )
//				  .append( "\" style=\"fill:rgb(" )
//				    .append( age ).append( "," ).append( age ).append( "," ).append( age )
//				  .append( ")\" />" );
//			}
//		}
//		y = s*params.height;
//		for( int i = 0; i < params.width; ++i ) {
//			for( int j = 0; j < params.height; ++j ) {
//				final short fuel = fuel_load[i][j];
//				sb.append( "<rect x=\"" ).append( i*s ).append( "\" y=\"" ).append( j*s + y )
//				  .append( "\" width=\"" ).append( s ).append( "\" height=\"" ).append( s )
//				  .append( "\" style=\"fill:rgb(" )
//				    .append( fuel ).append( "," ).append( fuel ).append( "," ).append( fuel )
//				  .append( ")\" />" );
//			}
//		}
//		sb.append( "</svg>" );
//		return sb.toString();
//	}
}
