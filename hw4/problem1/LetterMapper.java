package stubs;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class LetterMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
  private IntWritable lengthObject = new IntWritable();
  private Text letterObject = new Text();
  private boolean caseSensitive;
  
  public void setup(Context context) {
	  Configuration conf = context.getConfiguration();
	  caseSensitive = conf.getBoolean("caseSensitive", true);
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
		if (word.length() > 0) {
			String letter = word.substring(0, 1);
			if (caseSensitive) {
				letterObject.set(letter);
			} else {
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
