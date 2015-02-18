package com.ece4600.mainapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.kymjs.aframe.database.KJDB;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Location extends Activity implements OnClickListener,SensorEventListener {
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CANADA);
	public SharedPreferences postureSettings;
	public SharedPreferences.Editor editor;
	
	private ToggleButton recordButton;
	private Boolean saveState;
	private String userName, fileName;
	private Time now = new Time();
	private locationFileOperations fileOps = new locationFileOperations();
	
	private double zx = 0;
	private double zy = 0;
	
	private WifiAdmin mWifiAdmin;
	private Button btn_map;
	private List<ScanResult> list;
	private ScanResult mScanResult;
	private StringBuffer sb = new StringBuffer();
	private Map<String, String> scanMap = new HashMap<String, String>();
	private List<RecordInfo> xmlList = null;
	private TextView tvXMLResult;
	private TextView tvNowWifi;
	private TextView tvJSResult;
	private boolean ispuase = true;


	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
			
				doJisuan();

				this.sendEmptyMessageDelayed(1, 500);
				break;

			case 2:
				if (scanMap != null) {
					tvNowWifi.setText("Current wifi information" + scanMap.toString());
				}
				break;
			case 3:

				StringBuffer buffer = new StringBuffer();
				String string2 = "total calculated " + count + " times**********Each distance is"
						+ lists.toString();
				buffer.append(string2);
				if (resultList.size() > 0) {
					// find minimum
					// Double maxCount = Collections.max(resultList);
					// Double mixCount = Collections.min(resultList);
					// RecordInfo recordInfo2 = resultMap.get(mixCount);
					// TODO find minimum five

					if (isRoom) {
					
						Double mixCount = Collections.min(resultList);

						buffer.append("**********" + mixCount);
						RecordInfo recordInfo2 = resultMap.get(mixCount);

						buffer.append("**********************current location "
								+ recordInfo2.getRoomName());
						
						tvJSResult.setText(buffer.toString());
					} else {
						
						Collections.sort(resultList);
						
						double zx = 0;
						double zy = 0;
						
						for (int i = 0; i < 5; i++) {
							zx = zx
									+ Double.valueOf(resultMap.get(
											resultList.get(i)).getRoomName());
							zy = zy
									+ Double.valueOf(resultMap.get(
											resultList.get(i)).getSpotName());
							buffer.append(
									"  "
											+ i
											+ "     :"
											+ resultList.get(i)
											+ "***x:"
											+ resultMap.get(resultList.get(i))
													.getRoomName())
									.append("***y:"
											+ resultMap.get(resultList.get(i))
													.getSpotName())
									.append("***");
						}

						zx = ((200+10.81*(zx/5))*9.3677)/9362; //((200+10.81*n)*9.3677)/9362
						zy = ((470+10.81*(zy/5))*9.3677)/6623; //((470+10.81*n)*9.3677)/6623 on y. 
						
						if (saveState){
							fileOps.write(fileName, zx, zy);
						}

						buffer.append("***********current location determined:x is" + zx
								+ "******y is" + zy);
					
						tvJSResult.setText(buffer.toString());
					}

				} else {
					tvJSResult.setText(string2 + "**********not found");

				}

				Log.e("xmlList.size()", "xmlList.size()=" + xmlList.size());
				Log.e("count", "count=" + count);
				break;

			default:
				break;
			}
		}

	};

	private KJDB kjdb;
	SensorManager manager;
	float currentDegree = 120f + 0f;

	private boolean isDegree = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		mWifiAdmin = new WifiAdmin(this);
		manager = (SensorManager) getSystemService(SENSOR_SERVICE);

		isRoom = getIntent().getBooleanExtra("isRoom", false);
		if (isRoom) {
			isDegree = false;
		}
		
		btRoom = (Button) findViewById(R.id.but_isroom);
		btRoom.setText("In Room "+isRoom);
		btRoom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isRoom) {
					isRoom = false;
					isDegree = true;
				}else{
					isRoom = true;
					isDegree = false;
				}
				btRoom.setText("Room "+isRoom);
			}
		});
		findViewById(R.id.read_xml).setOnClickListener(this);
		btCheck = (Button) findViewById(R.id.check_data);
		btCheck.setOnClickListener(this);

		//findViewById(R.id.save_data).setOnClickListener(this);
		tvXMLResult = (TextView) findViewById(R.id.tv_read_result);
		tvNowWifi = (TextView) findViewById(R.id.tv_now_wifi);
		tvJSResult = (TextView) findViewById(R.id.tv_jisuan_result);
		btPause = (Button) findViewById(R.id.pause);
		btn_map = (Button) findViewById(R.id.btn_map);
		btn_map.setOnClickListener(this);
		btReturn=(Button) findViewById(R.id.button1);
		recordButton = (ToggleButton)findViewById(R.id.locationSave);
		
		setUpPreferences();
		saveState = false;
		recordButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(recordButton.isChecked()){  // Start recording
					saveState = true;
					
					now.setToNow();
					fileName = userName+ " Location " + now.format("%m-%d-%Y %H-%M-%S") + ".csv";
					fileOps.writeHeader(fileName, userName, now.format("%m-%d-%Y"));
				}
				else{ // stop recording

				   saveState = false;
				}
				
			}
		});
		
		btReturn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				startActivity(new Intent(Location.this, MainActivity.class));
				finish();
				
			}
		});
		
		btPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ispuase == false) {
					ispuase = true;
					btPause.setText("Pause");
					btCheck.setText("Start");
					mHandler.removeMessages(1);
					mHandler.removeMessages(2);
					mHandler.removeMessages(3);
				} else {
					Toast.makeText(Location.this, "Not Started", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		manager.registerListener(this,
				manager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		manager.unregisterListener(this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		ispuase = true;
		mHandler.removeMessages(1);
		mHandler.removeMessages(2);
		mHandler.removeMessages(3);
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.read_xml:
			Toast.makeText(this, "current Degree" + currentDegree,
					Toast.LENGTH_SHORT).show();
			new ImportDatabaseTask().execute();
			break;
		//case R.id.save_data:
			
			//saveXML();
			//break;
       
		case R.id.check_data:
			if (ispuase) {
				ispuase = false;
				btPause.setText("Pause");
				btCheck.setText("Calculating");
				mHandler.sendEmptyMessage(1);
			} else {
				Toast.makeText(Location.this, "Already Started", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_map:
			Bundle bundle = new Bundle();
			Intent intent = new Intent();
			bundle.putDouble("zx", zx);
			bundle.putDouble("zy", zy);
			intent.setClass(Location.this, Location_map.class);
			intent.putExtras(bundle);
			startActivity(intent);
			Toast.makeText(this, "zx="+zx+"zy="+zy,Toast.LENGTH_LONG).show();
		break;

		default:
			break;
		}
	}
	public void onBackPressed() {
		// do something on back.return;
		startActivity(new Intent(Location.this, MainActivity.class));
		finish();
	}

	private void doJisuan() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				scanWifi();
			}
		}).start();

	};

	private void scanWifi() {
		
		if (sb != null) {
			sb = new StringBuffer();
		}
	
		mWifiAdmin.startScan();
		list = mWifiAdmin.getWifiList();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				
				mScanResult = list.get(i);
				String mac = mScanResult.BSSID;
				String level = "" + mScanResult.level;

				scanMap.put(mac, level);
			}

			mHandler.sendEmptyMessage(2);

			
			checkData();
		}
	}

	
	private void readData() {
		Log.e("sss", "readData");
		datalist = kjdb.findAll(User.class);

		if (datalist != null) {
			tvXMLResult.setText("database result" + datalist.toString());
		}

		xmlList = new ArrayList<RecordInfo>();

		for (int i = 0; i < datalist.size(); i++) {
			User user = datalist.get(i);
			RecordInfo recordInfo2 = new RecordInfo();
			if (isRoom) {
				recordInfo2.setRoomName(user.getRoom());
				recordInfo2.setSpotName("");
			} else {
				recordInfo2.setRoomName(user.getX());
				recordInfo2.setSpotName(user.getY());
			}

			List<WifiInfo> wifiList = new ArrayList<WifiInfo>();
			wifiList.add(new WifiInfo(Constans.ROUTER1, user.getRouter1()));
			wifiList.add(new WifiInfo(Constans.ROUTER2, user.getRouter2()));
			wifiList.add(new WifiInfo(Constans.ROUTER3, user.getRouter3()));
			wifiList.add(new WifiInfo(Constans.ROUTER4, user.getRouter4()));
			wifiList.add(new WifiInfo(Constans.ROUTER5, user.getRouter5()));
			wifiList.add(new WifiInfo(Constans.ROUTER6, user.getRouter6()));
			recordInfo2.setWifiInfos(wifiList);
			xmlList.add(recordInfo2);
		}

	}

	/**
	 * 
	 * 
	 * AP1: e8:de:27:7b:97:1c AP2: e8:de:27:36:52:ee AP3: e8:de:27:7b:97:42 AP4:
	 * e8:de:27:36:54:2e AP5: e8:de:27:7b:97:52 AP6: e8:de:27:36:54:40
	 */
	private void checkData() {

		
		if (lists.size() > 0) {
			lists.clear();
		}
	
		if (resultList.size() > 0) {
			resultList.clear();
		}
		
		if (resultMap.size() > 0) {
			resultMap.clear();
		}

		count = 0;
		if (xmlList != null && xmlList.size() > 0) {
			for (int i = 0; i < xmlList.size(); i++) {
		
				RecordInfo recordInfo = xmlList.get(i);
				
				List<WifiInfo> wifiInfos = recordInfo.getWifiInfos();
				
				double doDistance = doDistance(wifiInfos);

				if (doDistance != 0.0) {
					count++;
					lists.add(doDistance);

					resultList.add(doDistance);
			
					resultMap.put(doDistance, recordInfo);
				}
			}
			mHandler.sendEmptyMessage(3);
		}
	}

	private int count = 0;

	private List<Double> lists = new ArrayList<Double>();
	private List<Double> resultList = new ArrayList<Double>();
	private Map<Double, RecordInfo> resultMap = new HashMap<Double, RecordInfo>();
	private Button btPause;
	private Button btCheck;
	private Button btReturn;

	double nowDistance = 0;

	
	private double doDistance(List<WifiInfo> wifiInfos) {

		nowDistance = 0;

		if (scanMap != null) {
			for (int i = 0; i < wifiInfos.size(); i++) {
				WifiInfo wifiInfo = wifiInfos.get(i);
				if (scanMap.containsKey(wifiInfo.getBssid())) {
					
					int xmlLevel = Integer.valueOf(scanMap.get(wifiInfo
							.getBssid()));
				
					int nowLevel = wifiInfo.getLevel();
			
					int newLevel = Math.abs(xmlLevel - nowLevel);
				
					nowDistance += Math.pow(newLevel, 2);

				}
			}

		}

		Log.e("Math.sqrt(nowDistance)", "" + Math.sqrt(nowDistance));
		return Math.sqrt(nowDistance);
	}

	
	private void saveXML() {
		stringBuffer.append("result:").append(tvXMLResult.getText().toString())
				.append("***nowWifi:").append(tvNowWifi.getText().toString())
				.append("***JSResult:").append(tvJSResult.getText().toString());
		pATH = Environment.getExternalStorageDirectory() + "/MyData/";
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File f = new File(pATH);
			if (!f.exists()) {
				f.mkdir();
			}
			setDatasToSD();
		} else {
			Toast.makeText(getApplicationContext(), "SD can't work",
					Toast.LENGTH_LONG).show();
		}
	}

	StringBuffer stringBuffer = new StringBuffer();
	private String pATH;
	private List<User> datalist;
	private boolean isRoom;
	private Button btRoom;

	private void setDatasToSD() {
		if (stringBuffer.toString() != null && stringBuffer.toString() != "") {

			ObjectOutputStream oos = null;
			try {
				Person person = new Person(stringBuffer.toString());
				stringBuffer = new StringBuffer();
				FileOutputStream fos = new FileOutputStream(pATH + ""
						+ saveNowTime() + "record" + ".txt");
				oos = new ObjectOutputStream(fos);
				oos.writeObject(person);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (oos != null) {
					try {
						oos.close();
						Toast.makeText(getApplicationContext(),
								"save success!", Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private String saveNowTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date curDate = new Date(System.currentTimeMillis());
		String str = formatter.format(curDate);
		return str;
	}

	private class ImportDatabaseTask extends AsyncTask<Void, Void, String> {

		private final ProgressDialog dialog = new ProgressDialog(Location.this);

		@Override
		protected void onPreExecute() {

			this.dialog.setMessage("Getting database...");

			this.dialog.show();

		}

		@Override
		protected String doInBackground(final Void... args) {

			if (!Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {

				return "Couldn't find SD card";

			}

			String wifiData = "wifiData";
		
			if (isDegree) {
				if (45 <= currentDegree && currentDegree < 135) {
					wifiData = "wifiData90";
				} else if (135 <= currentDegree && currentDegree < 225) {
					wifiData = "wifiData180";
				} else if (225 <= currentDegree && currentDegree < 315) {
				wifiData = "wifiData270";
				} else {
					wifiData = "wifiData0";
				}
			} else {
				wifiData = "wifiRoom";
			}

			
			File dbBackupFile = new File(
					Environment.getExternalStorageDirectory(), wifiData);

			if (!dbBackupFile.exists()) {

				return "couldn't find: " + wifiData;

			} else if (!dbBackupFile.canRead()) {

				return "found SDcard" + wifiData + "but can't read!";

			}

			Log.e("sss", "currentDegree:" + currentDegree);
			Log.e("sss", "wifiData" + wifiData);

			kjdb = KJDB.create(Location.this, Environment
					.getExternalStorageDirectory().toString(), wifiData, true);
			return "Imported completed!";

		}

		@Override
		protected void onPostExecute(final String msg) {

			if (this.dialog.isShowing()) {

				this.dialog.dismiss();

			}

			Toast.makeText(Location.this, msg, Toast.LENGTH_SHORT).show();

			readData();
		}

	}

	

	public static void copyFile(File src, File dst) throws IOException {

		FileChannel inChannel = new FileInputStream(src).getChannel();

		FileChannel outChannel = new FileOutputStream(dst).getChannel();

		try {

			inChannel.transferTo(0, inChannel.size(), outChannel);

		} finally {

			if (inChannel != null)

				inChannel.close();

			if (outChannel != null)

				outChannel.close();

		}

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float degree = Math.round(event.values[0]);
		currentDegree = degree;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
	
	public void setUpPreferences(){
		
		postureSettings = getSharedPreferences("userPrefs", MODE_PRIVATE);
    	editor = postureSettings.edit();
    	
    	userName = postureSettings.getString("name", "Mike");
	}
	
	
}
