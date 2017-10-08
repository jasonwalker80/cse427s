package stubs;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class TopNMapper extends Mapper<LongWritable, Text, NullWritable, Text> {
	/*
	 * Initialize topN parameter
	 */
  private int N = 0;
  private SortedMap<Integer, String> topN = new TreeMap<Integer, String>();
  
  public void setup(Context context) {
	  Configuration conf = context.getConfiguration(); // get the configuration from the job's context
	  this.N = conf.getInt("N", 0); // retrieve the parameter N, default to zero if not specified
  }
  
  @Override
  public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

	 String line = value.toString().trim();
	 /*
	  * String movieTitle = key.toString();
	  * int ratings = value.get();
	  * 
	  */
	 String[] movieRatingsArray = line.split("\\t");
	 String movieTitle = movieRatingsArray[0];
	 int ratings = Integer.parseInt(movieRatingsArray[1]);
	 String movieRatings = movieTitle + "," + ratings;
	 topN.put(ratings, movieRatings);
	 if (topN.size() > N) {
		 topN.remove(topN.firstKey());
	 }
  }

  @Override
  public void cleanup(Context context) throws IOException, InterruptedException {
	  for (String movieRatings : topN.values()) {
		  context.write(NullWritable.get(),new Text(movieRatings));
	  }
  }
}
