package com.ece4600.mainapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class EditUserSettings extends Activity {

	public SharedPreferences settings;
	public SharedPreferences.Editor editor;
	
	Spinner month, day, year;
	final List<String> listMonth=new ArrayList<String>();
	final List<String> listDay=new ArrayList<String>();
	final List<String> listYear=new ArrayList<String>();
	
	Button save, cancel;
	EditText firstName, lastName, weight;
	RadioGroup sex;
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_usersettings);


	    month = (Spinner)findViewById(R.id.month);
	    day = (Spinner)findViewById(R.id.day);
	    year = (Spinner)findViewById(R.id.year);
	    
	    firstName = (EditText)findViewById(R.id.patientFirstName);
	    lastName = (EditText)findViewById(R.id.patientLastName);
	    weight = (EditText)findViewById(R.id.weight);
	    
	    sex = (RadioGroup)findViewById(R.id.sex);
	    
	    save = (Button)findViewById(R.id.userRegister);
	    cancel = (Button)findViewById(R.id.userCancel);
	    
	    setUpPreferences();
	    setUpSpinners();
	    setUpButtons();
//	    restorePreferences();
	}
	
	public void setUpButtons(){
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String temp;
				
				temp = firstName.getText().toString().replace(" ", "") 
						+ " " + lastName.getText().toString().replace(" ", "");
				
				editor.putString("name", temp);
				
				editor.putString("weight", weight.getText().toString());
				
				int id = sex.getCheckedRadioButtonId();
				
				if (id == R.id.Other){
			        editor.putString("sex", "Female");
			    }
				else{
					editor.putString("sex", "Male");
				}
				
				temp = month.getSelectedItem().toString() + "/" + day.getSelectedItem().toString() + "/" + year.getSelectedItem().toString();
				
				editor.putString("DOB", temp);
				
				editor.commit();
				
				startActivity(new Intent(EditUserSettings.this, MainActivity.class));
				finish();
				
			}
		});	
		
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(EditUserSettings.this, MainActivity.class));
				finish();
			}
		});	
	}
	
	public void setUpSpinners(){
		listMonth.add("Month");
		listMonth.add("1");
		listMonth.add("2");
		listMonth.add("3");
		listMonth.add("4");
		listMonth.add("5");
		listMonth.add("6");
		listMonth.add("7");
		listMonth.add("8");
		listMonth.add("9");
		listMonth.add("10");
		listMonth.add("11");
		listMonth.add("12");
		
		ArrayAdapter<String> adp1=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listMonth);
		adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		month.setAdapter(adp1);
		
		listDay.add("Day");
		for (int i=1; i<=31; i++){
			listDay.add(String.valueOf(i));
		}
		
		ArrayAdapter<String> adp2=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listDay);
		adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		day.setAdapter(adp2);
		
		
		listYear.add("Year");
		for (int i=2015; i>=1950; i--){
			listYear.add(String.valueOf(i));
		}
		
		ArrayAdapter<String> adp3=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listYear);
		adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		year.setAdapter(adp3);
		
		
	}
	
	public void setUpPreferences(){
    	settings = getSharedPreferences("userPrefs", MODE_PRIVATE);
    	editor = settings.edit();
    }
	
//	public void restorePreferences(){
//		String[] name = settings.getString("name", "Mike Jones").split(" ");
//		firstName.setText(name[0]);
//		lastName.setText(name[1]);
//		
//		String[] dob = settings.getString("DOB", "1/1/2015").split("/");
//		month.setSelection(Integer.valueOf(dob[0]));
//		day.setSelection(Integer.valueOf(dob[1]));
//		year.setSelection(2015 - Integer.valueOf(dob[2]));
//		
//		if(settings.getString("sex","Male").equals("Male")){
//			sex.check(R.id.Male);
//		}
//		else{
//			sex.check(R.id.Female);
//		}
//		
//		weight.setText(settings.getString("weight", ""));
//		
//		
//	}

}
