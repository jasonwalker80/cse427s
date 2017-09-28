package stubs;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.junit.Test;

public class SentimentPartitionTest {

	SentimentPartitioner mpart;

	@Test
	public void testSentimentPartition() {
		
		Configuration conf = new Configuration();
		try {
			DistributedCache.addCacheFile(new URI("positive-words.txt"), conf);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			DistributedCache.addCacheFile(new URI("negative-words.txt"), conf);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SentimentPartitioner spart= new SentimentPartitioner();
		spart.setConf(conf);
		int result;		
		
		/*
		 * Test the words "love", "deadly", and "zodiac". 
		 * The expected outcomes should be 0, 1, and 2. 
		 */
        
 		/*
		 * TODO implement
		 */          
		result = spart.getPartition(new Text("deadly"), new IntWritable(1), 3);
		
		assertEquals(0, result);
	}

}
