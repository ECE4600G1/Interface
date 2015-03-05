package com.ece4600.mainapp;

import java.util.concurrent.TimeUnit;
import com.badlogic.gdx.audio.analysis.FFT;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Pedometer extends Activity{
	
	
	
	private TextView step,speed, target, bar;
	Button reset, returnbutton, start, stop, settings;
	private int stepnum = 0, stepdetect = 0, stepthres = 0, targetnum = 0, barnum = 0;
	private double speednum = 0, sizenum = 0, stepprenum = 0, steppretotal = 0, steppreav = 0;
	private long timedetect = 0, timeSecondsstart = 0, timestart = 0, timeSecondsstop = 0, timestop = 0;
	private boolean startflag = false;
	private BluetoothAdapter myBluetoothAdapter;
	public SharedPreferences settingst;
	public SharedPreferences.Editor editort;
	private ProgressBar progressBar;
	
	private double peak = 0, fftpeak = 0, freq = 0;
	private static int fs = 50;
	private int N = 1024;
	private int index = 0, freqindex = 0, j = 0;
	private float[] arrayX = new float[N];
	private float[] arrayY = new float[N];
	private float[] arrayZ = new float[N];
	private double[] new_sig;
	private boolean fftflag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pedometer);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initializeViews();
		reset = (Button) findViewById(R.id.pedo_reset);
		returnbutton = (Button) findViewById(R.id.returnpedo);
		start = (Button) findViewById(R.id.pedo_start);
		stop = (Button) findViewById(R.id.pedo_stop);
		settings = (Button)findViewById(R.id.pedoSettings);
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  	  	progressBar = (ProgressBar)findViewById(R.id.progressBar1);
	  	progressBar.setProgress(0);
	    bar = (TextView) findViewById(R.id.pedo_bar); // The text inside the circular progressbar

		bluetoothTest();
		
		IntentFilter intentFilter = new IntentFilter("PEDOMETER_EVENT");
        registerReceiver(broadcastRx, intentFilter);

        setUpPreferences();
  	  	restorePreferences();
  	  	


	}

	public void initializeViews() {
		step = (TextView) findViewById(R.id.pedo_stepnum);
		speed = (TextView) findViewById(R.id.pedo_speednum);
		target = (TextView) findViewById(R.id.pedo_targetsnum);
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
					Intent i = new Intent(Pedometer.this,Bluetooth.class);
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

	 @Override
	 protected void onDestroy() {
	  super.onDestroy();
	  //un-register BroadcastReceiver
	  unregisterReceiver(broadcastRx);
	 }

	

	@Override
	
	protected void onResume() {
	super.onResume();
	
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("PEDOMETER_ACTION");
    registerReceiver(broadcastRx, intentFilter);
	}
	
	@Override
	protected void onPause() {
	super.onPause();

    LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);   
    bManager.unregisterReceiver(broadcastRx);
	}

	

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pedo_start:
			onResume();
			//timestart = System.currentTimeMillis();
			//timeSecondsstart = TimeUnit.MILLISECONDS.toSeconds(timestart);
			//countdowndisplay();
			stepthres = stepnum;
			//stepthres = 0;
			speednum = 0;
			startflag = true;
			final Toast toast = Toast.makeText(getApplicationContext(), "Step Detection Start", Toast.LENGTH_SHORT);
		    toast.show();
		    Handler handler = new Handler();
		        handler.postDelayed(new Runnable() {
		           @Override
		           public void run() {
		               toast.cancel(); 
		           }
		    }, 500);
			break;
		case R.id.pedo_stop:
			onPause();
			startflag = false;
			final Toast toaststop = Toast.makeText(getApplicationContext(), "Step Detection Stop", Toast.LENGTH_SHORT);
		    toaststop.show();
		    Handler handlerstop = new Handler();
		        handlerstop.postDelayed(new Runnable() {
		           @Override
		           public void run() {
		               toaststop.cancel(); 
		           }
		    }, 500);	    
			break;
		case R.id.returnpedo:
			startActivity(new Intent(Pedometer.this, MainActivity.class));
			finish();
			break;
		case R.id.pedo_reset:
			step.setText("0.0 steps");
			speed.setText("0.0 steps/min");
			startflag = false;	
			fftflag = false;
			freq = 0;
			steppretotal = 0;
			steppreav = 0;
		    bar.setText("0%");
		  	progressBar.setProgress(0);
			break;
		case R.id.pedoSettings:
			startActivity(new Intent(Pedometer.this, TargetSetting.class));
			finish();
			break;
		default:
			break;
		}
	}

//	private void countdowndisplay() {
//		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//		alertDialog.setMessage("00:05");
//		alertDialog.show();
//
//		new CountDownTimer(5000, 1000) {
//			public void onTick(long millisUntilFinished) {
//				alertDialog.setMessage("00:" + (millisUntilFinished / 1000));
//			}
//
//			@Override
//			public void onFinish() {
//				alertDialog.setMessage("Completed");
//				alertDialog.dismiss();
//			}
//		}.start();
//	}

	public void onBackPressed() {
		// do something on back.return;
		startActivity(new Intent(Pedometer.this, MainActivity.class));
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pedometer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.action_settings:
			PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
    		startActivity(new Intent(this, Bluetooth.class));
    		finish();
    		break;
		case R.id.pedomenu_heart:
			startActivity(new Intent(this, Heartrate.class));
			finish();
			break;
		case R.id.pedomenu_loca:
			startActivity(new Intent(this, Location.class));
			finish();
			break;
		case R.id.pedomenu_post:
			startActivity(new Intent(this, Posture.class));
			finish();
			break;
		}
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return true;
	}

	// Broadcast receiver
	// Receives updates from postureService
		
	public void setUpPreferences(){
    	settingst = getSharedPreferences("pedoPrefs", MODE_PRIVATE);
    	editort = settingst.edit();
    }
	
	public void restorePreferences(){
		target.setText(settingst.getString("target", "0000"));
		targetnum = Integer.parseInt(settingst.getString("target", "0000"));
		try
		{
			sizenum = Double.parseDouble(settingst.getString("size", "1.0f"));
		}
		catch(NumberFormatException e)
		{
		  //not a double
		}
		
		
	}
		
		private BroadcastReceiver broadcastRx = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        
		        	stepnum = intent.getIntExtra("STEP", stepnum);
		        	stepdetect = stepnum - stepthres;
		        	
		        	editort.putInt("stepsTaken", stepdetect);
		        	editort.commit();
		        	
//		        	timestop = System.currentTimeMillis();
//					timeSecondsstop = TimeUnit.MILLISECONDS.toSeconds(timestop);
//					timedetect = timeSecondsstop - timeSecondsstart;
//					if (timedetect != 0){
//						speednum = stepdetect*60/ timedetect;
//					}
//		        	
		        	float CurrentX  = intent.getFloatExtra("CurrentX", 0.0f);
		        	float CurrentY  = intent.getFloatExtra("CurrentY", 0.0f);
		        	float CurrentZ  = intent.getFloatExtra("CurrentZ", 0.0f);
		        	
		        	if (fftflag == true){
			        	freq = (double)freqindex*(double)fs/(double)N;
			        	steppreav = freq*sizenum*2.237;
			        	//stepprenum = freq*N/fs;
			        	//steppretotal = steppretotal + stepprenum;
			        	//steppreav = (steppretotal+stepdetect)/2;
			        	fftflag = false;
						Log.i("FFFFF", "Stepone"+ stepprenum + "steptotal"+ steppretotal + "Freq" + freq + "Ave" + steppreav);
		        	}
		        	
		        	if (startflag == true){
		        	step.setText(Integer.toString(stepdetect) + "steps");
		        		if (steppreav != 0){
		        			speed.setText(Double.toString(steppreav) + " Miles/hour");
		        		}
					barnum = stepdetect*100/targetnum;
				    Log.i("Bar", "Bar"+ barnum + "Step"+ stepdetect);
				    bar.setText(Integer.toString(barnum) + "%");
				  	progressBar.setProgress(barnum);
		        	if (j < N) {
		    			arrayX[j] = CurrentX;
		    			arrayY[j] = CurrentY;
		    			arrayZ[j] = CurrentZ;
		    			j++;
		    			Log.i("FFT", String.valueOf(CurrentX) + "," + String.valueOf(CurrentY) + ","+String.valueOf(CurrentZ));
		    		}else{
		    			new_sig = fft(N, fs, arrayX, arrayY, arrayZ);
		    			j = 0;
		    		}
		        	
		        	}
		        	
		       }
		        
		};
		
		private double[] fft(int N, int fs, float[] arrayX, float[] arrayY, float[] arrayZ) {
			float[] fft_imx, fft_imy, fft_imz, fft_rex, fft_rey, fft_rez;
			// float[] mod_spec =new float[array.length/2];
			double[] fft = new double[N];
			double fft_x, fft_y, fft_z;
			
//			// Zero Pad signal
//			for (int i = 0; i < N; i++) {
	//
//				if (i < array.length) {
//					new_arrayX[i] = array[i];
//					new_arrayY[i] = array[i];
//					new_arrayZ[i] = array[i];
//				} else {
//					new_arrayX[i] = 0;
//					new_arrayY[i] = 0;
//					new_arrayZ[i] = 0;
//				}
//			}

			FFT fftx = new FFT(N, fs);
			FFT ffty = new FFT(N, fs);
			FFT fftz = new FFT(N, fs);
			fftx.forward(arrayX);
			ffty.forward(arrayY);
			fftz.forward(arrayZ);
			fft_imx = fftx.getImaginaryPart();
			fft_rex = fftx.getRealPart();
			fft_imy = ffty.getImaginaryPart();
			fft_rey = ffty.getRealPart();
			fft_imz = fftz.getImaginaryPart();
			fft_rez = fftz.getRealPart();
			for (int k = 0; k < N/2; k++) {
				fft_x = Math.sqrt(Math.pow(fft_imx[k],2) + (Math.pow(fft_rex[k],2)));
				fft_y = Math.sqrt(Math.pow(fft_imy[k],2) + (Math.pow(fft_rey[k],2)));
				fft_z = Math.sqrt(Math.pow(fft_imz[k],2) + (Math.pow(fft_rez[k],2)));
				double fftt = ((Math.pow(fft_x, 2) + Math.pow(fft_y, 2) + Math.pow(fft_z, 2)))/(fs*N);
				//double fftt = -Math.log10((1/(fs*N)) * (Math.pow(fft_x, 2) + Math.pow(fft_y, 2) + Math.pow(fft_z, 2)));
				Log.i("fftv", "Value " + fftt + "X" + fft_x + "Y" + fft_y + "Z" + fft_z);
				if (fftt > peak && k > 0 && fftt > 0.1) {
					peak = fftt;
					index = k;
					Log.i("fftindex", "K " + k);
				}
			}
			freqindex = index;
			fftpeak = peak;
			peak = 0;
			index = 0;
			fftflag = true;
			Log.i("fft", "Frequency " + freqindex + "Peak" + fftpeak);
//			tmpi = fft.getImaginaryPart();
//			tmpr = fft.getRealPart();
			
			return fft;

		}
		
		
	

}