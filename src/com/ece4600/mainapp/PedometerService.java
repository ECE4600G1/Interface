package com.ece4600.mainapp;

import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class PedometerService extends Service{

	private float lastX = 0, lastY = 0, lastZ = 0;
	private float deltaX = 0;
	private float deltaY = 0;
	private float deltaZ = 0;
	private float test = 0;
	private float MaxX = 0, xoldvalue = 0, MaxY = 0, yoldvalue = 0, MaxZ = 0, zoldvalue = 0;
	private int stepnum = 0, i = 0, xp = 0, yp = 0, zp = 0, xn = 0, yn = 0, zn = 0, iteration = 500;
	private long LastStepDetection = 0;
	private float StepDetectionDelta = 0, speednum = 0;
	private double DifferenceDelta = 1.0;
	private double minPeak = 0.2;
	public static dataArrayFloat[] array_1d = new dataArrayFloat[1];
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		/* Called when service is first created
		 * one time setup. Not called if already running*/
		MaxX = 0;
		MaxY = 0;
		MaxZ = 0;		
		i = 0;
		stepnum = 0;
		speednum = 0;
		xoldvalue = 0;
		yoldvalue = 0;
		zoldvalue = 0;
		deltaX = 0;
		deltaY = 0;
		deltaZ = 0;
		
		//Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
		//h.postDelayed(resend, 1000);	
		
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId){
		/*Called by the system when an app component requests that a 
		 * Service start using startService()
		 * Once started, it can run in the background indefinitely*/
		
		float X = intent.getFloatExtra("X", 0.0f);
		float Y = intent.getFloatExtra("Y", 0.0f);
		float Z = intent.getFloatExtra("Z", 0.0f);
		
		dataArrayFloat data = new dataArrayFloat(X, Y, Z);
		array_1d[0] = data;
		
		Log.i("Pedometer", String.valueOf(X) + "," + String.valueOf(Y) + ","+String.valueOf(Z));
		
		stepDetection();
		
		return super.onStartCommand(intent,flags, startId);
	}
	
	public void onDestroy(){
		/*Called when a service no longer used and being destroyed*/
		super.onDestroy();
	}// End of onDestroy
	
//	private Runnable resend = new Runnable() {
//		   @Override
//		   public void run() {
//			 
//			  Intent i = new Intent("PEDOMETER_EVENT");
//			  i.putExtra("STEP", stepnum);
//			  i.putExtra("SPEED", speednum);
//			  sendBroadcast(i);
//			 
//			  Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
//			  h.postDelayed(this, 1000);
//			  
//		   }};
		   
	public void stepDetection() {
			float accX = array_1d[0].xaxis;
			float accY = array_1d[0].yaxis;
			float accZ = array_1d[0].zaxis;
		
			deltaX = accX - lastX;
			deltaY = accY - lastY;
			deltaZ = accZ - lastZ;
			
			long time = System.currentTimeMillis();
			long delta = time - LastStepDetection;
			long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(delta);
			test = Math.max(Math.abs(accX), Math.max(Math.abs(accY), Math.abs(accZ)));
			// if the change is below 1.5, it is just plain noise
			if ((deltaX < 0.05) && (deltaY < 0.05) && (deltaZ < 0.05)) {
				deltaX = 0;
				deltaY = 0;
				deltaZ = 0;
			} else if (iteration == 500) {
				iteration = 0;
				if ((test == Math.abs(accX)) && (test > 0.5)) {
					i = 1;
				} else if ((test == Math.abs(accY)) && (test > 0.5)) {
					i = 2;
				} else if ((test == Math.abs(accZ)) && (test > 0.5)) {
					i = 3;
				}
			}

			switch (i) {
			case 1:
				if (deltaX > 0) {
					xp++;
					MaxX = Math.max(MaxX, Math.max(Math.abs(accX), xoldvalue));
				} else if (xp > 2) {
					if (deltaX < 0) {
						xn++;
						if (xn > 1 && delta > StepDetectionDelta && MaxX - Math.abs(accX) > minPeak) {
							LastStepDetection = time;
							stepnum++;
							//timeSeconds += timeSeconds;
							iteration++;
							xp = 0;
							xn = 0;
							MaxX = 0;
						}
					}
				}
				xoldvalue = Math.abs(accX);
				Log.i("Pedometer", "Step detected Xaxis" + stepnum + "Delta time" + timeSeconds);
				break;
			case 2:
				if (deltaY > 0) {
					yp++;
					MaxY = Math.max(MaxY, Math.max(Math.abs(accY), yoldvalue));
				} else if (yp > 2) {
					if (deltaY < 0) {
						yn++;
						if (yn > 1 && delta > StepDetectionDelta && MaxY - Math.abs(accY) > minPeak) {
							LastStepDetection = time;
							stepnum++;
							//timeSeconds += timeSeconds;
							iteration++;
							yp = 0;
							yn = 0;
							MaxY = 0;
						}
					}
				}
				yoldvalue = Math.abs(accY);
				Log.i("Pedometer", "Step detected Yaxis " + stepnum + "Delta time" + timeSeconds);
				break;
			case 3:
				if (deltaZ > 0) {
					zp++;
					MaxZ = Math.max(MaxZ, Math.max(Math.abs(accZ), zoldvalue));
				} else if (zp > 2) {
					if (deltaZ < 0) {
						zn++;
						if (zn > 1 && delta > StepDetectionDelta && MaxZ - Math.abs(accZ) > minPeak) {
							LastStepDetection = time;
							stepnum++;
							//timeSeconds += timeSeconds;
							iteration++;
							zp = 0;
							zn = 0;
							MaxZ = 0;
						}
					}
				}
				zoldvalue = Math.abs(accZ);
				Log.i("Pedometer", "Step detected Zaxis " + stepnum + "Delta time" + timeSeconds);
				break;
			default:
				break;
			}
			// set the last know values of x,y,z
			lastX = accX;
			lastY = accY;
			lastZ = accZ;
			
			
			//Perhaps you dont need a handler
			Handler acc = new Handler(Looper.getMainLooper());
			acc.post(new Runnable(){
				@Override
				public void run(){
					Intent i = new Intent("PEDOMETER_EVENT");
					
					i.putExtra("CurrentX", lastX);
					i.putExtra("CurrentY", lastY);
					i.putExtra("CurrentZ", lastZ);
					
					i.putExtra("STEP", stepnum);
					sendBroadcast(i);
				}
			});
		}
	}
