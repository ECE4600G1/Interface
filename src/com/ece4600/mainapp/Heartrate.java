package com.ece4600.mainapp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import com.ece4600.mainapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class Heartrate extends Activity {
	public SharedPreferences settings;
	public SharedPreferences.Editor editor;
	
	private final static String TAG = "BLUETOOTH";
	private final String appName = "wellNode";
	private String readFilePath;
	
	private BluetoothAdapter myBluetoothAdapter;
	//private rfDuinoClass rfDuino;
	//private readFileClass readFile;
	private static Context context;
	
	Button startButton, stopButton, backButton;
	ToggleButton recordButton;
	
	private Handler handler = new Handler();
	
	
	//Line variables

	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	
	private GraphicalView view;
	
	private boolean start = false;
	private boolean firstTime = true;
	private int index, time;
	
	private int pointsToDisplay = 75;
	private int yMax = 15;
	private int yMin = 0;
	private int xScrollAhead = 35;
	
	private ECGLine line = new ECGLine();
	private final int chartDelay = 3; // millisecond delay for count
	public LinkedBlockingQueue<Float> queue = btMateService.bluetoothMateQueueForUI;

	
	private float samplingRate = 0.003f; 
	private float currentX;
	private ChartThread chartThread;
	
	
	
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CANADA);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heartrate);
		
		
		context = getBaseContext();
		
		
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		
		startButton = (Button)findViewById(R.id.startButton);
		stopButton = (Button)findViewById(R.id.stopButton);
		backButton = (Button)findViewById(R.id.returnheart);
		recordButton =(ToggleButton)findViewById(R.id.recordButton);
		
		
		line.initialize();
		currentX = 0.0f;
		initButtons();
		
		 ChartHandler chartUIHandler = new ChartHandler();
		 chartThread = new ChartThread(chartUIHandler);
		 chartThread.start();
		 
		 createAppFolder();
		 createECGFolder();

		 
		 paintGraph();
		 
		 setUpPreferences();
		 
		 recordButton.setChecked(settings.getBoolean("recordState", false));
		
	}
	
	 @Override
	 protected void onDestroy() {
	  super.onDestroy();
	  //un-register BroadcastReceiver
	 // unregisterReceiver(broadcastRx);
	  
	  Intent i = new Intent("BTMATE_EVENT");
	  i.putExtra("command", 'p');
	  sendBroadcast(i);
		
	  
	  //Intent intent2 = new Intent(Heartrate.this, btMateService.class);
	  //stopService(intent2);
	  
	 }
	 
	 
	 @Override
	 protected void onResume() {
		super.onResume();
	}
		
	@Override
	protected void onPause() {
		Intent i = new Intent("BTMATE_EVENT");
		i.putExtra("command", 'p');
		sendBroadcast(i);
		
		 //Intent intent2 = new Intent(Heartrate.this, btMateService.class);
		  //stopService(intent2);
		super.onPause();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.heartrate, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		switch(item.getItemId()){
		
		case R.id.heartmenu_loca:
    		
    		break;
    	case R.id.heartmenu_post:
    		
    		break;
    	case R.id.heartmenu_about:
    		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
    		startActivity(new Intent(this, About.class));
    		//finish();
    		break;
    	case R.id.action_settings:
    		
    		break;
    		
    	}
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return true; 
		
	}
	
	
	public void initButtons(){
		
		
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//startDAQ ();
				 Intent i = new Intent("BTMATE_EVENT");
				 i.putExtra("command", 's');
			     sendBroadcast(i);
			     chartThread.startPlot();
			     
			}     
	    });
		
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// stopDAQ();
			     Intent i = new Intent("BTMATE_EVENT");
				 i.putExtra("command", 'p'); // stop recieving data
			     sendBroadcast(i);
			     chartThread.cancel();
			}     
	    });
		
		
		recordButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(recordButton.isChecked()){  // Start recording
					  Intent i = new Intent("BTMATE_EVENT");
						i.putExtra("command", 'r');
						sendBroadcast(i);
				}
				else{ // stop recording

					 Intent i = new Intent("BTMATE_EVENT");
					 i.putExtra("command", 'n');
						sendBroadcast(i);
				}
				
			}
		});
		
		backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Heartrate.this, MainActivity.class));
				//finish();
				
			}
		});
		
		
	}
	
	
	
	public void stopDAQ (){
		start = false;
		line.stop();
		time =0;
		firstTime=true;
			
	}
	
	public void startDAQ (){
		if (firstTime){
		//line.initialize();
		// initialize accelerometers
	
		
		firstTime=false;
		time = 0;
		

		}
		index =0;
		start = true;
		
	}

	public void pauseDAQ(){
		start = false;
		line.stop();
		time =0;
		firstTime=true;
		
	}


	class ChartHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			
			double yVal = ((double)msg.arg1)/1000;
			
			//Log.e(TAG,String.valueOf(yVal));
			
        	if (firstTime){
        		time = 0;
        		index =0;
        		start = true;
        		firstTime = false;
        	}
			
        	line.addPoint(time, yVal);
        	time++;
        	
        	
        	//line.rePaint();
			//Get Graph information:
			GraphicalView lineView = line.getView(context);
			//Get reference to layout:
			LinearLayout layout =(LinearLayout)findViewById(R.id.chart);
			//clear the previous layout:
			layout.removeAllViews();
			//add new graph:
			layout.addView(lineView);
			}
	}
	
	class ChartThread extends Thread{
		public boolean continuePlot = true;
		private Handler handler;
		
		
		public ChartThread(Handler handler){
			GraphicalView lineView = line.getView(context);
			this.handler = handler;
		}
		
		@Override
		public void run(){
			
			while(continuePlot){
				
				double yVal = 0;
				
				try {
					Thread.sleep(chartDelay);
					if (queue.size() >= 1){
					yVal = (double) queue.poll();
					currentX = currentX + samplingRate;
					Message msg = Message.obtain();
					msg.arg1 = (int)Math.round(yVal*1000);
					handler.sendMessage(msg);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				
				/*if (yVal != 0.0f){		
				currentX = currentX + samplingRate;
				
				Message msg = Message.obtain();
				msg.arg1 = (int)Math.round(yVal*1000);
				handler.sendMessage(msg);	
				}*/
			}	
		}
		
		public void cancel(){
			continuePlot = false;
		}
		public void startPlot(){
			continuePlot = true;
		}
	}
	
	
  	private void createAppFolder(){
  		final String PATH = Environment.getExternalStorageDirectory() + "/" + appName + "/";
  		if(!(new File(PATH)).exists()) 
  		new File(PATH).mkdirs();
  	}
  
  	private void createECGFolder(){
  		final String PATH = Environment.getExternalStorageDirectory() + "/" + appName + "/ECG Recordings";
  		if(!(new File(PATH)).exists()) 
  		new File(PATH).mkdirs();
  	}
	
	
	public void paintGraph(){
		//Get Graph information:
		GraphicalView lineView = line.getView(context);
		//Get reference to layout:
		LinearLayout layout =(LinearLayout)findViewById(R.id.chart);
		//clear the previous layout:
		layout.removeAllViews();
		//add new graph:
		layout.addView(lineView);
	}
	
	public void setUpPreferences(){
    	settings = getSharedPreferences("ECGPrefs", MODE_PRIVATE);
    	editor = settings.edit();
    }
}