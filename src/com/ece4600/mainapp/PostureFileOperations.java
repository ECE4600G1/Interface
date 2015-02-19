package com.ece4600.mainapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class PostureFileOperations {
	 /*Notes:
	  * Time is measured in ms
	  * 
	  * */
	  private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CANADA);
	  private final int sampleRate = 300;
	  
	  private final String PATH = Environment.getExternalStorageDirectory() + "/wellNode/Posture";
	  private Time now = new Time();
	  
	   public PostureFileOperations(){
		   createPostureFolder();
	   }
	   
	   public void writeHeader(String fname, String userName, String date){
		   String time;
		      try {
		          
		          File file = new File(PATH, fname);
		          // If file does not exists, then create it
		          if (!file.exists()) {
		            file.createNewFile();
		          }
		          FileWriter fw = new FileWriter(file.getAbsolutePath(),true);
		          BufferedWriter bw = new BufferedWriter(fw);
		          
		         
		          String writeThis;
		          writeThis =  "Posture Recognition" + "\r\n" ;
		          fw.append(writeThis);
		          
		          writeThis =  "File name: " + fname;
		          fw.append(writeThis);
		          fw.append("\r\n\r\n");
		          
		          writeThis =  "Patient name: " + userName;
		          fw.append(writeThis);
		          fw.append("\r\n");
		          
		          writeThis =  "Date: " + date;
		          fw.append(writeThis);
		          fw.append("\r\n");
		          
		          time = getTimeStamp();
		          writeThis =  "Time: " + time;
		          fw.append(writeThis);
		          fw.append("\r\n\r\n");
		          
		          
		          
		          
		          fw.append("\r\n");
		          fw.append("Time Stamp,Duration,Posture #,Posture");
		          fw.append("\r\n");
		          
		          //fw.append(time + ",");
		          fw.close();
		          Log.d("Suceess","Sucess");
		          
		          return ;
		        } catch (IOException e) {
		          e.printStackTrace();
		          return ;
		        }
	   }
	   
	   
	   public void write(String fname, double postureTime, String postureStr, short postureNum, Boolean STOP, Boolean firstTime){
		  
	      try {
	        
	        File file = new File(PATH, fname);
	        // If file does not exists, then create it
	        if (!file.exists()) {
	          file.createNewFile();
	        }
	        FileWriter fw = new FileWriter(file.getAbsolutePath(),true);

	        //Time Stamp, Duration,Posture #, Posture	
	        if (firstTime){
	        	fw.append(getTimeStamp() + ",");
	        } else{
	        
	    	fw.append(postureTime + ",");
	    	fw.append(postureNum + ",");
	    	fw.append(postureStr);
	    	fw.append("\r\n");
	    	if(!STOP){
	    	fw.append(getTimeStamp() + ",");
	    	}
	        }
	        fw.close();
	   
	        return ;
	      } catch (IOException e) {
	        e.printStackTrace();
	        return ;
	      }
	   }
	   
	  
	   
	   public void write2(String fName, String fcontent, String fPath, int recordState){
		   File myFile = new File(fPath, fName);
		   
		  
	   }
	   //
	   
	   
	   public String getTimeStamp(){
		   now.setToNow();
	       String time = "";
		   /*Calendar c = Calendar.getInstance();
		   time = sdf.format(c.getTime());*/
	       time = now.format("%H:%M:%S");
			 
		    return time;
	   }
	   
	   /**
	    * Return date in specified format.
	    * @param milliSeconds Date in milliseconds
	    * @param dateFormat Date format 
	    * @return String representing date in specified format
	    */
	   public static String getDate(long milliSeconds, String dateFormat)
	   {
	       // Create a DateFormatter object for displaying date in specified format.
	       SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

	       // Create a calendar object that will convert the date and time value in milliseconds to date. 
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTimeInMillis(milliSeconds);
	        return formatter.format(calendar.getTime());
	   }

		private void createPostureFolder(){
	  		if(!(new File(PATH)).exists()) 
	  		new File(PATH).mkdirs();
	  	}
}
