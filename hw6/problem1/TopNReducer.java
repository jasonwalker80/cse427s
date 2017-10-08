package stubs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

/* 
 * To define a reduce function for your MapReduce job, subclass 
 * the Reducer class and override the reduce method.
 * The class definition requires four parameters: 
 *   The data type of the input key (which is the output key type 
 *   from the mapper)
 *   The data type of the input value (which is the output value 
 *   type from the mapper)
 *   The data type of the output key
 *   The data type of the output value
 */   
public class TopNReducer extends Reducer<NullWritable, Text, IntWritable, Text> {

	private int N = 0; //Initialize the N value for topN, default is zero
	private SortedMap<Integer, String> topN = new TreeMap<Integer, String>();
	
	  public void setup(Context context) {
		  Configuration conf = context.getConfiguration(); // get the configuration from the job's context
		  this.N = conf.getInt("N", 0); // retrieve the parameter N, default to zero if not specified
	  }
  /*
   * The reduce method runs once for each key received from
   * the shuffle and sort phase of the MapReduce framework.
   * The method receives a key of type Text, a set of values of type
   * IntWritable, and a Context object.
   */
  @Override
	public void reduce(NullWritable key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		
		/*
		 * For each value in the set of values passed to us by the mapper:
		 */
		for (Text value : values) {
			String movieRatings = value.toString().trim();
			String[] movieRatingsArray = movieRatings.split(",");
			String movieTitle = movieRatingsArray[0];
			int ratings = Integer.parseInt(movieRatingsArray[1]);
			topN.put(ratings,movieTitle);
			if (topN.size() > N) {
				topN.remove(topN.firstKey());
			}
		}
		
		/*
		 * Call the write method on the Context object to emit a key
		 * and a value from the reduce method. 
		 */
		List<Integer> keys = new ArrayList<Integer>(topN.keySet());
		for (int i=keys.size()-1; i >= 0; i--) {
			context.write(new IntWritable(keys.get(i)), new Text(topN.get(keys.get(i))));
		}
	}
}