package com.ece4600.mainapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class About extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        TextView t2 = (TextView) findViewById(R.id.textView3);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
	
	    Button returnButtton = (Button)findViewById(R.id.button1);
	    
	    returnButtton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});	
	}

}
