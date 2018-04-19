
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class MovieMax 
{
	public static class TokenizerMapper extends Mapper<Object, Text, Text, FloatWritable>
	{
		private Text movie_name = new Text();
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException 
		{
			for (String movie : value.toString().split("\n") ) {
				System.out.println(movie);
				System.out.println("\n\n\n\n\n\n\n\n\n");
				String[] splitted = movie.split(";");
                                //IntWritable rating = new IntWritable(Integer.parseInt(splitted[1]));
				FloatWritable rating = new FloatWritable(Float.parseFloat(splitted[1]));
				movie_name.set(splitted[0]);
				context.write(movie_name, rating);
				System.out.println(splitted[0] + " " + splitted[1]);
			}
		}
	}
  
	public static class IntSumReducer extends Reducer<Text, FloatWritable, Text, Text> 
	{
		private Text result = new Text();
		public void reduce(Text key, Iterable<FloatWritable> values, Context context) throws IOException, InterruptedException 
		{
			Float max = new Float(-1.0), min = new Float(10.0);
			for (FloatWritable val : values) {
				if (val.get() > max) max = val.get();
				if (val.get() < min) min = val.get();
			}
			System.out.println("Max - " + max.toString());
			result.set(max.toString() + "" + min.toString());
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception 
	{
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) 
		{
			System.err.println("Usage: MovieMax <in> <out>");
			System.exit(2);
		}
    
		Job job = new Job(conf, "movie max");
		
		job.setJarByClass(MovieMax.class);
		job.setMapperClass(TokenizerMapper.class);
		
		//job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(FloatWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
    
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
    
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
