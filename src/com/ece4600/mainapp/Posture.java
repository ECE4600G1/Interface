package com.ece4600.mainapp;


import java.util.Calendar;
import java.util.GregorianCalendar;

import org.achartengine.GraphicalView;

import com.ece4600.mainapp.Heartrate.ChartThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.widget.Toast;


public class Posture extends Activity {
	private Time now = new Time();
	public SharedPreferences postureSettings;
	public SharedPreferences.Editor editor;
	public OnSharedPreferenceChangeListener settingsListen;
	public String fileName;
	
	
	private BluetoothAdapter myBluetoothAdapter;

	// main code
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private TextView posture1,posture2,posture3, posture4,posture5, postureText;
	public TextView timenano,x_avg,threshold;
	public dataSample[] array_10 = new dataSample[10];
	int i = 0;
	public ImageView img;
	
	//public ImageView img = new ImageView(this);
	
	
	Bitmap posture_states[] = new Bitmap[4];
	
	private PosturePie pieChart = new PosturePie();
	private static Context context;
	
	//TODO delete this
	Button clear, timeLineButton;
	private ChartThread chartThread;
	
	@Override
	
	protected void onCreate(Bundle savedInstanceState) {
		
		//bluetooth stuff starts here
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_posture);
		
		
		context = getBaseContext();
		pieChart.initialize();
		pieChart.updateData(1,1,1,1);
		
		paintGraph();
		
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		bluetoothTest();
		setupMessageButton();
		//bluetooth stuff ends here;

		
		posture1 = (TextView) findViewById(R.id.posture1);
		posture2 = (TextView) findViewById(R.id.posture2);
		posture3 = (TextView) findViewById(R.id.posture3);
		posture4 = (TextView) findViewById(R.id.posture4);
		posture5 = (TextView) findViewById(R.id.posture5);
		

		
		postureText = (TextView) findViewById(R.id.postureText);
		
		img = (ImageView) findViewById(R.id.displayIMG); 
		
		Resources res = getResources();
		
		posture_states[0] = BitmapFactory.decodeResource(getResources(), R.drawable.standmpi);
		posture_states[1] = BitmapFactory.decodeResource(getResources(), R.drawable.sitmpi);
		posture_states[2] = BitmapFactory.decodeResource(getResources(), R.drawable.bendmpi);
		posture_states[3] = BitmapFactory.decodeResource(getResources(), R.drawable.laydownmpi);
		
		img.setImageBitmap(posture_states[0]);
		postureText.setText("Standing");
	
        IntentFilter intentFilter = new IntentFilter("POSTURE_EVENT");
        registerReceiver(broadcastRx, intentFilter);
        
        settingsListen = new OnSharedPreferenceChangeListener(){
		      public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		    	  pieChart.updateData(postureSettings.getInt("standTime", 0),postureSettings.getInt("bendTime", 0)
		    			  ,postureSettings.getInt("sitTime", 0),postureSettings.getInt("lieTime", 0));
		    	  paintGraph();
		    	  
		    	  // New posture
			       posture1.setText("1. " + postureSettings.getString("passPosture1", "1."));
			       posture2.setText("2. " + postureSettings.getString("passPosture2", "2."));
			       posture3.setText("3. " + postureSettings.getString("passPosture3", "3."));
			       posture4.setText("4. " + postureSettings.getString("passPosture4", "4."));
			       posture5.setText("5. " + postureSettings.getString("passPosture5", "5."));
		      }
		};
		
		setUpPreferences();
		postureSettings.registerOnSharedPreferenceChangeListener(settingsListen);
		restorePreferences();
		
		clear = (Button)findViewById(R.id.button1);
		timeLineButton = (Button)findViewById(R.id.postureTimeLine);
		timeLineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	    		
				startActivity(new Intent(Posture.this, PostureTimeLine.class));
				finish();
			}
		});	
		
		
		clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	    		
	    		editor.putInt("sitTime",0);
	    		editor.putInt("standTime",0);
	    		editor.putInt("bendTime",0);
	    		editor.putInt("lieTime",0);
	    		editor.commit();
			}
		});	
		
		//Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
		//h.postDelayed(updatePie, 1000);	
	}
	
	
	
	 @Override
	 protected void onDestroy() {
	  super.onDestroy();
	  //un-register BroadcastReceiver
	  unregisterReceiver(broadcastRx);
	  postureSettings.unregisterOnSharedPreferenceChangeListener(settingsListen);
	 }

	

	@Override
	
	protected void onResume() {
	super.onResume();
	
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("POSTURE_ACTION");
    registerReceiver(broadcastRx, intentFilter);
    
    postureSettings.registerOnSharedPreferenceChangeListener(settingsListen);
	}
	
	@Override
	protected void onPause() {
	super.onPause();
	postureSettings.unregisterOnSharedPreferenceChangeListener(settingsListen);
    LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);   
    bManager.unregisterReceiver(broadcastRx);
	}

	
	public void bluetoothTest(){
		int state = myBluetoothAdapter.getState();
		if (state == 10){
			AlertDialog.Builder alertDialogHint = new AlertDialog.Builder(this);
			alertDialogHint.setMessage("Bluetooth is OFF! Connection Fail!");
			alertDialogHint.setPositiveButton("Bluetooth Setting",
			new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(Posture.this,Bluetooth.class);
					startActivity(i);
					finish();
				}
			});
			alertDialogHint.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			AlertDialog alertDialog = alertDialogHint.create();
			alertDialog.show();
		}
	}

	private void setupMessageButton(){
    	Button messageButton = (Button)findViewById(R.id.returnpost);
    	messageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Posture.this, MainActivity.class));
				finish();
			}
		});	
    }
	
	public void onBackPressed() {
		// do something on back.return;		
		startActivity(new Intent(Posture.this, MainActivity.class));
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.posture, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		super.onOptionsItemSelected(item);
    	switch(item.getItemId()){
    	case R.id.postmenu_pedo:
    		startActivity(new Intent(this, Pedometer.class));
    		finish();
    		break;
    	case R.id.postmenu_loca:
    		startActivity(new Intent(this, Location.class));
    		finish();
    		break;
    	case R.id.postmenu_heart:
    		startActivity(new Intent(this, Heartrate.class));
    		finish();
    		break;
    	}
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	startActivity(new Intent(this, Bluetooth.class));
    		finish();
            return true;
        }
        return true; 
	}
	
	
// Broadcast reciever
// Recieves updates from postureService
	

	
	private BroadcastReceiver broadcastRx = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        
	        	String posture = intent.getStringExtra("POSTURE");
	        	float avgX1  = intent.getFloatExtra("avgX1", 0.0f);
	        	float avgY1  = intent.getFloatExtra("avgY1", 0.0f);
	        	float avgZ1  = intent.getFloatExtra("avgZ1", 0.0f);
	        	
	        	float avgX2  = intent.getFloatExtra("avgX2", 0.0f);
	        	float avgY2  = intent.getFloatExtra("avgY2", 0.0f);
	        	float avgZ2  = intent.getFloatExtra("avgZ2", 0.0f);
	        	
	        	
	        	
	        /*	if (avgX1 != 0.0f){
	    		axisX1.setText("X: "+avgX1);
	    		axisY1.setText("Y: "+avgY1);
	    		axisZ1.setText("Z: "+avgZ1);
	    		
	    		axisX2.setText("X: "+avgX2);
	    		axisY2.setText("Y: "+avgY2);
	    		axisZ2.setText("Z: "+avgZ2);
	        	}
	        	*/
	        	if (posture != null){
	        		
	        		if (posture.equals("STAND")){
	        			img.setImageBitmap(posture_states[0]);
	        			postureText.setText("Standing");
	        		}
	        		else if (posture.equals("SIT")){
	        			img.setImageBitmap(posture_states[1]);
	        			postureText.setText("Sitting");
	        		}
	        		else if (posture.equals("BEND")){
	        			img.setImageBitmap(posture_states[2]);
	        			postureText.setText("Bending");
	        		}
	        		else if (posture.equals("LIEFRONT")|| posture.equals("LIEBACK") || posture.equals("LIERIGHT") || posture.equals("LIELEFT")){
	        			img.setImageBitmap(posture_states[3]);
	        			if (posture.equals("LIEFRONT")){
	        				postureText.setText("Lying down (front-side)");
	        			}
	        			else if(posture.equals("LIEBACK")){
	        				postureText.setText("Lying down (back-side)");
	        			}
	        			else if(posture.equals("LIERIGHT")){
	        				postureText.setText("Lying down (right-side)");
	        			}
	        			else if(posture.equals("LIELEFT")){
	        				postureText.setText("Lying down (left-side)");
	        				
	        			}	        			
	        		}
	        		
	        		
	        	}
	        
	    }
	};
	public void paintGraph(){
		//Get Graph information:
		
		GraphicalView lineView = pieChart.getView(context);
		//Get reference to layout:
		LinearLayout layout =(LinearLayout)findViewById(R.id.pieChart);
		//clear the previous layout:
		layout.removeAllViews();
		//add new graph:
		if (layout != null)
				layout.addView(lineView);
	}
	
	

	
	public void setUpPreferences(){
    	postureSettings = getSharedPreferences("posturePrefs", MODE_MULTI_PROCESS );
    	editor = postureSettings.edit();
    	
    }
	
	private Runnable updatePie = new Runnable() {
		   @Override
		   public void run() {
			  //Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
		      //h.postDelayed(this, 1000);
				
		      double timeP1, timeP2, timeP3, timeP4;
		      	timeP1 = postureSettings.getInt("standTime", 0);
				timeP2 = postureSettings.getInt("bendTime", 0);
				timeP3 = postureSettings.getInt("sitTime", 0);
				timeP4 = postureSettings.getInt("lieTime", 0);
				
		    	  pieChart.updateData(timeP1,timeP2,timeP3,timeP4);
		    	  
		    	  paintGraph();
			  
			  
			  
			
			  
		   }};
		   
		   public void restorePreferences(){
			   String userName, fName, date;
			   int x = postureSettings.getInt("standTime", 0);
			   pieChart.updateData(postureSettings.getInt("standTime", 0),postureSettings.getInt("bendTime", 0)
		    			  ,postureSettings.getInt("sitTime", 0),postureSettings.getInt("lieTime", 0));
		       paintGraph();
		       
		       
		       posture1.setText(postureSettings.getString("passPosture1", "1."));
		       posture2.setText(postureSettings.getString("passPosture2", "2."));
		       posture3.setText(postureSettings.getString("passPosture3", "3."));
		       posture4.setText(postureSettings.getString("passPosture4", "4."));
		       posture5.setText(postureSettings.getString("passPosture5", "5."));
		       
		
		       
		   }
	
}

	
