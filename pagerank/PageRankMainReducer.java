package pagerank;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

//input: URL    PageRankScore(current round)
//  or
//input: URL	"PageRankScore|	outURL1 outURL2 outURL3"

//output: URL    PageRankScore(current round) ourURL1 outURL2 outURL3
//Since we need to do the iteration here, so we need to make the output format the same as the mappers' input

public class PageRankMainReducer extends Reducer<Text,Text,Text,Text> {
	public static final double decay = 0.85;
	@Override 
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
		double newScore = 0.0;
		String urlString = "";
		//now we get two kinds of values
		for(Text v : values) {
			String curOperation = v.toString();
			if(curOperation.startsWith("Score|")) { //make sure curOperation's length == 2
				if(curOperation.split("\002", 2).length == 1) { //Score| then nothing follows
					urlString = "";
				} else {
					urlString = curOperation.split("\002", 2)[1].replaceAll("\\s+$", "");
				}
			} else {
				try {
					newScore += decay * Double.parseDouble(v.toString());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}	
		newScore += 1 - decay;
		if(urlString != null && urlString != "" && urlString != " ") { //has outURLs
			context.write(key, new Text("Score|" + newScore + "\002" + urlString));
		} else {
			context.write(key, new Text("Score|" + newScore));
		}
	}
}
