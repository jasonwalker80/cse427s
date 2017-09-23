package stubs;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AverageReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {
	private DoubleWritable averageLength = new DoubleWritable();
  @Override
  public void reduce(Text key, Iterable<IntWritable> values, Context context)
      throws IOException, InterruptedException {

	  int wordLength = 0;
	  int count = 0;
	  /*
	   * For each value in the set of values passed to us by the mapper:
	   */
      for (IntWritable value : values) {
    	  /*
    	   * Add the length value to the total wordLength for this key.
    	   * Also, count the occurrences of the key
    	   */
    	  wordLength += value.get();
    	  count += 1;
      }	
	
      averageLength.set( (double) wordLength / count);
		/*
		 * Call the write method on the Context object to emit a key
		 * and a value from the reduce method. 
		 */
		context.write(key, averageLength);

  }
}