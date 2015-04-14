import java.net.URI;
import java.net.URISyntaxException;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Properties;
import java.lang.*;

import org.apache.log4j.Logger;
import org.apache.hadoop.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

public class TwitterStreaming {

    /** The actual Twitter stream. It's set up to collect raw JSON data */
    private TwitterStream twitterStream;
    Properties prop = new Properties();
    // FileOutputStream fos;
    //String hdfs_path = "hdfs://localhost:9000/tweets/twitterstream.json";
    String hdfs_path;

    public TwitterStreaming() {

        //load a properties file
        try {
            prop.load(new FileInputStream("twitter4j.properties"));

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(prop.getProperty("oauth.consumerKey"));
            cb.setOAuthConsumerSecret(prop.getProperty("oauth.consumerSecret"));
            cb.setOAuthAccessToken(prop.getProperty("oauth.accessToken"));
            cb.setOAuthAccessTokenSecret(prop.getProperty("oauth.accessTokenSecret"));
            cb.setJSONStoreEnabled(true);
            cb.setIncludeEntitiesEnabled(true);

            twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void startTwitter(double[][] bdbox) {

        // Set up the stream's listener (defined above),
        twitterStream.addListener(listener);

        System.out.println("Starting Twitter streaming...");

        // Set up a filter to pull out industry-relevant tweets
        FilterQuery query = new FilterQuery().locations(bdbox);
        twitterStream.filter(query);

    }

    public void cleanTwitter() {

        System.out.println("Cleaning up Twitter stream...");
        twitterStream.cleanUp();

    }
    public void stopTwitter() {

        System.out.println("Shutting down Twitter stream...");
        twitterStream.shutdown();

    }
    public void setHDFS(String loc) {

    	this.hdfs_path = "/tweets/twitterstream_"+loc+".json";
    }
    StatusListener listener = new StatusListener() {

        // The onStatus method is executed every time a new tweet comes in.
        public void onStatus(Status status) {

           FileSystem hdfs = null;
           Path path = null;
           //FSDataOutputStream os = null;
           BufferedWriter wd = null;
           String outjson = null;
           try {

             Configuration hadoopConf = new org.apache.hadoop.conf.Configuration();
             hadoopConf.addResource(new Path("/usr/local/hadoop/etc/hadoop/core-site.xml"));
             hadoopConf.addResource(new Path("/usr/local/hadoop/etc/hadoop/hdfs-site.xml"));

             hdfs = FileSystem.get(new URI("hdfs://localhost:9000"),hadoopConf);
             path = new Path(new URI(hdfs_path));
             outjson = DataObjectFactory.getRawJSON(status);

                        BufferedReader bfr=new BufferedReader(new InputStreamReader(hdfs.open(path)));//open file first
                        String str = null;
                        BufferedWriter br=new BufferedWriter(new OutputStreamWriter(hdfs.create(path,true)));


                        while ((str = bfr.readLine())!= null)
                        {
                            br.write(str); // write file content
                            br.newLine();
                           //System.out.println("   ->>>>>>>>>>>>  "+str);
                        }

                        System.out.println(outjson);
                        if(status.getGeoLocation() != null){

                        	br.write(outjson);  // append into file
                            br.newLine();
                        }

                        br.close(); // close it

		} //catch (URISyntaxException | IOException e)
		catch (Exception e)
		{ 
			Logger.getRootLogger().error(e);
		}


        }

        // This listener will ignore everything except for new tweets
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
        public void onScrubGeo(long userId, long upToStatusId) {}
        public void onException(Exception ex) {}
        public void onStallWarning(StallWarning warning) {}
    };

    public static void main(String[] args) throws InterruptedException {

      double[][] bdbox = new double[2][2];
      TwitterStreaming twitter = new TwitterStreaming();
      twitter.setHDFS(args[0]);
      System.out.print(twitter.hdfs_path);
      if (args[0].equals("melb")){

          bdbox[0][0] = 143.3454;
          bdbox[0][1] = -38.7847;
          bdbox[1][0] = 146.5248;
          bdbox[1][1] = -36.2211;

      }
      else if (args[0].equals("syd")){

          bdbox[0][0] = 150.3657;
          bdbox[0][1] = -34.2149;
          bdbox[1][0] = 151.3641;
          bdbox[1][1] = -33.3926;

      } 
      else if (args[0].equals("perth")){

          bdbox[0][0] = 115.5989;
          bdbox[0][1] = -33.3873;
          bdbox[1][0] = 116.8664;
          bdbox[1][1] = -31.467;

      }
      else if (args[0].equals("ade")){

          bdbox[0][0] = 138.4509;
          bdbox[0][1] = -35.3724;
          bdbox[1][0] = 139.4191;
          bdbox[1][1] = -34.3705;

      }
      else if (args[0].equals("brisb")){

          bdbox[0][0] = 152.674;
          bdbox[0][1] = -28.1653;
          bdbox[1][0] = 153.5433;
          bdbox[1][1] = -27.0415;

      }
       // double[][] melb_bdbox = {{143.3454,-38.7847}, {146.5248,-36.2211}};
       // double[][] syd_bdbox = {{150.3657,-34.2149}, {151.3641,-33.3926}};
       // double[][] perth_bdbox = {{115.5989,-33.3873}, {116.8664,-31.467}};
       // double[][] ade_bdbox = {{138.4509,-35.3724}, {139.4191,-34.3705}};
       // double[][] brisb_bdbox = {{152.674,-28.1653}, {153.5433,-27.0415}};

        // 	for(int i = 0; i < bdbox.length; i++)
        //  {
        //       for(int j = 0; j < bdbox[0].length; j++)
        //      System.out.print(bdbox[i][j]); 
        //  }
      twitter.startTwitter(bdbox);
      //set the thread sleep for 1 day
      Thread.sleep(86400000);
        //Thread.sleep(Long.MAX_VALUE);
	  twitter.cleanTwitter(); // shutdown internal stream consuming thread
      twitter.stopTwitter();

}

}


