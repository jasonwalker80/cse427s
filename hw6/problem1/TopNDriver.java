package stubs;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;

/* 
 * This TopNDriver is a MR driver to generate the Top N for a list
 * of text values and Integer values. This class expects the output
 * from another MR job such as AggregateRatings. The configuration
 * for this MR job must define a value for N, the number of top
 * values to return in the final list. Ex. -D N=15
 * 
 * The following is the code for the driver class:
 */
public class TopNDriver extends Configured implements Tool {

	/*
	 * Define the main function to execute tool run with configuration
	 */
  public static void main(String[] args) throws Exception {
	  int exitCode = ToolRunner.run(new Configuration(), new TopNDriver(), args);
	  System.exit(exitCode);
  }
  
  public int run(String[] args) throws Exception {
    /*
     * The expected command-line arguments are the paths containing
     * input and output data. Terminate the job if the number of
     * command-line arguments is not exactly 2.
     */
    if (args.length != 2) {
      System.out.printf("Usage: TopNDriver <input dir> <output dir>\n");
      System.exit(-1);
    }

    /*
     * Instantiate a Job object for your job's configuration.  
     */
    Job job = new Job(getConf());
    
    /*
     * Specify the jar file that contains your driver:
     */
    job.setJarByClass(TopNDriver.class);
    
    /*
     * Specify an easy to read name for the job.
     * This job name will appear in reports and logs.
     */
    job.setJobName("TopN");

    /*
     * Specify the paths to the input and output data based on the
     * command-line arguments.
     */
    FileInputFormat.setInputPaths(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    /*
     * Specify the mapper and reducer classes.
     */
    job.setMapperClass(TopNMapper.class);
    job.setReducerClass(TopNReducer.class);

     
    /*
     * For the topN application, the mapper's output keys and
     * values have the same data types as the reducer's output keys 
     * and values: NullWritable and Text.
     */
    job.setMapOutputKeyClass(NullWritable.class);
    job.setMapOutputValueClass(Text.class);

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
