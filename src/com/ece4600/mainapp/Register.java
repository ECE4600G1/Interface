package com.ece4600.mainapp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class Register extends Activity {

	public SharedPreferences settings;
	public SharedPreferences.Editor editor;
	
	Spinner month, day, year;
	final List<String> listMonth=new ArrayList<String>();
	final List<String> listDay=new ArrayList<String>();
	final List<String> listYear=new ArrayList<String>();
	
	Button register, cancel;
	EditText firstName_android, lastName_android, weight_android,username_android,password_android,phone_android,address_android;
	RadioGroup sex_android;
    // database constants defined here //
	private static final String LOGIN_URL = "http://wellnode.ca/webservice/register.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_USERNAME = "username";
    private static final String TAG_PASSWORD = "password";
    private static final String TAG_FIRSTNAME = "firstname";
    private static final String TAG_LASTNAME = "lastname";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_PHONENUMBER = "phone";
    private static final String TAG_DOB = "dob_string";
    private static final String TAG_SEX = "sex";
    private static final String TAG_WEIGHT = "weight";
	
	
    JSONParser jsonParser = new JSONParser();
    private ProgressDialog pDialog;
    
	
	//defination ends here
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_register);


	    month = (Spinner)findViewById(R.id.month);
	    day = (Spinner)findViewById(R.id.day);
	    year = (Spinner)findViewById(R.id.year);
	    
	    firstName_android = (EditText)findViewById(R.id.patientFirstName);
	    lastName_android = (EditText)findViewById(R.id.patientLastName);
	    weight_android = (EditText)findViewById(R.id.weight);

	    sex_android = (RadioGroup)findViewById(R.id.sex);
	    
	    register = (Button)findViewById(R.id.userRegister);
	    cancel = (Button)findViewById(R.id.userCancel);
	    phone_android = (EditText)findViewById(R.id.phone);
	    username_android = (EditText)findViewById(R.id.username);
	    password_android = (EditText)findViewById(R.id.password);
	    address_android = (EditText)findViewById(R.id.address);
	    
	    
	    
//	    setUpPreferences();
	    setUpSpinners();
	    setUpButtons();
//	    restorePreferences();
	}
	
	public void setUpButtons(){
		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				String temp;
//				
//				temp = firstName.getText().toString().replace(" ", "") 
//						+ " " + lastName.getText().toString().replace(" ", "");
//				
//				editor.putString("name", temp);
//				
//				editor.putString("weight", weight.getText().toString());
//				
//				int id = sex.getCheckedRadioButtonId();
//				
//				if (id == R.id.Female){
//			        editor.putString("sex", "Female");
//			    }
//				else if (id == R.id.Male)
//				{
//					editor.putString("sex", "Male");
//				}
//				else{
//					editor.putString("sex", "Other");
//				}
//				
//				temp = month.getSelectedItem().toString() + "/" + day.getSelectedItem().toString() + "/" + year.getSelectedItem().toString();
//				
//				editor.putString("DOB", temp);
				
//				editor.commit();
//				
				new AttemptRegister().execute();
				
		//		startActivity(new Intent(Register.this, Login.class));
		//		finish();
				
			}
		});	
		
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Register.this, Login.class));
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
	
	class AttemptRegister extends AsyncTask<String, String, String> {

		 /**
	  * Before starting background thread Show Progress Dialog
	  * */
		boolean failure = false;
		
	 @Override
	 protected void onPreExecute() {
	     super.onPreExecute();
	     pDialog = new ProgressDialog(Register.this);
	     pDialog.setMessage("Attempting Registeration...");
	     pDialog.setIndeterminate(false);
	     pDialog.setCancelable(true);
	     pDialog.show();
	 }
		
		@Override
		protected String doInBackground(String... args) {
			// TODO Auto-generated method stub
			 // Check for success tag
	     int success;
	     
	     String sex,dob;

	     int id = sex_android.getCheckedRadioButtonId();
			
			if (id == R.id.Female1){
		        sex = "Female";
		    }
			else if (id == R.id.Male)
			{
				sex = "Male";
			}
			else{
				sex = "Other";
			}

			dob = month.getSelectedItem().toString() + "/" + day.getSelectedItem().toString() + "/" + year.getSelectedItem().toString();
			

	     
	     
//	     String firstname_database, lastname_database;
	     String username = username_android.getText().toString();
	     String password = password_android.getText().toString();
	     String phone = phone_android.getText().toString();
	     String firstname = firstName_android.getText().toString();
	     String lastname = lastName_android.getText().toString();
	     String address = address_android.getText().toString();
	     String weight = weight_android.getText().toString();
//	     String name = firstName_android.getText().toString().replace(" ", "") 
//					+ " " + lastName_android.getText().toString().replace(" ", "");
	     
	     
//	     String password = password_android.getText().toString();
	     try {
	         // Building Parameters
	         List<NameValuePair> params = new ArrayList<NameValuePair>();
	         params.add(new BasicNameValuePair(TAG_USERNAME, username));
	         params.add(new BasicNameValuePair(TAG_PASSWORD, password));
	         params.add(new BasicNameValuePair(TAG_FIRSTNAME, firstname));
	         params.add(new BasicNameValuePair(TAG_LASTNAME, lastname));
	         params.add(new BasicNameValuePair(TAG_PHONENUMBER, phone));
	         params.add(new BasicNameValuePair(TAG_ADDRESS, address));
	         params.add(new BasicNameValuePair(TAG_WEIGHT, weight));
	         params.add(new BasicNameValuePair(TAG_SEX, sex));
	         params.add(new BasicNameValuePair(TAG_DOB, dob));
//	         params.add(new BasicNameValuePair("username", username));
	         
	         
//	         private static final String TAG_SUCCESS = "success";
//	         private static final String TAG_MESSAGE = "message";
//	         private static final String TAG_USERNAME = "username";
//	         private static final String TAG_PASSWORD = "password";
//	         private static final String TAG_FIRSTNAME = "firstname";
//	         private static final String TAG_LASTNAME = "lastname";
//	         private static final String TAG_ADDRESS = "address";
//	         private static final String TAG_PHONENUMBER = "phone";
//	         private static final String TAG_DOB = "dob_string";
//	         private static final String TAG_SEX = "sex";
//	         private static final String TAG_WEIGHT = "weight";
	         
	         
	         
	         
	         
	         
	         
	         
	     //    params.add(new BasicNameValuePair("password", password));

	         Log.d("request!", "starting");
	         // getting product details by making HTTP request
	         JSONObject json = jsonParser.makeHttpRequest(
	                LOGIN_URL, "POST", params);

	         // check your log for json response
	         Log.d("Registering", json.toString());

	         // json success tag
	         success = json.getInt(TAG_SUCCESS);

	         
	         if (success == 1) {
	//             firstname_database = json.getString(TAG_FIRSTNAME);
	//             lastname_database = json.getString(TAG_LASTNAME);
	        	 Log.d("Login Successful!", json.toString());
	         	//Intent i = new Intent(Login.this, ReadComments.class);
	         	Intent i = new Intent(getApplicationContext(), Login.class);
//	         	i.putExtra("database_user",username_result); // this is where the perference is sent through . need to see how perference is setup. 
//	         	i.putExtra("database_address",user_address);
//	        	editor.putString("name", username_result);
//	        	editor.putString("weight", user_address);
//	        	editor.commit();
	         	finish();
					startActivity(i);
	         	return "Thank you for registering";
	         }else{
	        	
	         	Log.d("Login Failure!", json.getString(TAG_MESSAGE));
	       //  	Toast.makeText(getApplicationContext(), json.getString(TAG_MESSAGE),Toast.LENGTH_LONG).show();
	         	return json.getString(TAG_MESSAGE);
	         	
	         }
	     } catch (JSONException e) {
	         
	    	 e.printStackTrace();
	     }

	     return null;
			
		}
		
		

		
		/**
	  * After completing background task Dismiss the progress dialog
	  * **/
	 protected void onPostExecute(String result) {
	     // dismiss the dialog once product deleted
	     pDialog.dismiss();
	     if (result != null){
	     	Toast.makeText(Register.this, result, Toast.LENGTH_LONG).show();
	     		}

	 		}

		}
	
	
//	public void setUpPreferences(){
//    	settings = getSharedPreferences("userPrefs", MODE_PRIVATE);
//    	editor = settings.edit();
//    }
//	
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

