package com.ece4600.mainapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qozix.tileview.TileView;
import com.qozix.tileview.markers.MarkerEventListener;

import android.widget.ImageView;

public class Location_map extends Activity {

    TileView tileView;
    MyTask objMyTask;
    double x_pos, y_pos;
    private static final int SLEEP_TIME=((60*1000)/1000);

    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        // Create our TileView
        tileView = new TileView(this);

        // Set the minimum parameters
        tileView.setSize(9362,6623);
        tileView.addDetailLevel(1f, "tiles/1000_%col%_%row%.png", "downsamples/map.png");
        tileView.addDetailLevel(0.5f, "tiles/500_%col%_%row%.png", "downsamples/map.png");
        tileView.addDetailLevel(0.25f, "tiles/250_%col%_%row%.png", "downsamples/map.png");
        tileView.addDetailLevel(0.125f, "tiles/125_%col%_%row%.png", "downsamples/map.png");
        // Add the view to display it
        setContentView(tileView);
        // use pixel coordinates to roughly center it
        // they are calculated against the "full" size of the mapView 
        // i.e., the largest zoom level as it would be rendered at a scale of 1.0f
//        tileView.moveToAndCenter( 9362,6623 );
//        tileView.slideToAndCenter( 9362,6623 );

        tileView.defineRelativeBounds( 0, 0, 1, 1 );
        tileView.moveToAndCenter( 0.5, 0.5);
//        frameTo( 0.5, 0.5 );
        
        // Set the default zoom (zoom out by 4 => 1/4 = 0.25)
        tileView.setScale( 0.125 );
  //      tileView.addMarkerEventListener(Calculate_EventListener);
        
       // ImageView markerA = new ImageView(this);
      //  markerA.setImageResource(R.drawable.calculator_small); // can use another image for calculate
       // markerA.setTag("Calculate");

        ImageView markerB = new ImageView(this);
        markerB.setImageResource(R.drawable.maps_marker_blue_small);
        markerB.setTag("User Location");
        markerB.setOnClickListener( markerClickListener );
        
       // tileView.addMarker(markerA, 0.1, 0.16, -0.5f, -a1.0f); // horizontal, vertical 
        
        //        tileView.removeMarker(markerA);
     
        Bundle bundle =getIntent().getExtras();
        x_pos = bundle.getDouble("zx");
        y_pos = bundle.getDouble("zy");
        tileView.addMarker(markerB, x_pos, y_pos+0.05,-0.5f, -1.0f);		

    }
    //n,n should be at ((200+10.81*n)*9.3677)/9362 on x and ((470+10.81*n)*9.3677)/6623 on y. 
//    public MarkerEventListener Calculate_EventListener = new MarkerEventListener() {
//    	@Override
//    	public void onMarkerTap( View markerB, int x, int y ) {
//    	Toast.makeText( getApplicationContext(), "Calculating", Toast.LENGTH_SHORT).show();
//		objMyTask = new MyTask();
//		objMyTask.execute();
//		tileView.moveMarker(markerB, x_pos, y_pos,-0.5f, -1.0f);
//	//	tileView.addMarker(markerA, 0.3, 0.4);
//    	}
//    };
//    
    
    class MyTask extends AsyncTask<Void, Integer, Void> {

		Dialog dialog;
		ProgressBar progressBar;
		TextView tvLoading,tvPer;
		Button btnCancel;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(Location_map.this);
			dialog.setCancelable(false);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.progressdialog);

			progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar1);
			tvLoading = (TextView) dialog.findViewById(R.id.tv1);
			tvPer = (TextView) dialog.findViewById(R.id.tvper);
			btnCancel = (Button) dialog.findViewById(R.id.btncancel);

			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					objMyTask.cancel(true);
					dialog.dismiss();
				}
			});

			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			 
/// put algorithm here -------------> result should be output into x_pos and y_pos 
// x_pos and y_pos are all normalized to 1. // might need conversion to get the right values
			
					
			//	{x_pos = x_pos +0.1;
				//y_pos = y_pos +0.1;
				//if (x_pos > 1){
				//	x_pos = 0.3;
				//}
			//	if (y_pos > 1){
				//	y_pos =0.1;
				//}
				//}

				
				
				
/// <------------------------------------
//				{
//					System.out.println(i);
//					publishProgress(i);
//					try {
//						Thread.sleep(SLEEP_TIME);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
			// can use arraylist to implement a tracer. each time the x_pos and y_pos are generated
			//	place the result into an array list and then use tileView.Drawpath to connect the dots.

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressBar.setProgress(values[0]);
			tvLoading.setText("Calculating " + values[0] + " %");
			tvPer.setText(values[0]+" %");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
//			x_pos = 0.2;
//			y_pos = 0.2;
			final Toast toast = Toast.makeText(getApplicationContext(), "Location Determination Complete", Toast.LENGTH_SHORT);
			    toast.show();
			    Handler handler = new Handler();
			        handler.postDelayed(new Runnable() {
			           @Override
			           public void run() {
			               toast.cancel(); 
			           }
			    }, 700);
			dialog.dismiss();
			//can make a toast once it is done
			
			

	       
			//	AlertDialog alert = new AlertDialog.Builder(Location.this).create();

		//		alert.setTitle("Completed!!!");
		//		alert.setMessage("Your Task is Completed SuccessFully!!!");
//			alert.setButton("Dismiss", new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//
//				}
//			});
//			alert.show();
		}
	}
	
    public TileView getTileView(){
		return tileView;
	}
	
    
	private View.OnClickListener markerClickListener = new View.OnClickListener() {
			@Override
				    public void onClick( View markerB) {
					// get reference to the TileView
					TileView tileView = getTileView();
					
					final Toast toast = Toast.makeText(getApplicationContext(), "Calculating", Toast.LENGTH_SHORT);
				    toast.show();
				    Handler handler = new Handler();
				        handler.postDelayed(new Runnable() {
				           @Override
				           public void run() {
				               toast.cancel(); 
				           }
				    }, 100);
				        // x_meter = x_pos * factor  ((200+10.81*n)*9.3677)/9362 on x and ((450-10.81*n)*9.3677)/6623 on y. 
					SampleCallout callout = new SampleCallout( markerB.getContext() , ((x_pos)*9362/9.3677-185)/10.81, (450-(y_pos)*6623/9.3677)/10.81); // x_pos->x_meter
					
					tileView.addCallout( callout, x_pos, y_pos, -0.5f, -1.0f );
					callout.transitionIn();
					objMyTask = new MyTask();
					objMyTask.execute();
					tileView.moveMarker(markerB, x_pos, y_pos+0.05,-0.5f, -1.0f);
					tileView.moveToAndCenter(x_pos, y_pos);
// we saved the coordinate in the marker's tag
					
// lets center the screen to that coordinate
//					tileView.slideToAndCenter( position[0], position[1] );
// create a simple callout
					
// add it to the view tree at the same position and offset as the marker that invoked it
					
// a little sugar
					
			}
};
    
    
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

