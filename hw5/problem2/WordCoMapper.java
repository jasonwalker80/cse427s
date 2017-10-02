package stubs;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class WordCoMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	private static String separator = ", ";
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
	   * 
	   * If you are not familiar with the use of regular expressions in
	   * Java code, search the web for "Java Regex Tutorial." 
	   */
	  String[] words = line.split("\\W+");
	  
	  for (int i=0; i < (words.length-1); i++) {
		  String word1 = words[i];
		  String word2 = words[i+1];
		  String wordSeparator = word1.concat(separator);
		  String wordPair = wordSeparator.concat(word2);
		  /*
		   * Call the write method on the Context object to emit a key
		   * and a value from the map method.
		   */
		  context.write(new Text(wordPair.toLowerCase()), new IntWritable(1));
	  }
	  
  }	
}
