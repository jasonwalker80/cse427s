package stubs;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class LetterMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	/*
	 * Initialize objects for length and letter. Also initialize variable for case sensitivity
	 */
  private IntWritable lengthObject = new IntWritable();
  private Text letterObject = new Text();
  private boolean caseSensitive;
  
  public void setup(Context context) {
	  Configuration conf = context.getConfiguration(); // get the configuration from the job's context
	  caseSensitive = conf.getBoolean("caseSensitive", true); // retrieve the parameter caseSensitive, default to true if not specified
  }
  
  @Override
  public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

	/*
	 * Convert the line, which is received as a Text object,
	 * to a String object.
	 */
	 String line = value.toString();

	/*
	 * The line.split("\\W+") call uses regular expressions to split the
	 * line up by non-word characters.
	 */
	 for (String word : line.split("\\W+")) {
		 /*
		  * Only process words that have length greater than zero
		  */
		if (word.length() > 0) {
			/*
			 * Pull the first letter off the word with substring
			 */
			String letter = word.substring(0, 1);
			if (caseSensitive) {
				/*
				 * In caseSensitive mode, do not lower case
				 */
				letterObject.set(letter);
			} else {
				/*
				 * when case INsensitive, lowerCase the letter
				 */
				letterObject.set(letter.toLowerCase());
			}
			lengthObject.set(word.length());
		  
			/*
			 * Call the write method on the Context object to emit a key
			 * and a value from the map method.
			 */
			context.write(letterObject, lengthObject);
		}	
	 }
  }
}
