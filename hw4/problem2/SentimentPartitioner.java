package stubs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Partitioner;

public class SentimentPartitioner extends Partitioner<Text, IntWritable> implements
    Configurable {
	/*
	 * Initialize configuration variable and positive/negative word sets
	 */

  private Configuration configuration;
  Set<String> positive = new HashSet<String>();
  Set<String> negative = new HashSet<String>();


  @Override
  public void setConf(Configuration configuration) {
	  /*
	   * set the configuration on this object
	   */

	  this.configuration = configuration;
	  
	  /*
	   * Read the positive word list and add to positive set
	   */
	  File positiveFile = new File("positive-words.txt");
	  BufferedReader positiveIn = null;
	  try {
	      positiveIn = new BufferedReader(new InputStreamReader(new FileInputStream(positiveFile)));
	      String line;
	      while ((line = positiveIn.readLine()) != null) { //ensure file line is not empty
	        if (line.matches("^;")) {
	        	/*
	        	 * ignore lines starting with ;
	        	 */
	        } else {
	        	positive.add(line.toString());
	        } 
	      }
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	      IOUtils.closeStream(positiveIn);
	    }		
	  
	  	/*
	  	 * Parse the negative word list and add to the negative set
	  	 */
	  	File negativeFile = new File("negative-words.txt");	
	  	BufferedReader negativeIn = null;
	  	try {
	      negativeIn = new BufferedReader(new InputStreamReader(new FileInputStream(negativeFile)));
	      String line;
	      while ((line = negativeIn.readLine()) != null) { //ensure file line is not empty
	        if (line.matches("^;")) {
	        	/*
	        	 * ignore lines starting with ;
	        	 */
	        } else {
	        	negative.add(line.toString());
	        } 
	      }
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	      IOUtils.closeStream(negativeIn);
	    }	
  }

  /**
   * Implement the getConf method for the Configurable interface.
   */
  @Override
  public Configuration getConf() {
    return configuration;
  }

  /*
   * getPartition receives the words as keys (i.e., the output key from the mapper.)
   * It returns an integer representation of the sentiment category
   * (positive, negative, neutral). We also check that 3 reducers have been specified and fail otherwise.
   */
  
  public int getPartition(Text key, IntWritable value, int numReduceTasks) {
	  /*
	   * Fail unless we have exactly three reducers
	   */
	  if ( numReduceTasks != 3 ) {
		  System.out.printf("Must define exactly 3 reducers in job configuration!");
	      System.exit(1);
	  }
	  /* 
	   * Test each condition, positive, negative and if not found assume neutral
	   * If the word is found in the first set do not look in the other sets
	   */
	  if ( positive.contains( key.toString() ) ) {
		  /*
		   * positive word, return 0 for reducer
		   */
		  return 0;
	  } else if ( negative.contains( key.toString() ) ) {
		  /* 
		   * negative word, return 1 for reducer 
		   */
		  return 1;
	  } else {
		  /*
		   * neutral word, return 2 for reducer
		   */
		  return 2;
	  }
  }
}
