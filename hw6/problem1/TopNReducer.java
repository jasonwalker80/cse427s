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

/* 
 * Define a reduce function for our TopN MapReduce job. 
 */   
public class TopNReducer extends Reducer<NullWritable, Text, IntWritable, Text> {

	private int N = 0; //Initialize the N value for topN, default is zero
	/*
	 * Initialize a sorted Tree Map object to retain the topN values
	 * if we exceed N we can reduct the size of this data structure
	 * we remove the first key, value since it's sorted.
	 */
	private SortedMap<Integer, String> topN = new TreeMap<Integer, String>();
	
	  public void setup(Context context) {
		  Configuration conf = context.getConfiguration(); // get the configuration from the job's context
		  this.N = conf.getInt("N", 0); // retrieve the parameter N, default to zero if not specified
	  }
  /*
   * The reduce method runs once for each key received from
   * the shuffle and sort phase of the MapReduce framework.
   * The method receives a NullWritable key and a set of values of type
   * Text, and a Context object.
   */
  @Override
	public void reduce(NullWritable key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		
		/*
		 * For each text value in the set of values passed to us by the mapper:
		 */
		for (Text value : values) {
			/*
			 * Parse the value as a string while removing leading/trailing whitespace
			 */
			String movieRatings = value.toString().trim();
			/*
			 * The expected format of the string is a comma-delimited string
			 * The first value is the movieTitle, the second is the sum of the ratings
			 */
			String[] movieRatingsArray = movieRatings.split(",");
			String movieTitle = movieRatingsArray[0];
			int ratings = Integer.parseInt(movieRatingsArray[1]);
			/*
			 * Add the ratings value as the key of our sorted TreeMap
			 */
			topN.put(ratings,movieTitle);
			/*
			 * If we have exceeded the size of our final list, N
			 * then remove the first key, value pair, sorted by key
			 * therefore the smallest will be remove from the list.
			 */
			if (topN.size() > N) {
				topN.remove(topN.firstKey());
			}
		}
		
		/*
		 * Iterate over the list of keys in our set
		 * Call the write method on the Context object to emit a key
		 * and a value from the reduce method. 
		 */
		List<Integer> keys = new ArrayList<Integer>(topN.keySet());
		for (int i=keys.size()-1; i >= 0; i--) {
			context.write(new IntWritable(keys.get(i)), new Text(topN.get(keys.get(i))));
		}
	}
}