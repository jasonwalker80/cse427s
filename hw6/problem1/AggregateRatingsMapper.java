package stubs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/* 
 * dDefine a map function for the AggregateRatings MapReduce job
 */

public class AggregateRatingsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	/*
	 * Initialize a map of Movie ID to the Movie Title
	 */
	private Map<Integer, String> movieMap = new HashMap<Integer, String>(); 
	
	/*
	 * During the setup function of the Map class,
	 * parse the input file to create a mapping of Movie ID to Movie Title
	 * This HashMap is used later to lookup the Movie Title by ID
	 */
	public void setup(Context context) {
		/*
		 * Load the file object. Hadoop manages the location of the file for us
		 * Only a relative path is required to load this file.
		 */
		  File movieMapFile = new File("movie_titles.txt");  
		  BufferedReader movieMapIn = null;
		  try {
			  /*
			   * Read each line of the file with a BufferedReader
			   */
		      movieMapIn = new BufferedReader(new InputStreamReader(new FileInputStream(movieMapFile)));
		      String line;
		      while ((line = movieMapIn.readLine()) != null) {
		    	  /*
		    	   * Parse the line as a string and trim whitespace
		    	   */
		    	  String movieMapLine = line.toString().trim();
		    	  /*
		    	   * Split the line on a comma and assume it has 3 columns
		    	   */
		    	  String[] movieMapArray = movieMapLine.split(",");
		    	  if (movieMapArray.length == 3) {
		    		  /*
		    		   * The movie ID is in column 1 and the movie Title is in column3
		    		   * Add the mapping to the HashMap object with the ID as the key.
		    		   */
		    		  int movieId = Integer.parseInt(movieMapArray[0]);
		    		  String movieTitle = movieMapArray[2];
		    		  movieMap.put(movieId, movieTitle);
		    	  }
		        } 
		    } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
		      IOUtils.closeStream(movieMapIn);
		    }
	  }
  /*
   * The map method runs once for each line of text in the input file.
   * The method receives a key of type LongWritable, a value of type
   * Text, and a Context object.
   */
	
	
  @Override
  public void map(LongWritable key, Text value, Context context)
      throws IOException, InterruptedException {

    /*
     * Convert the line, which is received as a Text object,
     * to a String object and remove leading or trailing whitespace
     */
    String line = value.toString().trim();

    /*
     * The line.split(",") call uses regular expressions to split the comma-delimited line
     */
    String[] words = line.split(",");
    /*
     * Only parse lines that were split into three parts, comma-delimited.
     */
    if (words.length == 3) {
    	/*
    	 * The movie ID is the first column of the comma-delimited record
    	 */
    	int movieId = Integer.parseInt(words[0]);
    	/*
    	 * The rating value is in the 3rd column
    	 * An example value is 5.0 and hence must be parsed as a string.
    	 * However, all of the values in the file are integer values
    	 */
    	String ratingString = words[2];
    	double ratingDouble = Double.parseDouble(ratingString);

    	/*
    	 * Initialize the movie Title variable since it may not be defined
    	 */
    	String movieTitle = null;
    	if (movieMap.containsKey(movieId)) {
    		movieTitle = movieMap.get(movieId);
    		/*
    		 * The double ratings value can be cast as an integer
    		 */
        	int rating = (int) ratingDouble;        
        	/*
             * Call the write method on the Context object to emit a key
             * and a value from the map method.
             */
            context.write(new Text(movieTitle), new IntWritable(rating));
    	} else {
    		/*
    		 * Print an error message that the movie title was not found.
    		 */
    		System.out.printf("Unable to find movie title for ID!");
    	}
    }
  }
}