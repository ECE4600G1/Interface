package com.ece4600.mainapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.ece4600.mainapp.JSONParser;
//import com.ece4600.mainapp.ServerService.Task;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ServerService extends Service {
	// get values from shared preference
	public SharedPreferences postureSettings;
	public SharedPreferences.Editor editor;
//	public SharedPreferences settings;
	
	public void setUpPreferences(){
    	
    	postureSettings = getSharedPreferences("posturePrefs", MODE_PRIVATE );
    	editor = postureSettings.edit();
    }
	
	public void restorePreferences(){

		
	}
	
	////
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	ScheduledFuture timerHandle;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	
	@Override
    public void onCreate() {
        Toast.makeText(this, "The new Service was Created", Toast.LENGTH_LONG).show();
      //  new Thread(new Task()).start();
        setUpPreferences();
	     Calendar c = Calendar.getInstance(); // testing only will be removed later. 
	     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//
	     String strDate = sdf.format(c.getTime());//
        //hardcoding posture for testing purpose
        editor.putString("posture","stand2,"+strDate+"/sit3,"+strDate+"/test4,"+strDate);
        editor.putInt("counter", 3);
     // editor.
        editor.commit();

        
        
        PeriodicUpdate(20);
    }
	
    public void PeriodicUpdate(long period) {
        final Runnable updateDatabase = new Runnable() {
            public void run() {
                
            	String temp = postureSettings.getString("posture","error");
            	String temp_pair; 
                String[] ss=temp.split("/");
                int totalLength = ss.length;
                for(int i=0;i<ss.length;i++)
                {
                    System.out.println(ss[i]);
                }
            	// String[][] arrays = { array1, array2, array3, array4, array5 };
            	
                
                
                Log.d("update", "New postureupdate");
                int success=0;
        	 	final String LOGIN_URL = "http://wellnode.ca/webservice/posture.php";
        	 	setUpPreferences();
        	    final String TAG_SUCCESS = "success";
        		
        	    JSONParser jsonParser = new JSONParser();
        	     
        	     
//        	     Calendar c = Calendar.getInstance(); // testing only will be removed later. 
//        	     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//
//        	     String strDate = sdf.format(c.getTime());//
        	     
        	     String username = postureSettings.getString("username","error"); // 
        	     String posture = postureSettings.getString("posture","error"); // 
        	     
        	     //loop start


                
        	     
        	     try {
        	         // Building Parameters
        	    	 
        	    	 //loop starts here
                     for(int j=0; j<totalLength; j++)
                     
                     {
                    	 
                     System.out.println("Count is: " + j);
                     temp_pair = ss[j];
                     String[] temp_pair_holder = temp_pair.split(",");
        	    	 
        	         List<NameValuePair> params = new ArrayList<NameValuePair>();
        	         params.add(new BasicNameValuePair("username", username)); // pass shared preference here 
        	         params.add(new BasicNameValuePair("timetag", temp_pair_holder[1])); // however we need another service that logs everything for 5 seconds or more then save it in array
        	         params.add(new BasicNameValuePair("posture", temp_pair_holder[0])); // then parse it and send it via jsonparser. // 


        	         
        	         // getting product details by making HTTP request
        	         JSONObject json = jsonParser.makeHttpRequest(
        	                LOGIN_URL, "POST", params);
                     
                     Log.d("upload", "starting");
                     // check your log for json response
        	         Log.d("Registering", json.toString());
                     
        	         // json success tag
        	         success = json.getInt(TAG_SUCCESS);
                     }
        	         
        	         
        	         // loop ends here
        	         
        	         if (success == 1) {
        	//             firstname_database = json.getString(TAG_FIRSTNAME);
        	//             lastname_database = json.getString(TAG_LASTNAME);
        	        	 Log.d("Login Successful!", "yes");
        	         	//Intent i = new Intent(Login.this, ReadComments.class);

        	         	return;
        	         }else{
        	        	
        	         	Log.d("Login Failure!","");
        	       //  	Toast.makeText(getApplicationContext(), json.getString(TAG_MESSAGE),Toast.LENGTH_LONG).show();
        	         	return;
        	         	
        	         }
        	     } 
        	     
                 
        	     
        	    
        	     
        	     
        	     
        	     catch (JSONException e) {
        	         
        	    	 e.printStackTrace();
        	     }
        	     
        	     
        	     
                 
        	      //loop ends

        	     return;
            }
        };
        
        timerHandle = scheduler.scheduleAtFixedRate(updateDatabase, period, period,TimeUnit.SECONDS);
    }
	
	
	
//	class Task implements Runnable {
//		@Override
//		public void run() {
//			
//
//			// TODO Auto-generated method stub
//			 // Check for success tag
//	     int success;
//	     String username;
//	 	final String LOGIN_URL = "http://wellnode.ca/webservice/posture.php";
//
//	    final String TAG_SUCCESS = "success";
//		
//	    JSONParser jsonParser = new JSONParser();
//	     
//	     
//	     Calendar c = Calendar.getInstance();
//	     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	     String strDate = sdf.format(c.getTime());
//	     username = postureSettings.getString("username","error");
//	    
//	     try {
//	         // Building Parameters
//	    	 
//	    	 //transmission of data starts here///
//	         List<NameValuePair> params = new ArrayList<NameValuePair>();
//	         params.add(new BasicNameValuePair("username", username));
//	         params.add(new BasicNameValuePair("timetag", strDate));
//	         params.add(new BasicNameValuePair("posture", "bend"));
//
//
//	         Log.d("upload", "starting");
//	         // getting product details by making HTTP request
//	         JSONObject json = jsonParser.makeHttpRequest(
//	                LOGIN_URL, "POST", params);
//
//	         // check your log for json response
//	         Log.d("Registering", json.toString());
//
//	         // json success tag
//	         success = json.getInt(TAG_SUCCESS);
//
//	         
//	         if (success == 1) {
//	//             firstname_database = json.getString(TAG_FIRSTNAME);
//	//             lastname_database = json.getString(TAG_LASTNAME);
//	        	 Log.d("Login Successful!", json.toString());
//	         	//Intent i = new Intent(Login.this, ReadComments.class);
//
//	         	return;
//	         }else{
//	        	
//	         	Log.d("Login Failure!","");
//	       //  	Toast.makeText(getApplicationContext(), json.getString(TAG_MESSAGE),Toast.LENGTH_LONG).show();
//	         	return;
//	         	
//	         }
//	     } catch (JSONException e) {
//	         
//	    	 e.printStackTrace();
//	     }
//
//	     return;
//			
//		
//			
//			
//			
//		}
//
//	}
//	
	
	
    @Override
    public void onStart(Intent intent, int startId) {
    	// For time consuming an long tasks you can launch a new thread here...
        Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();
       
   
    }
 
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        scheduler.shutdown();
        super.onDestroy();
    }
}
