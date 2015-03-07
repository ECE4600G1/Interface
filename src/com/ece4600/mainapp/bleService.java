package com.ece4600.mainapp;

import static java.util.UUID.fromString;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import com.ece4600.mainapp.btMateService.connectState;
import com.ece4600.mainapp.btMateService.deviceState;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public class bleService extends Service{
	 public SharedPreferences settings;
	 public SharedPreferences.Editor editor;
	 
	 
    private final static String TAG = bleService.class.getSimpleName();
    private final static String DEBUG = "DEBUG";
	private BluetoothManager mBluetoothManager;
	
	protected BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mConnectedGatt1, mConnectedGatt2;
	
	private enum mSensorState {CONNECTED, DISCONNECTED};
	private mSensorState mSensor1, mSensor2;
	private BluetoothAdapter myBluetoothAdapter;
	
	private static final long SCAN_PERIOD = 2500;  // Used to scan for devices for only 10 secs
    
    Context context;
    private Handler handler = new Handler();
    
    //TI SensorTag device info
    /*
     * device1_MAC = "90:59:AF:0B:82:F4"
     * device2_MAC = "90:59:AF:0B:82:D9"
     * device3_MAC = "BC:6A:29:AB:61:CF"
     * 
     * */
    private final String device1_MAC = "90:59:AF:0B:82:F4";
    private final String device2_MAC = "90:59:AF:0B:82:D9";
    
    
    public dataArray[] array_2d = new dataArray[2];
    
    private String fileNameD1, fileNameD2, filePath, fileName;
    private int count;
    
    //--------------------------------------------------------------------
    // TI SensorTag UUIDs
    //--------------------------------------------------------------------
	public final static UUID 
	    UUID_IRT_SERV = fromString("f000aa00-0451-4000-b000-000000000000"),
	    UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000"),
	    UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"), // 0: disable, 1: enable

	    UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
	    UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
	    UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	    UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	    UUID_HUM_SERV = fromString("f000aa20-0451-4000-b000-000000000000"),
	    UUID_HUM_DATA = fromString("f000aa21-0451-4000-b000-000000000000"),
	    UUID_HUM_CONF = fromString("f000aa22-0451-4000-b000-000000000000"), // 0: disable, 1: enable

	    UUID_MAG_SERV = fromString("f000aa30-0451-4000-b000-000000000000"),
	    UUID_MAG_DATA = fromString("f000aa31-0451-4000-b000-000000000000"),
	    UUID_MAG_CONF = fromString("f000aa32-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	    UUID_MAG_PERI = fromString("f000aa33-0451-4000-b000-000000000000"), // Period in tens of milliseconds

	    UUID_BAR_SERV = fromString("f000aa40-0451-4000-b000-000000000000"), 
	    UUID_BAR_DATA = fromString("f000aa41-0451-4000-b000-000000000000"),
	    UUID_BAR_CONF = fromString("f000aa42-0451-4000-b000-000000000000"), // 0: disable, 1: enable
	    UUID_BAR_CALI = fromString("f000aa43-0451-4000-b000-000000000000"), // Calibration characteristic

	    UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"), 
	    UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
	    UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z

	    UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"), 
	    UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb"),
	    UUID_CCC_DESC = fromString("00002902-0000-1000-8000-00805f9b34fb");
    
	final static String MY_ACTION = "MY_ACTION"; // what does this do?
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    //TODO do something useful
		initialize();
		
	    createAppFolder();
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		filePath = Environment.getExternalStorageDirectory() + "/G01Capstone/";
		
    	Time now = new Time();
		now.setToNow();
		fileName = "DEVICES " + now.format("%Y-%m-%d %H-%M-%S") + ".txt";
		count = 0;
		
		
		mSensor1 = mSensorState.DISCONNECTED; //  set the sensor indicator value to disconnected
		mSensor2 = mSensorState.DISCONNECTED;
		MyThread myThread = new MyThread(); // creating a new thread?
		myThread.start();
		
		setUpPreferences();
		
		IntentFilter intentFilter = new IntentFilter("BLE_EVENT");
	    registerReceiver(mBLEReceiver, intentFilter);
	    
		int state = myBluetoothAdapter.getState();
		if (state == 10){
			final Toast toast = Toast.makeText(bleService.this,"Bluetooth is OFF!", Toast.LENGTH_SHORT);
		    toast.show();
		    Handler handlerstop = new Handler();
		        handlerstop.postDelayed(new Runnable() {
		           @Override
		           public void run() {
		               toast.cancel(); 
		               turnOffSelf();
		           }
		    }, 500);
		}
		
		//handler.postDelayed(test, 100);
		
		 
	    return  super.onStartCommand(intent, flags, startId);
	  }
	
	
	
//	private Runnable test = new Runnable() {
//		   @Override
//		   public void run() {
//
//			  
//			  Intent i = new Intent(bleService.this, PostureService.class);
//				 i.putExtra("XVal1", 1.0f);
//				 i.putExtra("YVal1", 2.0f);
//				 i.putExtra("ZVal1", 3.0f);
//				 startService(i);
//				 handler.postDelayed(this, 100);
//
//		   }
//		};
	
	
	@Override
	public void onCreate(){
		
	}
	@Override
	public void onDestroy(){ // disconnects the sensortag connection after quitting service

		 Intent i = new Intent(bleService.this, PostureService.class);
		 i.putExtra("STOP", true);
		  i.putExtra("XVal1", (float) array_2d[0].xaxis);
		  i.putExtra("YVal1",(float) array_2d[0].yaxis);
		  i.putExtra("ZVal1", (float) array_2d[0].zaxis);
		  
		  i.putExtra("XVal2", (float) array_2d[1].xaxis);
		  i.putExtra("YVal2",(float) array_2d[1].yaxis);
		  i.putExtra("ZVal2", (float) array_2d[1].zaxis);
		 startService(i);
		  
		 
	if (mConnectedGatt1 != null)	
		mConnectedGatt1.disconnect();
	if (mConnectedGatt2 != null)	
		mConnectedGatt2.disconnect();
	if (mBluetoothAdapter != null)	
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	
	 unregisterReceiver(mBLEReceiver);

	}
	 
	/**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) { // not sure how the logic works here
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        
        if(mBluetoothAdapter == null) mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;  
    }

final class MyThread extends Thread{
 		 
 		 @Override
 		 public void run() {

 		dataArray data = new dataArray(0.0f, 0.0f, 0.0f);
 		array_2d[0] = data;
 		array_2d[1] = data;
 		startScan();

	
	
 		 }	
}
    

//--------------------------	
//	Start Scanning for devices
//--------------------------
 public void startScan(){
	 if (mSensor1 == mSensorState.DISCONNECTED)
	 mBluetoothAdapter.startLeScan(mLeScanCallback);
	 else 
	 mBluetoothAdapter.startLeScan(mLeScanCallback2);
	 
	 Log.i(DEBUG, "start scan");
	 Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
	 h.postDelayed(mStopScanRunnable, SCAN_PERIOD);
 }
 
private BluetoothAdapter.LeScanCallback mLeScanCallback = 
	new BluetoothAdapter.LeScanCallback(){
	@Override
	public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable(){
			@Override
			public void run(){
				Log.i("BLE", "New LE Device: " + device.getName() + " @ " + rssi);
				if (device.getAddress().equals(device1_MAC) && (mSensor1 == mSensorState.DISCONNECTED)){
					mConnectedGatt1 = device.connectGatt(context, false, mGattCallback1);
					 stopScan();
				}
			}
		});
		}
	};

	
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback2 = 
			new BluetoothAdapter.LeScanCallback(){
			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
				Handler h = new Handler(Looper.getMainLooper());
				h.post(new Runnable(){
					@Override
					public void run(){
						Log.i("BLE", "New LE Device: " + device.getName() + " @ " + rssi);
						if (device.getAddress().equals(device2_MAC) && (mSensor2 == mSensorState.DISCONNECTED)){
							mConnectedGatt2 = device.connectGatt(context, false, mGattCallback2);
							 stopScan();
						}
					}
				});
				}
			};	
//--------------------------	
//	Stop scanning for devices
//--------------------------

public void stopScan(){
	Log.i(DEBUG, "Stop scan");
	mBluetoothAdapter.stopLeScan(mLeScanCallback);
	
}

private Runnable mStopScanRunnable = new Runnable() {
    @Override
    public void run() {
        stopScan();
     if (mSensor1 == mSensorState.DISCONNECTED)   
        turnOffSelf();
    }
};
 	
//--------------------------	
// GATT callback 1
//--------------------------
private BluetoothGattCallback mGattCallback1 = new BluetoothGattCallback() {
	
	/* What occurs once the device is connected:*/
	@Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            /*
             * Once successfully connected, we must next discover all the services on the
             * device before we can read and write their characteristics.
             */
        	Handler h = new Handler(Looper.getMainLooper());
    		h.post(new Runnable(){
    			@Override
    			public void run(){
    				Log.i(DEBUG, "Conntection successful, Getting Services");
    				//Toast.makeText( bleService.this, "Device 1 connected", Toast.LENGTH_SHORT).show();
    				final Toast toast = Toast.makeText(bleService.this,"Device 1 connected", Toast.LENGTH_SHORT);
				    toast.show();
				    Handler handlerstop = new Handler();
				        handlerstop.postDelayed(new Runnable() {
				           @Override
				           public void run() {
				               toast.cancel(); 
				           }
				    }, 500);
    			}
    		});
    		mSensor1 = mSensorState.CONNECTED;
            gatt.discoverServices();
            
           
        } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
         
        	mSensor1 = mSensorState.DISCONNECTED;
        	mSensor2 = mSensorState.DISCONNECTED;
        	turnOffSelf();
            
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            /*
             * If there is a failure at any stage, simply disconnect
             */
            gatt.disconnect();
        }
    }
	
	@Override
	/* New services connected*/
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
        	Log.i(DEBUG, "onServicesDiscovered received: " + status);
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
        
        enableSensor(gatt);
    }
	
	public void enableSensor(BluetoothGatt gatt){
		BluetoothGattCharacteristic c;
		Log.i(DEBUG, "Enabling accelerometers");
		c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_CONF);
		c.setValue(new byte[] {0x01});
		
		gatt.writeCharacteristic(c);
	}
	
	/*Writing to a characteristic*/
	@Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        //After writing the enable flag, next we read the initial value
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable(){
			@Override
			public void run(){
				
				Toast.makeText( bleService.this, "Device 1 accelerometers enabled", Toast.LENGTH_SHORT).show();
				final Toast toast = Toast.makeText(bleService.this,"Device 1 accelerometers enabled", Toast.LENGTH_SHORT);
			    toast.show();
			    Handler handlerstop = new Handler();
			        handlerstop.postDelayed(new Runnable() {
			           @Override
			           public void run() {
			               toast.cancel(); 
			           }
			    }, 500);
			}
		});
		
		startScan();

    }
	/* Read a sensor:*/
	public void readSensor(BluetoothGatt gatt){
		BluetoothGattCharacteristic c;
		c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
		gatt.readCharacteristic(c);
		
	}
	
	/*On characteristic read:*/
	@Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic c, int status) {
        byte[] rawValue = c.getValue();
        String strValue = null;
        int intValue = 0;
        
        float[] vector;
        vector = new float[3];
        vector = sensorTag.getAccelerometerValue(c);
		dataArray data = new dataArray(vector[0], vector[1], vector[2]);
		array_2d[0] = data;
		Log.e(TAG, "recieved data 1");
	}
}; //End of mGattCallback1



//--------------------------	
//GATT callback 2
//--------------------------
private BluetoothGattCallback mGattCallback2 = new BluetoothGattCallback() {
	
	/* What occurs once the device is connected:*/
	@Override
 public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
     if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
         /*
          * Once successfully connected, we must next discover all the services on the
          * device before we can read and write their characteristics.
          */
     	Handler h = new Handler(Looper.getMainLooper());
 		h.post(new Runnable(){
 			@Override
 			public void run(){
 				Log.i(DEBUG, "Conntection successful, Getting Services");
 				//Toast.makeText( bleService.this, "Device 2 connected", Toast.LENGTH_SHORT).show();
 				final Toast toast = Toast.makeText(bleService.this,"Device 2 connected", Toast.LENGTH_SHORT);
			    toast.show();
			    Handler handlerstop = new Handler();
			        handlerstop.postDelayed(new Runnable() {
			           @Override
			           public void run() {
			               toast.cancel(); 
			           }
			    }, 500);
 			}
 		});
 		mSensor2 = mSensorState.CONNECTED;
         gatt.discoverServices();
        
     } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
    	 mSensor2 = mSensorState.DISCONNECTED;
    	 mSensor1 = mSensorState.DISCONNECTED;
     	 turnOffSelf();
     } else if (status != BluetoothGatt.GATT_SUCCESS) {
         /*
          * If there is a failure at any stage, simply disconnect
          */
         gatt.disconnect();
     }
 }
	
	@Override
	/* New services connected*/
 public void onServicesDiscovered(BluetoothGatt gatt, int status) {
     if (status == BluetoothGatt.GATT_SUCCESS) {
     	Log.i(DEBUG, "onServicesDiscovered received: " + status);
     } else {
         Log.w(TAG, "onServicesDiscovered received: " + status);
     }
     
     enableSensor(gatt);
 }
	
	public void enableSensor(BluetoothGatt gatt){
		BluetoothGattCharacteristic c;
		Log.i(DEBUG, "Enabling accelerometers");
		c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_CONF);
		c.setValue(new byte[] {0x01});
		
		gatt.writeCharacteristic(c);
	}
	
	/*Writing to a characteristic*/
	@Override
 public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
     //After writing the enable flag, next we read the initial value
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable(){
			@Override
			public void run(){
				//Log.i(DEBUG, "Connection successful, Getting Services");
				//Toast.makeText( bleService.this, "Device 2 accelerometers enabled", Toast.LENGTH_SHORT).show();
				final Toast toast = Toast.makeText(bleService.this,"Device 2 accelerometers enabled", Toast.LENGTH_SHORT);
			    toast.show();
			    Handler handlerstop = new Handler();
			        handlerstop.postDelayed(new Runnable() {
			           @Override
			           public void run() {
			               toast.cancel(); 
			           }
			    }, 500);
				Intent dialogIntent = new Intent(bleService.this, MainActivity.class);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				bleService.this.startActivity(dialogIntent);
			}
		});
		
		//readSensor(gatt);
		poll();
 }
	/* Read a sensor:*/
	public void readSensor(BluetoothGatt gatt){
		BluetoothGattCharacteristic c;
		c = gatt.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
		gatt.readCharacteristic(c);
		
	}
	
	/*On characteristic read:*/
	@Override
 public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic c, int status) {
     byte[] rawValue = c.getValue();
     String strValue = null;
     int intValue = 0;
     
     float[] vector;
     vector = new float[3];
     vector = sensorTag.getAccelerometerValue(c);
     
	 dataArray data = new dataArray(vector[0], vector[1], vector[2]);
	 array_2d[1] = data;
	 Log.e(TAG, "recieved data 2");
	}
}; //End of mGattCallback2




//POLLING
private void poll(){
	handler.postDelayed(runnable, 100);	
}


private void poll1(){
	handler.postDelayed(runnable1, 100);	
}



private Runnable runnable1 = new Runnable() {
	   @Override
	   public void run() {
		  
		  readDevice1();
		  
		  
		  handler.postDelayed(this, 100);
		  if (mSensor1 == mSensorState.CONNECTED){
			  //String data1 = "D1," + array_2d[0].xaxis +  "," + array_2d[0].yaxis +  "," + array_2d[0].zaxis;
			  //Log.i(DEBUG, data1);
       
			  Intent i = new Intent(bleService.this, PostureService.class);
			  i.putExtra("STOP", false);
			  i.putExtra("XVal1", (float) array_2d[0].xaxis);
			  i.putExtra("YVal1",(float) array_2d[0].yaxis);
			  i.putExtra("ZVal1", (float) array_2d[0].zaxis);
			  
			  i.putExtra("XVal2", (float) array_2d[1].xaxis);
			  i.putExtra("YVal2",(float) array_2d[1].yaxis);
			  i.putExtra("ZVal2", (float) array_2d[1].zaxis);
			  
			  startService(i);
		  }
	   else{
		   handler.removeCallbacks(this);
		   stopScan();
		   stopSelf();
	   }
		  
	   }};

	   
	   
	   

private Runnable runnable = new Runnable() {
	   @Override
	   public void run() {
		  
		  readDevice1();
		  readDevice2();
		  handler.postDelayed(this, 100);
	       
		  if (mSensor1 == mSensorState.CONNECTED && mSensor2 == mSensorState.CONNECTED){
          String data1 = "D1," + array_2d[0].xaxis +  "," + array_2d[0].yaxis +  "," + array_2d[0].zaxis;
          String data2 = ",D2," + array_2d[1].xaxis +  "," + array_2d[1].yaxis +  "," + array_2d[1].zaxis;
          String data = data1 + data2;
         // Log.i(DEBUG, data);
          
          Intent i = new Intent(bleService.this, PostureService.class);
          i.putExtra("STOP", false);
		  i.putExtra("XVal1", (float) array_2d[0].xaxis);
		  i.putExtra("YVal1",(float) array_2d[0].yaxis);
		  i.putExtra("ZVal1", (float) array_2d[0].zaxis);
		  
		  i.putExtra("XVal2", (float) array_2d[1].xaxis);
		  i.putExtra("YVal2",(float) array_2d[1].yaxis);
		  i.putExtra("ZVal2", (float) array_2d[1].zaxis);
		  startService(i);

		  
		  }
	   else{
		   handler.removeCallbacks(this);
		   stopScan();
		   stopSelf();
	   }
	};
};




private void readDevice1(){
	if (mSensor1 == mSensorState.CONNECTED){
	BluetoothGattCharacteristic c;
	c = mConnectedGatt1.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
	mConnectedGatt1.readCharacteristic(c);
	}
}




private void readDevice2(){
	if (mSensor2 == mSensorState.CONNECTED){
	BluetoothGattCharacteristic c;
	c = mConnectedGatt2.getService(UUID_ACC_SERV).getCharacteristic(UUID_ACC_DATA);
	mConnectedGatt2.readCharacteristic(c);
	}
}




	private void createAppFolder(){
  		final String PATH = Environment.getExternalStorageDirectory() + "/G01Capstone/";
  		if(!(new File(PATH)).exists()) 
  		new File(PATH).mkdirs();
  	}

	
	public void setUpPreferences(){
		String PATH = Environment.getExternalStorageDirectory() + "/wellNode/Posture";

		
		
    	settings = getSharedPreferences("bluetoothPrefs", MODE_PRIVATE);
    	editor = settings.edit();
    	
    	
    	String userName, date,fName;
    	int numFile;
    	
    	numFile = 0;
    	
    	postureSettings = getSharedPreferences("userPrefs", MODE_PRIVATE);
    	postureEditor = postureSettings.edit();
    	userName = "Mike";//postureSettings.getString("name", "Mike");
    	
	    postureSettings = getSharedPreferences("posturePrefs",MODE_MULTI_PROCESS );
	    postureEditor = postureSettings.edit();	
		
	    //numFile = postureSettings.getInt("numFile", 0);
     	now.setToNow();
     	date = "02-24-2015";//now.format("%m-%d-%Y");
     	
        Calendar c = new GregorianCalendar();
	    int day =c.get(Calendar.DAY_OF_MONTH);
	    
		/*if (day != postureSettings.getInt("currentDay", 0)){
			numFile = 0;
		}*/
	    fileName = userName + " Posture " + date;
	    filePath = PATH + "/" + fileName + ".csv";
	    File file = new File(filePath);
	    
	    if(file.exists()){
	    	Log.i("file", "file exists");
	    	while(file.exists()){
	    		numFile++;
	    		fileName = userName + " Posture " + date + "(" + numFile + ")";
	    		filePath = PATH + "/" + fileName + ".csv";
	    		file = new File(filePath);
	    	}
	    	Log.i("file", String.valueOf(numFile));
	    }else{
	    	Log.i("file", "file doenst exists");
	    }
	    
	    
	    
	    Log.d("Files", "Path: " + PATH);
	    File f = new File(PATH);        
	    File file2[] = f.listFiles();
	    Log.d("Files", "Size: "+ file2.length);
	    for (int i=0; i < file2.length; i++)
	    {
	        Log.d("Files", "FileName:" + file2[i].getName());
	    }
     	if (numFile != 0){
     		
     		
     		
     	//	now.setToNow();
         	//date = now.format("%m-%d-%Y");
         	
        	fName = userName + " Posture " + date + "(" + numFile + ")";
        	fileName = fName;
        	fileOps.writeHeader(fName,userName, date);
        	
        	numFile++;
        	postureEditor.putString("fileName", fileName);
        	postureEditor.putInt("numFile", numFile);
        	postureEditor.commit();
        	
        	Log.i("file", fName);
     	}
     	else{
     		
     		fName = userName + " Posture " + date;
        	fileName = fName;
        	fileOps.writeHeader(fName,userName, date);
        	
        	postureEditor.putString("fileName", fileName);
        	numFile++;
        	postureEditor.putInt("numFile", numFile);
        	
        	postureEditor.commit();
     		
     	}
     	
     	
    }
	
	
	public void turnOffSelf(){
		 editor.putBoolean("bluePosture", false);
		 editor.commit();
		 stopSelf();
	}
	
	
	
	
	public SharedPreferences postureSettings;
	public SharedPreferences.Editor postureEditor;
	private PostureFileOperations fileOps = new PostureFileOperations();
	
	private Time now = new Time();

	

	private final BroadcastReceiver mBLEReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			char action = intent.getCharExtra("command", '0');
			
			String userName, fName,date;
			int numFile;
			
			Intent i = new Intent(bleService.this, PostureService.class);
			i.putExtra("STOP", true);
			startService(i);
			
			postureSettings = getSharedPreferences("userPrefs", MODE_PRIVATE);
	    	postureEditor = postureSettings.edit();
	    	userName = postureSettings.getString("name", "Mike");
	    	
		    postureSettings = getSharedPreferences("posturePrefs",MODE_MULTI_PROCESS );
		    postureEditor = postureSettings.edit();	
			
		    numFile = postureSettings.getInt("numFile", 1);
	     	now.setToNow();
         	date = now.format("%m-%d-%Y");
         	
        	fName = userName + " Posture " + date + "(" + numFile + ")";
        	fileName = fName;
        	fileOps.writeHeader(fName,userName, date);
        	
        	postureEditor.putString("fileName", fileName);
        	numFile++;
        	postureEditor.putInt("numFile", numFile);
			
			Log.e("BLESERVICE", "broastcase");
			
			postureEditor.putBoolean("firstTime", true);
			postureEditor.commit();
			
			//stopSelf();

		};
	};


	
};





