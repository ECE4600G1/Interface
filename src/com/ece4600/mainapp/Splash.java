package com.ece4600.mainapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Splash extends Activity {
//	String user;
//	String pass;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		startScreen();
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//		user = preferences.getString("user", "");
//		pass = preferences.getString("pass", "");		
	}
	
    private void startScreen(){
    	Button messageButton = (Button)findViewById(R.id.startscreen);
    	messageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(MainActivity.this, "Pedometer", Toast.LENGTH_LONG).show();
				startActivity(new Intent(Splash.this, MainActivity.class)); // Login.java or Mainactivity.java
				finish();
			}
//			public void onClick(View v) {
//				if(user.equals("user") && pass.equals("pass")){
//					startActivity(new Intent(Splash.this, Bluetooth.class));
//					finish();
//				}
//				else{
//					//TODO change back to login screen
//					startActivity(new Intent(Splash.this, Bluetooth.class));
//					finish();
//				}
//			}
		});	
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
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
}
