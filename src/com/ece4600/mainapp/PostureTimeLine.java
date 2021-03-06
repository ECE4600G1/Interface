package com.ece4600.mainapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class PostureTimeLine extends Activity {
	private SharedPreferences postureSettings;
	private SharedPreferences.Editor editor;
	private static Context context;
	private GraphicalView mChartView;
	private String fileName, filePath, currentFile;
	private final String PATH = Environment.getExternalStorageDirectory() + "/wellNode/Posture";
	private Time now = new Time();

	private TimeSeries series = new TimeSeries("Line");
	private XYSeriesRenderer renderer = new XYSeriesRenderer();
	
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
	
	private int numFile, indexFile, m;
	private String userName;
	
	Button increaseBut, decreaseBut, returnBut;
	TextView numFileStr;
	
	
	private int[] count = new int[100];
	private String[] arraySpinner;
	
	Spinner spinner;
	private ArrayAdapter<String> dataAdapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_posturetimeline);
	    setUpGraphs();
	    setUpPreferences();
	    context = getBaseContext();
	    
	    indexFile = 0;
	    
	   // increaseBut = (Button)findViewById(R.id.increase);
	    //decreaseBut  = (Button)findViewById(R.id.decrease);
	    returnBut = (Button)findViewById(R.id.timeLineReturn);
	    
	    //numFileStr = (TextView)findViewById(R.id.numFile);
	    
	    
	  
	    
	    
	    initButtons();
	    
	    
	    spinner = (Spinner) findViewById(R.id.spinner1);
	    dataAdapter = new ArrayAdapter<String>(this,
	      android.R.layout.simple_spinner_item, new ArrayList<String>());
	    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(dataAdapter);
	    
	    checkFiles();
	    
	   /* filePath = PATH + "/" + spinner.getItemAtPosition(0).toString();
	    File file = new File(filePath);
	    if (file.exists()){
	    readFile();
	    paintGraph();
	    }else{
	    	mDataset.addSeries(series);
	    	paintGraph();
	    }*/
	    
	    
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

	        public void onItemSelected(AdapterView<?> parent, View arg1,
	                int pos, long arg3) {
	        	
	        	seeNextPlot();
	        	
	            String Text = parent.getSelectedItem().toString();
	            
	            filePath = PATH + "/" + spinner.getItemAtPosition(pos).toString();
	            
	            //filePath = PATH + "/" + Text.toString();
	            
	    	    File file = new File(filePath);
	    	    if (file.exists()){
	    	    readFile();
	    	    paintGraph();
	    	    }else{
	    	    	mDataset.addSeries(series);
	    	    	paintGraph();
	    	    }
	              }

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
	          }
	);
	    
	}
	
	public void readFile(){
		double x, xLast;
		int m = 0;
		int num, lines, n;
		n = 0;
		num = 0;
		//filePath = PATH + "/" + fileName + ".csv";
		//File file = new File(filePath);
		//series.clear();
		//mRenderer.removeAllRenderers();
		//setUpGraphs();
		//while(num < (numFile-1)){
			//if (file.exists()){
		BufferedReader br = null;
		String line = "";
		
		x = 0;
		xLast = 0;
		try {
			 
			br = new BufferedReader(new FileReader(filePath));
			
			
			LineNumberReader  lnr = new LineNumberReader(new FileReader(filePath));
			lnr.skip(Long.MAX_VALUE);
			lines = lnr.getLineNumber() + 1;
			// Finally, the LineNumberReader object should be closed to prevent resource leak
			lnr.close();
			
			
			n = 0;
			//Skip headers
			for (int i =0; i <9; i++)
				line = br.readLine(); 
			
			//Output the important plot data
			while (((line = br.readLine()) != null) & (n < (lines - 11)) ){
				String[] data = line.split(",");
				
				//Log.i("TEST",data[1] +"," + data[2]);
				try{
				series.add(x, Double.valueOf(data[2]));
				x = x + Double.valueOf(data[1]);
				if( (x - xLast) >= 1000){
					xLast = x;
					mRenderer.addXTextLabel(x,data[0]);
				}else{
					mRenderer.addXTextLabel(x,"");
				}
				
				//dataLine.addPoint(m, Double.parseDouble(data[0]));
				
				} catch (NumberFormatException e){
					Log.e("timeline", "FATAL NUMBER ERROr");
				}
				
				n++;
			
				m++;
				
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
			//}
		//num++;
		
		//file = new File(filePath);
		Log.e("timeline", filePath);
		//}
		mDataset.addSeries(series);
		
		
		
	}
	
	public void setUpGraphs(){
		// Customization time for only a single line 1!
				renderer.setColor(Color.BLUE);
				renderer.setPointStyle(PointStyle.CIRCLE);
				renderer.setFillPoints(true);
				renderer.setLineWidth(5f);
				
				//CHARACTERISITCS FOR ALL LINES		
				// Unable Zoom
				mRenderer.setApplyBackgroundColor(true);
				mRenderer.setBackgroundColor(Color.BLACK);
				mRenderer.setBarSpacing(10);
				mRenderer.setAxesColor(Color.GRAY);
				mRenderer.setZoomButtonsVisible(false);
				mRenderer.setZoomEnabled(true);
				//mRenderer.setMargins(margins);
				mRenderer.setChartTitle("");
				mRenderer.setChartTitleTextSize(35);
				mRenderer.setXTitle("Timeline");
				mRenderer.setYTitle("Posture");
				mRenderer.setPointSize(1);
				mRenderer.setShowGrid(true);
				mRenderer.setXLabels(0);		
				mRenderer.setAxisTitleTextSize(20);
				mRenderer.addSeriesRenderer(renderer);	
				mRenderer.setLabelsTextSize(15);
				mRenderer.setLegendTextSize(25);
				mRenderer.setGridColor(Color.WHITE);
				mRenderer.setXLabelsAngle(-45);
				//mRenderer.setYAxisMax(15);
				//mRenderer.setYAxisMin(4);
				mRenderer.setShowLabels(true);
				mRenderer.setShowLegend(false);
				mRenderer.setYAxisMax(7.25);
				mRenderer.setYAxisMin(-0.25);
			
				//mRenderer.setXAxisMax(windowSize);
				//mRenderer.setXAxisMin(0);
				//mRenderer.set
	}
	
	
	public void paintGraph(){
		GraphicalView lineView = ChartFactory.getLineChartView(getBaseContext(), mDataset, mRenderer);
		//Get reference to layout:
		LinearLayout layout =(LinearLayout)findViewById(R.id.chart);
		//clear the previous layout:
		layout.removeAllViews();
		//add new graph:
		layout.addView(lineView);
	}
	
	public void setUpPreferences(){
		String date;
		
		postureSettings = getSharedPreferences("userPrefs", MODE_PRIVATE);
    	editor = postureSettings.edit();
  
    	userName = postureSettings.getString("name", "Mike");
    	Log.d("name", userName);
		postureSettings = getSharedPreferences("posturePrefs", MODE_PRIVATE);
		editor = postureSettings.edit();
		
		now.setToNow();
     	date = now.format("%m-%d-%Y");
		
	    fileName = userName + " Posture " + date;
	    numFile = postureSettings.getInt("numFile", 1);
	    
	    currentFile = postureSettings.getString("fileName", "") + ".csv";
	}
	
	
	public void initButtons(){
		/*increaseBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				
				
	    		indexFile++;
	    		filePath = PATH + "/" + fileName + "(" + indexFile + ")" + ".csv";
	    		File file = new File(filePath);
	    		if ((file.exists()) & (indexFile < numFile)){
	    			
	    			seeNextPlot();
	    		
	    			readFile();
	    			paintGraph();
	    			updatefileNumStr();
	    		}else{
	    			indexFile--;
	    		}
			}
		});	
		
		
		decreaseBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
	    		indexFile--;
	    		
	    		if(indexFile == 0){
	    			filePath = PATH + "/" + fileName + ".csv";
	    			File file = new File(filePath);
		    		if (file.exists()){
		    			seeNextPlot();
		    			readFile();
		    			paintGraph();
		    			updatefileNumStr();
		    		}else{
		    			indexFile--;
		    		}
	    		} else if (indexFile < 0){
	    			indexFile++;
	    		} else {
	    			filePath = PATH + "/" + fileName + "(" + indexFile + ")" + ".csv";
		    		File file = new File(filePath);
		    		if (file.exists()){
		    			seeNextPlot();
		    			readFile();
		    			paintGraph();
		    			updatefileNumStr();
		    		}else{
		    			indexFile++;
		    		}
	    		}
				
			}
		});	
		*/
		
		returnBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(PostureTimeLine.this, Posture.class));
				finish();
				
			}
		});	
		
		
		
	}
	
	
	public void updatefileNumStr(){
		
		if (indexFile < 10){
			numFileStr.setText("File index: 0" + String.valueOf(indexFile));
		} else if (indexFile > 10){
			numFileStr.setText("File index: " + String.valueOf(indexFile));
		}
		
	}
	
	public void seeNextPlot(){
		mDataset.removeSeries(series);
		mRenderer.removeAllRenderers();
		mRenderer.clearXTextLabels();	
		series.clear();
		setUpGraphs();
		
	}
	
	
	public void checkFiles(){
		String [] name = userName.split(" ");
		
		m = 0;
	    Log.d("Files", "Path: " + PATH);
	    File f = new File(PATH);        
	    File file2[] = f.listFiles();
	    Log.d("Files", "Size: "+ file2.length);
	    for (int i=0; i < file2.length; i++)
	    {
	    	Log.d("Files", "FileName:" + file2[i].getName());
	        String[] sep = file2[i].getName().split(" ");
	        Log.d("Files", sep[0]);
	        
	        if (sep[0].equals(name[0]) &&  !file2[i].getName().equals(currentFile)){
	        	dataAdapter.add(file2[i].getName());
	        	
	        }
	        
	    }
	    
	 
	    
	    
	}
	
}
