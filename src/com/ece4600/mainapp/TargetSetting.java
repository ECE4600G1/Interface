package com.ece4600.mainapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TargetSetting extends Activity {
	
	public SharedPreferences settingst;
	public SharedPreferences.Editor editort;
	private Button save, cancel;
	private EditText target, size;
	private boolean textflag =  false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_target_setting);
		
		target = (EditText)findViewById(R.id.pedo_targetnum);
		size = (EditText)findViewById(R.id.pedo_sizenum);
		save = (Button)findViewById(R.id.pedo_save);
	    cancel = (Button)findViewById(R.id.pedo_can);
	    setUpPreferences();
	    setUpButtons();
	}

	private void setUpButtons() {
		// TODO Auto-generated method stub
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				editort.putString("target", target.getText().toString());
				editort.putString("size", size.getText().toString());				
				editort.commit();
				emptyText();
				if (textflag == true){
				startActivity(new Intent(TargetSetting.this, Pedometer.class));
				finish();
				}			
			}
		});	
		
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(TargetSetting.this, Pedometer.class));
				finish();
			}
		});	
	}

	public void emptyText(){
		if (size.getText().toString().equals("") || target.getText().toString().equals("") || size.getText().toString().equals("0") ||  target.getText().toString().equals("0") ){
			AlertDialog.Builder alertDialogHint = new AlertDialog.Builder(this);
			alertDialogHint.setMessage("Inputs are EMPTY! Please enter again!");
			alertDialogHint.setPositiveButton("OK",
			new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(TargetSetting.this,TargetSetting.class);
					startActivity(i);
					finish();
				}
			});
			AlertDialog alertDialog = alertDialogHint.create();
			alertDialog.show();
		}else{
			textflag = true;
		}
	}
	
	private void setUpPreferences() {
		// TODO Auto-generated method stub
    	settingst = getSharedPreferences("pedoPrefs", MODE_PRIVATE);
    	editort = settingst.edit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.target_setting, menu);
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
