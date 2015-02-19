package com.ece4600.mainapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Bluetooth extends Activity{
	private BluetoothAdapter myBluetoothAdapter;
	private ListView listpaired;
	Button blueon, blueoff, bluecancel, blueposture, bluepedometer, bluemain,blueECG;
	Switch postureState, pedometerState, ECGState;
    
    //For toast messages:
    Context context;
    private Handler handler = new Handler();
    
    //TI SensorTag device info
    private final String device1_MAC = "90:59:AF:0B:82:F4";
    private final String device2_MAC = "90:59:AF:0B:82:D9";
    private final String device3_MAC = "BC:6A:29:AB:61:CF";
    
	public SharedPreferences settings;
	public OnSharedPreferenceChangeListener settingsListen;
	public SharedPreferences.Editor editor;
	private boolean blueState;
    
    //--------------------------------------------------------------------
    // ON CREATE function
    //--------------------------------------------------------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		blueon = (Button)findViewById(R.id.blueon);
		blueoff = (Button)findViewById(R.id.blueoff);
		bluecancel = (Button)findViewById(R.id.bluecancel);
		blueposture =(Button)findViewById(R.id.blueposture);
		bluepedometer =(Button)findViewById(R.id.bluepedometer);
		bluemain =(Button)findViewById(R.id.bluemain);
		
		blueECG = (Button)findViewById(R.id.blueECG);
		
		pedometerState = (Switch)findViewById(R.id.bluePedometerState);
		postureState = (Switch)findViewById(R.id.bluePostureState);
		ECGState = (Switch)findViewById(R.id.blueECGState);
		
		setUpPreferences();
		restorePreferences();
		initButtons();
		
		context = this;
		
		settingsListen = new OnSharedPreferenceChangeListener(){
		      public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		    	  ECGState.setChecked(settings.getBoolean("blueECG", false));
		    	  postureState.setChecked(settings.getBoolean("bluePosture", false));
		    	  pedometerState.setChecked(settings.getBoolean("bluePedo", false));
		      }
		};
		
		settings.registerOnSharedPreferenceChangeListener(settingsListen);
		
	}
	
	 protected void onDestroy() {
	        super.onDestroy();
	        settings.unregisterOnSharedPreferenceChangeListener(settingsListen);
	    }



	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		listpaired.setAdapter(adapter);
		switch (v.getId()) {
		case R.id.blueon:
			startActivity(new Intent(Bluetooth.this, MainActivity.class));
			//finish();
			break;
		case R.id.blueoff:
			myBluetoothAdapter.disable();
			break;
		case R.id.bluecancel:
			//poll();
			startActivity(new Intent(Bluetooth.this, MainActivity.class));
			finish();
		default:
			break;
		}		     
	}

	
	
	public void initButtons(){
		bluecancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Bluetooth.this, MainActivity.class));
			}
		});
		
	    
		blueposture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 if(myBluetoothAdapter!=null){
					 
					 
		        Intent Postureintent = new Intent(Bluetooth.this, bleService.class);
		        
		        if (postureState.isChecked()){
					postureState.setChecked(false);
					
					editor.putBoolean("bluePosture", false);
					editor.commit();
					
					stopService(Postureintent);
				} else{
					if(ECGState.isChecked()){
						
						ECGState.setChecked(false);
						
						editor.putBoolean("blueECG", false);
						editor.commit();
						Intent ECGintent = new Intent(Bluetooth.this, btMateService.class);
						stopService(ECGintent);
					}
					
					postureState.setChecked(true);
					editor.putBoolean("bluePosture", true);
					editor.commit();
					
					startService(Postureintent);
				}
		        
				 }
			}     
	    });
		
		bluepedometer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 if(myBluetoothAdapter!=null){
					 
		        Intent pedointent = new Intent(Bluetooth.this, bleService_pedo.class);
		        if (pedometerState.isChecked()){
					pedometerState.setChecked(false);
					
					editor.putBoolean("bluePedo", false);
					editor.commit();
					
					stopService(pedointent);
				} else{
					pedometerState.setChecked(true);
					editor.putBoolean("bluePedo", true);
					editor.commit();
					startService(pedointent);
				}
		        
		        startService(pedointent);
				 }
			}     
	    });
		
		blueon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 if(myBluetoothAdapter==null){
					 Toast.makeText(getApplicationContext(), "Bluetooth service not available in the device", Toast.LENGTH_SHORT).show();
	             }
				 else{
					 if(!myBluetoothAdapter.isEnabled()){
							Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
							startActivityForResult(turnOn, 0);
							Toast.makeText(getApplicationContext(), "Bluetooth turned ON", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getApplicationContext(), "Bluetooth is already ON", Toast.LENGTH_SHORT).show();
						}
					 }	
			}     
	    });
		
		blueoff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myBluetoothAdapter.disable();
				Toast.makeText(getApplicationContext(), "Bluetooth Turned OFF", Toast.LENGTH_SHORT).show();
			}
		});
		
		bluemain.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Bluetooth.this, MainActivity.class));
			}
		});
		
		blueECG.setOnClickListener(new View.OnClickListener() {
			
			Intent ECGintent = new Intent(Bluetooth.this, btMateService.class);
	        

			@Override
			public void onClick(View v) {
				if (ECGState.isChecked()){
					ECGState.setChecked(false);
					
					editor.putBoolean("blueECG", false);
					editor.commit();
					
					stopService(ECGintent);
				} else{
					
					if(pedometerState.isChecked()){
						pedometerState.setChecked(false);
						editor.putBoolean("bluePedo", false);
						editor.commit();
				        Intent pedointent = new Intent(Bluetooth.this, bleService_pedo.class);
						stopService(pedointent);
						pedometerState.setChecked(false);
						
					}
					
					if (postureState.isChecked()){
						postureState.setChecked(false);
						
						editor.putBoolean("bluePosture", false);
						editor.commit();
					    Intent Postureintent = new Intent(Bluetooth.this, bleService.class);
						stopService(Postureintent);
					}
					
					ECGState.setChecked(true);
					editor.putBoolean("blueECG", true);
					editor.commit();
					startService(ECGintent);
				}
				
			}
			
		});
		
	}
	
	
	
	public void setUpPreferences(){
    	settings = getSharedPreferences("bluetoothPrefs", MODE_PRIVATE);
    	editor = settings.edit();
    }
	
	public void restorePreferences(){
		ECGState.setChecked(settings.getBoolean("blueECG", false));
		postureState.setChecked(settings.getBoolean("bluePosture", false));
		pedometerState.setChecked(settings.getBoolean("bluePedo", false));
		
	}
	
	}

 