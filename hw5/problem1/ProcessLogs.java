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

public class ProcessLogs extends Configured implements Tool {

	// implement main method to invoke ToolRunner
	public static void main(String[] args) throws Exception {
		  int exitCode = ToolRunner.run(new Configuration(), new ProcessLogs(), args);
		  System.exit(exitCode);
	  }
	  
	public int run(String[] args) throws Exception {

    if (args.length != 2) {
      System.out.printf("Usage: ProcessLogs <input dir> <output dir>\n");
      System.exit(-1);
    }

    /*
     * Instantiate a Job object for your job's configuration.  
     */
    Job job = new Job(getConf());
    
    job.setJarByClass(ProcessLogs.class);
    job.setJobName("Process Logs");

    /*
     * Specify the paths to the input and output data based on the
     * command-line arguments.
     */
    FileInputFormat.setInputPaths(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    /*
     * Specify the mapper and reducer classes.
     */
    job.setMapperClass(LogFileMapper.class);
    job.setReducerClass(SumReducer.class);
    
    // for testing outputs of the mapper, set to 0
    //job.setNumReduceTasks(0);

    /*
     * Specify the job's output key and value classes.
     */
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    
    boolean success = job.waitForCompletion(true);
    return success ? 0 : 1;
  }
}
