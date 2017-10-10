package stubs;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;

/* 
 * The following is the code for the driver class for aggregating movie ratings
 * This job will take movie ratings as well as a map file of movie titles
 * It will output the sum of the ratings for each movie
 * or the aggregate ratings job the input file is a text file of movie ID
 * the user ID and the users rating of the movie.
 */

public class AggregateRatings extends Configured implements Tool {

	/*
	 *  main function with configuration that executes run
	 */
	
  public static void main(String[] args) throws Exception {
	  int exitCode = ToolRunner.run(new Configuration(), new AggregateRatings(), args);
	  System.exit(exitCode);
  }
  
  public int run(String[] args) throws Exception {
    /*
     * The expected command-line arguments are the paths containing
     * input and output data. Terminate the job if the number of
     * command-line arguments is not exactly 2.
     */
    if (args.length != 2) {
      System.out.printf("Usage: AggregateRatings <input dir> <output dir>\n");
      System.exit(-1);
    }

    /*
     * Instantiate a Job object for your job's configuration.  
     */
    Job job = new Job(getConf());
    
    /*
     * Specify the jar file that contains the driver.
     */
    job.setJarByClass(AggregateRatings.class);
    
    /*
     * Specify an easy to read job name
     */
    job.setJobName("Aggregate Ratings");

    /*
     * Specify the paths to the input and output data based on the
     * command-line arguments.
     */
    FileInputFormat.setInputPaths(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    /*
     * Specify the mapper and reducer classes.
     */
    job.setMapperClass(AggregateRatingsMapper.class);
    job.setReducerClass(SumReducer.class);
     
    /*
     * For the aggregate ratings application, the mapper's output keys and
     * values have the same data types as the reducer's output keys 
     * and values: Text and IntWritable.
     */

    /*
     * Specify the job's output key and value classes.
     */
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    /*
     * Start the MapReduce job and wait for it to finish.
     * If it finishes successfully, return 0. If not, return 1.
     */
    boolean success = job.waitForCompletion(true);
    return success ? 0 : 1;
  }
}
