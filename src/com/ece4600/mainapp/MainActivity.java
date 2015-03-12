package com.ece4600.mainapp;

import org.achartengine.GraphicalView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
 

public class MainActivity extends Activity {
//	public static final String RECEIVE_JSON = "com.your.ece4600.RECEIVE_JSON";
	private PosturePie pieChart = new PosturePie();
	private static Context context;
	
	public SharedPreferences settings;
	public SharedPreferences.Editor editor,postureEditor;
	
	public SharedPreferences postureSettings;
	public OnSharedPreferenceChangeListener posturesListen;
	
	TextView  dob, weight,name,sex;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        
 	  	//starting Database upload here
  	  startService(new Intent(this, ServerService.class));
        
        
        //database related  starts here
//        user_name = (TextView)findViewById(R.id.name);
//        user_gender = (TextView)findViewById(R.id.gender);
//        
//
//        
//        Bundle extras = getIntent().getExtras();
//        if (extras != null) {		    
//        	
//        	String username = extras.getString("database_user");
//		    String address = extras.getString("database_address");
//
//		    user_name.setText("Name: " + username);
//		    user_gender.setText("Address: " +address);
//		}
//        
        
        //database related ends here
//        
    	context = getBaseContext();
		pieChart.initialize();
		pieChart.updateData(1,1,1,1);
		paintGraph();

        setupMessageButton1();
        setupMessageButton2();
        setupMessageButton3();
        setupMessageButton4();
        
        
        name = (TextView)findViewById(R.id.name);
        sex = (TextView)findViewById(R.id.gender);
        dob = (TextView)findViewById(R.id.DOB);
        weight = (TextView)findViewById(R.id.weightMain);
        
  	  	setUpPreferences();
  	  	restorePreferences();
  	  	setUpPostureListener();
  	  	
 
    }

    @Override
	 protected void onDestroy() {
	  super.onDestroy();
	  //un-register BroadcastReceiver
	  postureSettings.unregisterOnSharedPreferenceChangeListener(posturesListen);
	  
	  
	  stopService( new Intent(MainActivity.this,  btMateService.class));
	  stopService( new Intent(MainActivity.this,  bleService.class));

		

	 }

	

	@Override
	
	protected void onResume() {
	super.onResume();
	postureSettings.registerOnSharedPreferenceChangeListener(posturesListen);
	}
    private void setupMessageButton1(){
    	Button messageButton = (Button)findViewById(R.id.heart);
    	messageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(MainActivity.this, "Heart rate", Toast.LENGTH_LONG).show();
				startActivity(new Intent(MainActivity.this, Heartrate.class));
				//finish();
			}
		});	
    }
    
    private void setupMessageButton2(){
    	Button messageButton = (Button)findViewById(R.id.pedo);
    	messageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(MainActivity.this, "Pedometer", Toast.LENGTH_LONG).show();
				startActivity(new Intent(MainActivity.this, Pedometer.class));
				//finish();
			}
		});	
    }
    
    private void setupMessageButton3(){
    	Button messageButton = (Button)findViewById(R.id.loca);
    	messageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(MainActivity.this, "Location", Toast.LENGTH_LONG).show();
				startActivity(new Intent(MainActivity.this, Location.class));
				//finish();
			}
		});	
    }
    
    private void setupMessageButton4(){
    	Button messageButton = (Button)findViewById(R.id.post);
    	messageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(MainActivity.this, "Posture", Toast.LENGTH_LONG).show();
				startActivity(new Intent(MainActivity.this, Posture.class));
				//finish();
			}
		});	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	super.onOptionsItemSelected(item);
    	switch(item.getItemId()){
  
    	case R.id.mainmenu_logout:
    		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
    		startActivity(new Intent(this, Login.class));
    		stopService(new Intent(this, ServerService.class));
    		finish();
    		break;
    	case R.id.action_settings:
    		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
    		startActivity(new Intent(this, Bluetooth.class));
    		finish();
    		break;
    		
    	}
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return true; 
    }
    
    public void paintGraph(){
		//Get Graph information:
		GraphicalView lineView = pieChart.getView(context);
		//Get reference to layout:
		LinearLayout layout =(LinearLayout)findViewById(R.id.posturePie);
		//clear the previous layout:
		layout.removeAllViews();
		//add new graph:
		if (layout != null)
				layout.addView(lineView);
	}
//    
    
	public void setUpPreferences(){
    	settings = getSharedPreferences("userPrefs", MODE_PRIVATE);
    	editor = settings.edit();
    	
    	postureSettings = getSharedPreferences("posturePrefs", MODE_PRIVATE );
    	postureEditor = postureSettings.edit();
    	//postureEditor.putString("fileName", "");
    	//postureEditor.commit();
    	//Log.e("Main","main set up preferences");
    }
	
	public void restorePreferences(){
		int temp;
		name.setText("NAME: " + settings.getString("name", "Mike"));
		dob.setText("D.O.B.: " + settings.getString("DOB", "MM/DD/YYYY"));
		weight.setText("WEIGHT: " + settings.getString("weight", "xxx"));
		sex.setText("GENDER: " + settings.getString("sex", "Male"));
		
		temp = postureSettings.getInt("standTime", 0);
		Log.e("test", String.valueOf(temp));
		if (temp ==  0){
			pieChart.updateData(1,1,1,1);
			paintGraph();
		
		}else{
			pieChart.updateData(postureSettings.getInt("standTime", 0),postureSettings.getInt("bendTime", 0)
		  			  ,postureSettings.getInt("sitTime", 0),postureSettings.getInt("lieTime", 0));
				paintGraph();
		}
		
		
	}
	
	public void setUpPostureListener(){
		 posturesListen = new OnSharedPreferenceChangeListener(){
		      public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		    	  pieChart.updateData(postureSettings.getInt("standTime", 0),postureSettings.getInt("bendTime", 0)
		    			  ,postureSettings.getInt("sitTime", 0),postureSettings.getInt("lieTime", 0));
		    	  paintGraph();
		    	  
		    	  
		      }
		};
		postureSettings.registerOnSharedPreferenceChangeListener(posturesListen);

	}
}
