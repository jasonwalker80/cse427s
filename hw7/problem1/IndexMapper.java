package stubs;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class IndexMapper extends Mapper<Text, Text, Text, Text> {

  @Override
  public void map(Text key, Text value, Context context) throws IOException,
      InterruptedException {

    /*
     * Convert the line, which is received as a Text object,
     * to a String object.
     */
    String line = value.toString();
    
    /*
     * Retrieve the filename being processed
     */
    FileSplit split = (FileSplit)context.getInputSplit();
    String filename = split.getPath().getName();
    
    /*
     *  concatenate the string to emit
     */
    String outputValue = filename + "@" + key.toString();

    /*
     * Split the line of text via regex
     */
    for (String word : line.split("\\W+")) {
      if (word.length() > 0) {
        /*
         * Call the write method on the Context object to emit a key
         * and a value from the map method.
         */
        context.write(new Text(word.toLowerCase()), new Text(outputValue));
      }
    }
    
  }
}
