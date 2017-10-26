package stubs;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * On input, the reducer receives a word as the key and a set
 * of locations in the form "play name@line number" for the values. 
 * The reducer builds a readable string in the valueList variable that
 * contains an index of all the locations of the word. 
 */
public class IndexReducer extends Reducer<Text, Text, Text, Text> {

  @Override
  public void reduce(Text key, Iterable<Text> values, Context context)
      throws IOException, InterruptedException {

	  StringBuffer valueList = new StringBuffer();
	  
	  /*
	 * For each value in the set of values passed to us by the mapper:
	 */
		for (Text value : values) {
		  
			/*
		   * Add the value and a comma to the existing list
		   */
			
			if(valueList.length() != 0){ //if first word don't append comma
				valueList.append(",");
			}
			
			valueList.append(value.toString());
		}
		/*
		 * Call the write method on the Context object to emit a key
		 * and a value from the reduce method. 
		 */
		context.write(key, new Text(valueList.toString()));
    
  }
}
