package com.ece4600.mainapp;



import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class PostureService extends Service{
	public static dataArrayFloat[] array_10_D1 = new dataArrayFloat[11];
	public static dataArrayFloat[] array_10_D2 = new dataArrayFloat[11];
	public static int i,counter;
	public static String postureState, newPosture, nowPosture,changePosture;
	public static dataArrayFloat[] array_2d = new dataArrayFloat[2];
	
	private static float avgX1, avgY1, avgZ1, avgX2, avgY2, avgZ2;
	private double roll_1, roll_2,pitch_1,pitch_2 , roll_1_comp, roll_2_comp;
	private double probBack, probFront, probLeft, probRight;
	private double wScoreLBS, wScoreLFS, wScoreLLS, wScoreLRS;
	private double wScoreSIT, wScoreBEND, wScoreSTAND;
	
	
	//CONSTANTS
	private static double threshold_1_roll = (float)30.0;
	private static double threshold_2_roll = (float)30.0;
	private static float THRESHOLD_CONSTANT = (float) 0.85; // not used
	private static int threshold_counterPosture = (int) 3;
	private static int totalAvgNum1 = (int) 11; // number in average plus 1
	private static float divideAvg = 10.0f;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override 
	public void onCreate(){
		/* Called when service is first created
		 * one time setup. Not called if already running*/
		probBack = 0.0;
		probFront = 0.0;
		probLeft = 0.0;
		probRight = 0.0;
		
		i = 0;
		postureState = "STAND";
		newPosture = "STAND";
		changePosture = "STAND";
		nowPosture = "STAND";
		
		Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
		h.postDelayed(resendPosture, 1000);	
		
		super.onCreate();
	}

	@Override 
	public int onStartCommand(Intent intent, int flags, int startId){
		/*Called by the system when an app component requests that a 
		 * Service start using startService()
		 * Once started, it can run in the background indefinitely*/
		
		float xValue1 = intent.getFloatExtra("XVal1", 0.0f);
		float yValue1 = intent.getFloatExtra("YVal1", 0.0f);
		float zValue1 = intent.getFloatExtra("ZVal1", 0.0f);
		
		float xValue2 = intent.getFloatExtra("XVal2", 0.0f);
		float yValue2 = intent.getFloatExtra("YVal2", 0.0f);
		float zValue2 = intent.getFloatExtra("ZVal2", 0.0f);
		
		
		
		double dummy = Math.sqrt(xValue1 * xValue1 +  yValue1 * yValue1 +  zValue1 * zValue1 );
		
		if ((dummy >0.75)&&(dummy < 1.25))  {
			dataArrayFloat data = new dataArrayFloat(xValue1, yValue1, zValue1);
			array_10_D1[i] = data;
			dataArrayFloat data2 = new dataArrayFloat(xValue2, yValue2, zValue2);
			array_10_D2[i] = data2;
			Log.v("PostureService", "Recieved data");
			i++;
		}
		
		
		if (i == totalAvgNum1) //i = 11 to get rid of null pointer exception
		{
			i = 0;
			//running =true;
			//pThread.run();
			calculatePosture();
		}
		
		return super.onStartCommand(intent,flags, startId);
	}//end of onStartCommand(...)

	@Override 
	public void onDestroy(){
		/*Called when a service no longer used and being destroyed*/
		super.onDestroy();
	}// End of onDestroy
	
	
	
	private void calculatePosture(){
		avgX1 = 0;
		avgY1 = 0;
		avgZ1 = 0;
		
		avgX2 = 0;
		avgY2 = 0;
		avgZ2 = 0;
		
		for(int m = 1; m< totalAvgNum1;m++){
			avgX1 +=  array_10_D1[m].xaxis;
			avgY1 += array_10_D1[m].yaxis;
			avgZ1 +=  array_10_D1[m].zaxis;
			
			avgX2 +=  array_10_D2[m].xaxis;
			avgY2 += array_10_D2[m].yaxis;
			avgZ2 +=  array_10_D2[m].zaxis;
			
		}
		
		avgX1 = avgX1 / divideAvg;
		avgY1 = avgY1 / divideAvg;
		avgZ1 = avgZ1 / divideAvg;
		
		avgX2 = avgX2 / divideAvg;
		avgY2 = avgY2 / divideAvg;
		avgZ2 = avgZ2 / divideAvg;
		
	
		
		Handler hAvg = new Handler(Looper.getMainLooper());
		hAvg.post(new Runnable(){
			@Override
			public void run(){
				Intent i = new Intent("POSTURE_EVENT");
				
				i.putExtra("avgX1", avgX1);
				i.putExtra("avgY1", avgY1);
				i.putExtra("avgZ1", avgZ1);
				
				i.putExtra("avgX2", avgX2);
				i.putExtra("avgY2", avgY2);
				i.putExtra("avgZ2", avgZ2);
				
				sendBroadcast(i);
			}
		});
		
		
		float dumAvgZ1, dumAvgY1, dumAvgZ2, dumAvgY2;
		
		if (Math.abs(avgZ1) < 0.2){
			dumAvgZ1 = 0.0f;
		}
		else
			dumAvgZ1 = avgZ1;
		
		if (Math.abs(avgY1) < 0.2){
			dumAvgY1 = 0.0f;
		}
		else
			dumAvgY1 = avgY1;
		
		
		
		if (Math.abs(avgZ2) < 0.2){
			dumAvgZ2 = 0.0f;
		}
		else
			dumAvgZ2 = avgZ2;
		
		if (Math.abs(avgY2) < 0.2){
			dumAvgY2 = 0.0f;
		}
		else
			dumAvgY2 = avgY2;
		
		wScoreLBS = 0.0;
		wScoreLFS = 0.0;
		wScoreLLS = 0.0;
		wScoreLRS = 0.0;
		
		wScoreLBS =  calcWScoreLBS (avgX1, avgY1, avgZ1,avgX2, avgY2, avgZ2);
		wScoreLFS =  calcWScoreLFS(avgX1, avgY1, avgZ1,avgX2, avgY2, avgZ2);
		wScoreLLS =  calcWScoreLLS (avgX1, avgY1, avgZ1,avgX2, avgY2, avgZ2);
		wScoreLRS =  calcWScoreLRS (avgX1, avgY1, avgZ1,avgX2, avgY2, avgZ2);
		
		
		wScoreSIT = 0.0;
		wScoreSIT = calcWScoreSIT(avgX1, avgY1, avgZ1,avgX2, avgY2, avgZ2);
		wScoreSTAND = 0.0;
		wScoreSTAND =  calcWScoreSTAND(avgX1, avgY1, avgZ1,avgX2, avgY2, avgZ2);
		wScoreBEND = 0.0;
		wScoreBEND = calcWScoreBEND(avgX1, avgY1, avgZ1,avgX2, avgY2, avgZ2);
		
		if ((wScoreSTAND > wScoreLFS)&&(wScoreSTAND > wScoreLLS ) && (wScoreSTAND> wScoreLRS) && ( wScoreSTAND > wScoreLBS)
				&& (wScoreSTAND > wScoreSIT) && (wScoreSTAND> wScoreBEND)){
			nowPosture = "STAND";
		}
		else if((wScoreBEND > wScoreLFS)&&(wScoreBEND > wScoreLLS ) && (wScoreBEND> wScoreLRS) && ( wScoreBEND > wScoreLBS)
				&& (wScoreBEND > wScoreSTAND) && (wScoreBEND > wScoreSIT)){
			nowPosture = "BEND";
		}
		else if((wScoreSIT > wScoreLFS)&&(wScoreSIT > wScoreLLS ) && (wScoreSIT> wScoreLRS) && ( wScoreSIT > wScoreLBS)
				&& (wScoreSIT > wScoreSTAND) && (wScoreSIT> wScoreBEND)){
			nowPosture = "SIT";
		}
		else if ((wScoreLBS > wScoreLFS)&&(wScoreLBS > wScoreLLS ) && (wScoreLBS> wScoreLRS) && (wScoreLBS> wScoreSIT)
				&& (wScoreLBS > wScoreSTAND) && (wScoreLBS> wScoreBEND)){
			nowPosture = "LIEBACK";
		}
		else if ((wScoreLFS > wScoreLBS)&&(wScoreLFS> wScoreLLS ) && (wScoreLFS > wScoreLRS) && (wScoreLFS > wScoreSIT)
				&& (wScoreLFS > wScoreSTAND) && (wScoreLFS> wScoreBEND)){
			nowPosture = "LIEFRONT";
		}
		else if ((wScoreLRS > wScoreLFS)&&(wScoreLRS > wScoreLLS ) && (wScoreLRS> wScoreLBS) && (wScoreLRS > wScoreSIT)
				&& (wScoreLRS > wScoreSTAND)&& (wScoreLRS> wScoreBEND)){
			nowPosture = "LIERIGHT";
		}
		else if ((wScoreLLS > wScoreLFS)&&(wScoreLLS > wScoreLRS ) && (wScoreLLS> wScoreLBS) && (wScoreLLS > wScoreSIT)
				&& (wScoreLLS > wScoreSTAND) && (wScoreLLS> wScoreBEND)){
			nowPosture = "LIELEFT";
		}
		
		
		
		if (!nowPosture.equals(changePosture)){
			changePosture = nowPosture;
		}
		
		if (nowPosture.equals(changePosture))
		{
			counter++;
		}
		
		
		if (counter == threshold_counterPosture){
			newPosture = changePosture;
			counter =0;
		}
		
		if (!newPosture.equals(postureState)){
			// Where data is sent to posture class
		
			Log.e("PostureService", newPosture);
			postureState = newPosture;

			Intent i = new Intent("POSTURE_EVENT");
			i.putExtra("POSTURE", newPosture);
			sendBroadcast(i);
			
			Handler h = new Handler(Looper.getMainLooper());
			h.post(new Runnable(){
				@Override
				public void run(){
					//Log.i(DEBUG, "Connection successful, Getting Services");
					Toast.makeText( PostureService.this, postureState, Toast.LENGTH_SHORT).show();
					
				}
			});
		}
		else{
			//Log.e("PostureService", newPosture);
		}
	}// End of calculate Posture
	
	
	private Runnable resendPosture = new Runnable() {
		   @Override
		   public void run() {
			 
			  Intent i = new Intent("POSTURE_EVENT");
			  i.putExtra("POSTURE", newPosture);
			  sendBroadcast(i);
			 
			  
			  Handler h = new Handler(Looper.getMainLooper()); //handler to delay the scan, if can't connect, then stop attempts to scan
			  h.postDelayed(this, 1000);
			  
		   }};
		   
	//----------------------------------------------- 
	//  lie down fuzzy logic
	//----------------------------------------------- 	   
	private String lieDownPosture(double X, double Y, double Z){
		String liePosture = "LIE";
		
		
		// X axis
		if ( X <= -0.8){
			probRight = 1.0;
		}
		else if( (X < -0.75) && (X > -0.8)){
			probRight = -4.0*X - 3.0;
		}
		else if ( (X>= 0.75) && (X<0.8)){
			probLeft = 4.0 * X - 3.0;
		}
		else if (X>= 0.8){ // X> 0.8
			probLeft = 1.0;
		}
		
		
		if( (X >= -0.8) && (X <-0.6) ){
			probFront = 5.0 * X + 4.0;
			probBack = probFront;		
		}
		else if ((X>= -0.6) && (X<= 0.6)){
			probFront = 1.0;
			probBack = 1.0;
		}
		else if ((X> 0.6) && (X<=  0.8)){
			probFront = -5.0 * X + 4.0;
			probBack = probFront;
		}
		
		
		//Z-AXIS
		if ( Z <= -0.9){
			probBack += 1.0;
		}
		else if( (Z < -0.5) && (Z > -0.9)){
			probBack = -2.5 * Z -  1.25 + probBack;
		}
		else if ( (Z>= 0.5) && (Z<0.9)){
			probFront = 2.5 * Z - 1.25 + probFront;
		}
		else if (Z>= 0.9){
			probFront += 1.0;
		}
		
		
		if( (Z >= -0.9) && (Z <-0.5) ){
			probRight = 2.5 * Z + 2.25 + probRight;	
			probLeft = 2.5 * Z + 2.25 + probLeft;
		}
		else if ((Z>= -0.5) && (Z<= 0.5)){
			probRight += 1.0;
			probLeft += 1.0;
		}
		else if ((Z > 0.5) && (Z <0.9) ){
			probRight = -2.5 * Z + 2.25 + probRight;
			probLeft = -2.5 * Z + 2.25 + probLeft;
		}
		
		
		if ((probBack > probFront)&&(probBack > probLeft) && (probBack > probRight)){
			liePosture = "LIEBACK";
		}
		else if ((probFront > probBack)&&(probFront > probLeft) && (probFront > probRight)){
			liePosture = "LIEFRONT";
		}
		else if ((probRight > probFront)&&(probRight > probLeft) && (probRight > probBack)){
			liePosture = "LIERIGHT";
		}
		else{
			liePosture = "LIELEFT";
		}
		
		probBack = 0.0;
		probFront = 0.0;
		probRight = 0.0;
		probLeft = 0.0;
		return liePosture;
	}



	//----------------------------------------------- 
	//FUZZY LOGIC FUNCTIONS
	//----------------------------------------------- 
	private double calcWScoreLBS(double X1, double Y1, double Z1,double X2, double Y2, double Z2){
		double wScoreLBS;
		wScoreLBS = 0;
		
		//SENSORTAG ON CHEST
		// X axis
		if( (X1 >= -0.8) && (X1 <-0.6) ){
			wScoreLBS = 5.0 * X1 + 4.0;	
		}
		else if ((X1>= -0.6) && (X1<= 0.6)){
			wScoreLBS = 1.0;
		}
		else if ((X1> 0.6) && (X1<=  0.8)){
			wScoreLBS = -5.0 * X1 + 4.0;
		}
		//Y-AXIS
		if( (Y1 >= -0.1) && (Y1 <-0.05) ){
			wScoreLBS = 20.0 * Y1 + 2  + wScoreLBS;		
		}
		else if ((Y1>= -0.05) && (Y1<= 0.05)){
			wScoreLBS += 1.0;
		}
		else if ((Y1> 0.05) && (Y1<=  0.1)){
			wScoreLBS = -20.0 * Y1 + 2  + wScoreLBS;
		}
		//Z-AXIS
		if ( Z1 <= -0.9){
			wScoreLBS += 1.0;
		}
		else if( (Z1 < -0.5) && (Z1 > -0.9)){
			wScoreLBS = -2.5 * Z1 -  1.25 + wScoreLBS;
		}
		
		//SENSORTAG ON THIGH
		// X axis
		if( (X2 >= -0.8) && (X2 <-0.6) ){
			wScoreLBS = 5.0 * X2 + 4.0 + wScoreLBS;	
		}
		else if ((X2>= -0.6) && (X2<= 0.6)){
			wScoreLBS += 1.0;
		}
		else if ((X2> 0.6) && (X2<=  0.8)){
			wScoreLBS = -5.0 * X2 + 4.0 + wScoreLBS;
		}
		
		//Y-AXIS
		if( (Y2 >= -0.1) && (Y2 <-0.05) ){
			wScoreLBS = 20.0 * Y2 + 2  + wScoreLBS;		
		}
		else if ((Y2>= -0.05) && (Y2<= 0.05)){
			wScoreLBS += 1.0;
		}
		else if ((Y2> 0.05) && (Y2<=  0.1)){
			wScoreLBS = -20.0 * Y2 + 2  + wScoreLBS;
		}
		   
		   
		//Z-AXIS
		if ( Z2 <= -0.9){
			wScoreLBS += 1.0;
		}
		else if( (Z2 < -0.5) && (Z2 > -0.9)){
			wScoreLBS = -2.5 * Z2 -  1.25 + wScoreLBS;
		}		
		
		return wScoreLBS;	
	} 
	//**************** end of wScoreLBS

	private double calcWScoreLFS(double X1, double Y1, double Z1,double X2, double Y2, double Z2){
		double wScoreLFS;
		wScoreLFS = 0;
		
		// !!! sensorTag on chest
		// X axis
		if( (X1 >= -0.8) && (X1 <-0.6) ){
			wScoreLFS = 5.0 * X1 + 4.0;		
		}
		else if ((X1>= -0.6) && (X1<= 0.6)){
			wScoreLFS = 1.0;
		}
		else if ((X1> 0.6) && (X1<=  0.8)){
			wScoreLFS = -5.0 * X1 + 4.0;
		}
		
		//Y-AXIS
	   if( (Y1 >= -0.1) && (Y1 <-0.05) ){
			wScoreLFS = 20.0 * Y1 + 2  + wScoreLFS;		
		}
		else if ((Y1>= -0.05) && (Y1<= 0.05)){
			wScoreLFS += 1.0;
		}
		else if ((Y1> 0.05) && (Y1<=  0.1)){
			wScoreLFS = -20.0 * Y1 + 2  + wScoreLFS;
		}
		
		//Z-AXIS
		if ( (Z1>= 0.5) && (Z1<0.9)){
			wScoreLFS = 2.5 * Z1 - 1.25 + wScoreLFS;
		}
		else if (Z1>= 0.9){
			wScoreLFS += 1.0;
		}
		
		//!!! sensorTag on thigh
		// X axis
		if( (X2 >= -0.8) && (X2 <-0.6) ){
			wScoreLFS = 5.0 * X2 + 4.0 + wScoreLFS;		
		}
		else if ((X2>= -0.6) && (X2<= 0.6)){
			wScoreLFS += 1.0;
		}
		else if ((X2> 0.6) && (X2<=  0.8)){
			wScoreLFS = -5.0 * X2 + 4.0 +wScoreLFS;
		}
		
		//Y-AXIS
	   if( (Y2 >= -0.1) && (Y2 <-0.05) ){
			wScoreLFS = 20.0 * Y2 + 2  + wScoreLFS;		
		}
		else if ((Y2>= -0.05) && (Y2<= 0.05)){
			wScoreLFS += 1.0;
		}
		else if ((Y2> 0.05) && (Y2<=  0.1)){
			wScoreLFS = -20.0 * Y2 + 2  + wScoreLFS;
		}
		
		//Z-AXIS
		if ( (Z2>= 0.5) && (Z2<0.9)){
			wScoreLFS = 2.5 * Z2 - 1.25 + wScoreLFS;
		}
		else if (Z2>= 0.9){
			wScoreLFS += 1.0;
		}	
		return wScoreLFS;	
	}//**************** end of wScoreLFS

	
	private double calcWScoreLRS(double X1, double Y1, double Z1,double X2, double Y2, double Z2){
		double wScoreLRS;
		wScoreLRS = 0;
		
		//SENSORTAG ON CHEST
		// X axis
		if ( X1 <= -0.8){
			wScoreLRS = 1.0;
		}
		else if( (X1 < -0.75) && (X1 > -0.8)){
			wScoreLRS = -4.0*X1 - 3.0;
		}
		
		//Y-AXIS
		if( (Y1 >= -0.1) && (Y1 <-0.05) ){
			wScoreLRS = 20.0 * Y1 + 2  + wScoreLRS;		
		}
		else if ((Y1>= -0.05) && (Y1<= 0.05)){
			wScoreLRS += 1.0;
		}
		else if ((Y1> 0.05) && (Y1<=  0.1)){
			wScoreLRS = -20.0 * Y1 + 2  + wScoreLRS;
		}
		
		//Z-AXIS
		if( (Z1 >= -0.9) && (Z1 <-0.5) ){
			wScoreLRS = 2.5 * Z1 + 2.25 + wScoreLRS;	
		}
		else if ((Z1>= -0.5) && (Z1<= 0.5)){
			wScoreLRS += 1.0;
		}
		else if ((Z1 > 0.5) && (Z1 <0.9) ){
			wScoreLRS = -2.5 * Z1 + 2.25 + wScoreLRS;
		}
		

		//SENSORTAG ON THIGH
		// X axis
		if ( X2 <= -0.8){
			wScoreLRS += 1.0;
		}
		else if( (X2 < -0.75) && (X2 > -0.8)){
			wScoreLRS = -4.0*X2 - 3.0 + wScoreLRS;
		}
		
		//Y-AXIS
		if( (Y2 >= -0.1) && (Y2 <-0.05) ){
			wScoreLRS = 20.0 * Y2 + 2  + wScoreLRS;		
		}
		else if ((Y2>= -0.05) && (Y2<= 0.05)){
			wScoreLRS += 1.0;
		}
		else if ((Y2> 0.05) && (Y2<=  0.1)){
			wScoreLRS = -20.0 * Y2 + 2  + wScoreLRS;
		}
		
		//Z-AXIS
		if( (Z2 >= -0.9) && (Z2 <-0.5) ){
			wScoreLRS = 2.5 * Z2 + 2.25 + wScoreLRS;	
		}
		else if ((Z2>= -0.5) && (Z2<= 0.5)){
			wScoreLRS += 1.0;
		}
		else if ((Z2 > 0.5) && (Z2 <0.9) ){
			wScoreLRS = -2.5 * Z2 + 2.25 + wScoreLRS;
		}		
		
		
		return wScoreLRS;	
	}//**************** end of wScoreLRS

	
	
	
	private double calcWScoreLLS(double X1, double Y1, double Z1,double X2, double Y2, double Z2){
		double wScoreLLS;
		wScoreLLS = 0;
		
		//SENSORTAG ON CHEST
		// X axis
		if ( (X1>= 0.75) && (X1<0.8)){
			wScoreLLS = 4.0 * X1 - 3.0;
		}
		else if (X1>= 0.8){ // X> 0.8
			wScoreLLS = 1.0;
		}
		
		//Y-AXIS
		if( (Y1 >= -0.1) && (Y1 <-0.05) ){
			wScoreLLS = 20.0 * Y1 + 2  + wScoreLLS;		
		}
		else if ((Y1>= -0.05) && (Y1<= 0.05)){
			wScoreLLS += 1.0;
		}
		else if ((Y1> 0.05) && (Y1<=  0.1)){
			wScoreLLS = -20.0 * Y1 + 2  + wScoreLLS;
		}
		
		//Z-AXIS
		if( (Z1 >= -0.9) && (Z1 <-0.5) ){
			wScoreLLS = 2.5 * Z1 + 2.25 + wScoreLLS;
		}
		else if ((Z1>= -0.5) && (Z1<= 0.5)){
			wScoreLLS += 1.0;
		}
		else if ((Z1 > 0.5) && (Z1 <0.9) ){
			wScoreLLS = -2.5 * Z1 + 2.25 + wScoreLLS;
		}
		
		//SENSORTAG ON THIGH
		// X axis
		if ( (X2>= 0.75) && (X2<0.8)){
			wScoreLLS = 4.0 * X2 - 3.0 +wScoreLLS;
		}
		else if (X2>= 0.8){ // X> 0.8
			wScoreLLS += 1.0;
		}
		
		//Y-AXIS
		if( (Y2 >= -0.1) && (Y2 <-0.05) ){
			wScoreLLS = 20.0 * Y2 + 2  + wScoreLLS;		
		}
		else if ((Y2>= -0.05) && (Y2<= 0.05)){
			wScoreLLS += 1.0;
		}
		else if ((Y2> 0.05) && (Y2<=  0.1)){
			wScoreLLS = -20.0 * Y2 + 2  + wScoreLLS;
		}
		
		//Z-AXIS
		if( (Z2 >= -0.9) && (Z2 <-0.5) ){
			wScoreLLS = 2.5 * Z2 + 2.25 + wScoreLLS;
		}
		else if ((Z2>= -0.5) && (Z2<= 0.5)){
			wScoreLLS += 1.0;
		}
		else if ((Z2 > 0.5) && (Z2 <0.9) ){
			wScoreLLS = -2.5 * Z2 + 2.25 + wScoreLLS;
		}
		
		
		return wScoreLLS;	
	}//**************** end of wScoreLLS
	
	private double calcWScoreSIT(double X1, double Y1, double Z1,double X2, double Y2, double Z2){
		double wScoreSIT;
		wScoreSIT = 0;
		
		//SENSOR TAG ON CHEST:
		// X axis
		if ( (X1>= -0.2) && (X1<-0.1)){
			wScoreSIT = 10.0 * X1 + 2.0;
		}
		else if ( (X1>= -0.1) && (X1<=0.1)){ // X> 0.8
			wScoreSIT = 1.0;
		}
		else if ( (X1> 0.1) && (X1<=0.2)){ // X> 0.8
			wScoreSIT = -10.0 * X1 + 2.0;
		}
		//Y-AXIS
		if( (Y1 > -0.8) && (Y1 <= -0.7) ){
			wScoreSIT = -10.0 * Y1 - 7  + wScoreSIT;		
		}
		else if (Y1<= -0.8){
			wScoreSIT += 1.0;
		}
		//Z-AXIS
		if( (Z1 >= -0.6) && (Z1 <-0.5) ){
			wScoreSIT = 10 * Z1 + 6 + wScoreSIT;
		}
		else if ((Z1>= -0.5) && (Z1<= 0.3)){
			wScoreSIT += 1.0;
		}
		else if ((Z1 > 0.3) && (Z1 <= 0.4) ){
			wScoreSIT = -10.0 * Z1 + 4 + wScoreSIT;
		}
		
		
		//SENSOR TAG ON THIGH
		// X axis
		if ( (X2>= -0.3) && (X2<-0.15)){
			wScoreSIT = 6.67 * X2 + 2.0 + wScoreSIT;
		}
		else if ( (X2>= -0.15) && (X1<=0.15)){ // X> 0.8
			wScoreSIT += 1.0;
		}
		else if ( (X2> 0.15) && (X2<=0.3)){ // X> 0.8
			wScoreSIT = -6.67 * X2 + 2.0 + wScoreSIT;
		}
		//Y-AXIS
		if( (Y2 >= -0.3) && (Y2 < -0.1) ){
			wScoreSIT = 5.0 * Y2 + 1.5  + wScoreSIT;		
		}
		else if ((Y2 >= -0.1) && (Y2 <= 0.1)){
			wScoreSIT += 1.0;
		}
		else if ((Y2 > 0.1) && (Y2 <= 0.3)){
			wScoreSIT = -5.0 * Y2 + 1.5  + wScoreSIT;	
		}
		//Z-AXIS
		if( (Z2 > -0.8) && (Z2 <=-0.75) ){
			wScoreSIT = -20.0 * Z1 - 15 + wScoreSIT;
		}
		else if ((Z2<= -0.8)){
			wScoreSIT += 1.0;
		}
	
		
		return wScoreSIT;	
	}//**************** end of wScoreSIT
	
	private double calcWScoreSTAND(double X1, double Y1, double Z1,double X2, double Y2, double Z2){
		double wScoreSTAND;
		wScoreSTAND = 0;
		
		//SENSOR TAG ON CHEST:
		// X axis
		if ( (X1>= -0.5) && (X1<-0.4)){
			wScoreSTAND = 10.0 * X1 + 5.0;
		}
		else if ( (X1>= -0.4) && (X1<=0.4)){ 
			wScoreSTAND = 1.0;
		}
		else if ( (X1> 0.4) && (X1<=0.5)){ 
			wScoreSTAND = -10.0 * X1 + 5.0;
		}
		//Y-AXIS
		if( (Y1 > -0.8) && (Y1 <= -0.7) ){
			wScoreSTAND = -10.0 * Y1 - 7  + wScoreSTAND;		
		}
		else if (Y1<= -0.8){
			wScoreSTAND += 1.0;
		}
		//Z-AXIS
		if( (Z1 >= -0.2) && (Z1 < -0.1) ){
			wScoreSTAND = 10.0 * Z1 + 2.0 + wScoreSTAND;
		}
		else if ((Z1 >= -0.1) && (Z1<= 0.4)){
			wScoreSTAND += 1.0;
		}
		else if ((Z1 > 0.4) && (Z1 <= 0.7) ){
			wScoreSTAND = -3.33 * Z1 + 2.33 + wScoreSTAND;
		}
		
		
		//SENSOR TAG ON THIGH
		// X axis
		if ( (X2>= -0.3) && (X2<-0.15)){
			wScoreSTAND = 6.67 * X2 + 2.0 + wScoreSTAND;
		}
		else if ( (X2>= -0.15) && (X1<=0.15)){ 
			wScoreSTAND += 1.0;
		}
		else if ( (X2> 0.15) && (X2<=0.3)){ 
			wScoreSTAND = -6.67 * X2 + 2.0 + wScoreSTAND;
		}
		//Y-AXIS
		if( (Y2 > -0.8) && (Y2 <= -0.7) ){
			wScoreSTAND = -10.0 * Y2 - 7.0  + wScoreSTAND;		
		}
		else if ((Y2 <= -0.8) ){
			wScoreSTAND += 1.0;
		}
		//Z-AXIS
		if( (Z2 >= -0.4) && (Z2 < -0.3) ){
			wScoreSTAND = 10.0 * Z2 + 4.0 + wScoreSTAND;
		}
		else if ((Z2 >= -0.3) && (Z2 <= 0.3) ){
			wScoreSTAND += 1.0;
		}
		else if ((Z2 >  0.3) && (Z2 <= 0.4)){
			wScoreSTAND = -10.0 * Z2 + 4.0 + wScoreSTAND;
		}
	
		
		return wScoreSTAND;	
	}//**************** end of wScoreSTAND


	
	private double calcWScoreBEND(double X1, double Y1, double Z1,double X2, double Y2, double Z2){
		double wScoreBEND;
		wScoreBEND = 0;
		
		//SENSOR TAG ON CHEST:
		// X axis
		if ( (X1>= -0.5) && (X1<-0.4)){
			wScoreBEND = 10.0 * X1 + 5.0;
		}
		else if ( (X1>= -0.4) && (X1<=0.2)){ 
			wScoreBEND = 1.0;
		}
		else if ( (X1> 0.2) && (X1<=0.3)){ 
			wScoreBEND = -10.0 * X1 + 3.0;
		}
		//Y-AXIS
		if( (Y1 > -0.8) && (Y1 <= -0.7) ){
			wScoreBEND = -10.0 * Y1 - 7  + wScoreBEND;		
		}
		else if (Y1<= -0.8){
			wScoreBEND += 1.0;
		}
		//Z-AXIS
		if( (Z1 >= 0.5) && (Z1 < 0.9) ){
			wScoreBEND = 2.5 * Z1 - 1.25 + wScoreBEND;
		}
		else if ((Z1 >= 0.9)){
			wScoreBEND += 1.0;
		}
		
		
		
		//SENSOR TAG ON THIGH
		// X axis
		if ( (X2>= -0.3) && (X2<-0.15)){
			wScoreBEND = 6.67 * X2 + 2.0 + wScoreBEND;
		}
		else if ( (X2>= -0.15) && (X1<=0.15)){ 
			wScoreBEND += 1.0;
		}
		else if ( (X2> 0.15) && (X2<=0.3)){ 
			wScoreBEND = -6.67 * X2 + 2.0 + wScoreBEND;
		}
		//Y-AXIS
		if( (Y2 > -0.8) && (Y2 <= -0.7) ){
			wScoreBEND = -10.0 * Y2 - 7.0  + wScoreBEND;		
		}
		else if ((Y2 <= -0.8) ){
			wScoreBEND += 1.0;
		}
		//Z-AXIS
		if( (Z2 >= -0.4) && (Z2 < -0.3) ){
			wScoreBEND = 10.0 * Z2 + 4.0 + wScoreBEND;
		}
		else if ((Z2 >= -0.3) && (Z2 <= 0.3) ){
			wScoreBEND += 1.0;
		}
		else if ((Z2 >  0.3) && (Z2 <= 0.4)){
			wScoreBEND = -10.0 * Z2 + 4.0 + wScoreBEND;
		}
	
		
		return wScoreBEND;	
	}//**************** end of wScoreSTAND
}
