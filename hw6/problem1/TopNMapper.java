package stubs;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TopNMapper extends Mapper<LongWritable, Text, NullWritable, Text> {
	/*
	 * Initialize topN parameter
	 */
  private int N = 0;
  /*
   * Initialize a TreeMap to store the top values sorted
   * we can then remove the first item if we exceed N as it has the smallest value
   */
  private SortedMap<Integer, String> topN = new TreeMap<Integer, String>();
  
  public void setup(Context context) {
	  Configuration conf = context.getConfiguration(); // get the configuration from the job's context
	  this.N = conf.getInt("N", 0); // retrieve the parameter N, default to zero if not specified
  }
  
  @Override
  public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

	  /*
	   * Parse each value as a string and remove any leading or trailing whitespace characters
	   */
	 String line = value.toString().trim();

	 /*
	  * Split the line by a tab character
	  * The movieTitle is expected in column1 and
	  * the movie rating sum is expected as an integer in column2
	  */
	 String[] movieRatingsArray = line.split("\\t");
	 String movieTitle = movieRatingsArray[0];
	 int ratings = Integer.parseInt(movieRatingsArray[1]);
	 /*
	  * cat the movieTitle and rating together as a comma-delimited string 	
	  */
	 String movieRatings = movieTitle + "," + ratings;
	 /*
	  * Add this record to our TreeMap object
	  */
	 topN.put(ratings, movieRatings);
	 /*
	  * If we have exceeded the size of our final list
	  * remove the first item. Since this object stores the keys sorted
	  * we can simply remove the first key in the data structure to reduce
	  * the total size by one.
	  */
	 if (topN.size() > N) {
		 topN.remove(topN.firstKey());
	 }
  }

  @Override
  /*
   * After all map tasks have been run, output the values stored in our TreeMap data object
   */
  public void cleanup(Context context) throws IOException, InterruptedException {
	  for (String movieRatings : topN.values()) {
		  /*
		   * For each record in the topN object we will output the NullWrittable object
		   * as well as the movieRatings string with format: title,rating
		   */
		  context.write(NullWritable.get(),new Text(movieRatings));
	  }
  }
}
