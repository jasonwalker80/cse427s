package stubs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

/* 
 * To define a map function for your MapReduce job, subclass 
 * the Mapper class and override the map method.
 * The class definition requires four parameters: 
 *   The data type of the input key
 *   The data type of the input value
 *   The data type of the output key (which is the input key type 
 *   for the reducer)
 *   The data type of the output value (which is the input value 
 *   type for the reducer)
 */

public class AggregateRatingsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
	/*
	 * Initialize a map of Movie ID to the Movie Title
	 */
	private Map<Integer, String> movieMap = new HashMap<Integer, String>(); 
	
	/*
	 * Parse the input file to create a mapping of movie ID to Movie Title
	 */
	public void setup(Context context) {
		  File movieMapFile = new File("movie_titles.txt");  
		  BufferedReader movieMapIn = null;
		  try {
		      movieMapIn = new BufferedReader(new InputStreamReader(new FileInputStream(movieMapFile)));
		      String line;
		      while ((line = movieMapIn.readLine()) != null) {
		    	  String movieMapString = line.toString();
		    	  String movieMapLine = movieMapString.trim();
		    	  String[] movieMapArray = movieMapLine.split(",");
		    	  if (movieMapArray.length == 3) {
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
    String lineAsString = value.toString();
    String line = lineAsString.trim();

    /*
     * The line.split(",") call uses regular expressions to split the comma-delimited line
     */
    String[] words = line.split(",");
    /*
     * Only parse lines that were split into three parts, comma-delimited.
     */
    if (words.length == 3) {
        /*
         * Call the write method on the Context object to emit a key
         * and a value from the map method.
         */
    	String ratingString = words[2];
    	double ratingDouble = Double.parseDouble(ratingString);
    	/*
    	 * String[] ratingParts = ratingString.split(".");
    	 */
    	int movieId = Integer.parseInt(words[0]);
    	String movieTitle = null;
    	if (movieMap.containsKey(movieId)) {
    		movieTitle = movieMap.get(movieId);
		
		int rating = (int) ratingDouble;
        	context.write(new Text(movieTitle), new IntWritable(rating));
    	} else {
    		System.out.printf("Unable to find movie title for ID!");
    	}

    }
  }
}
