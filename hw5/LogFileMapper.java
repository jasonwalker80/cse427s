package stubs;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Example input line:
 * 96.7.4.14 - - [24/Apr/2011:04:20:11 -0400] "GET /cat.jpg HTTP/1.1" 200 12433
 *
 */
public class LogFileMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

  @Override
  public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

	  /*
	     * Convert the line, which is received as a Text object,
	     * to a String object.
	     */
	    String line = value.toString();

	    /*
	     * We need to split the string to pull the IP address out.
	     * There are several ways to do this:
	     * Find first dash, take to the left
	     * Here i have opted for the regex to ensure we get a good value
	     */
	    Pattern p = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");
	    Matcher m = p.matcher(line);
	    
	    /*
	     * The first record in the array should be IP address if split properly
	     */
	    
	    if(m.find()){
		    	String ipAddress = m.group(0);
			    /*
		         * Call the write method on the Context object to emit a key
		         * and a value from the map method. Here we write 1 back for a simple sum
		         */
		        context.write(new Text(ipAddress), new IntWritable(1));
	    }
  }
}
