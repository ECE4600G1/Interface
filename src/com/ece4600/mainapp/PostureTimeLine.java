package com.ece4600.mainapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.widget.LinearLayout;

public class PostureTimeLine extends Activity {
	private SharedPreferences postureSettings;
	private SharedPreferences.Editor editor;
	private static Context context;
	private GraphicalView mChartView;
	private String fileName, filePath;
	private final String PATH = Environment.getExternalStorageDirectory() + "/wellNode/Posture";
	private Time now = new Time();

	private TimeSeries series = new TimeSeries("Line");
	private XYSeriesRenderer renderer = new XYSeriesRenderer();
	
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); 
	
	private int numFile;
	private String userName;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_posturetimeline);
	    setUpGraphs();
	    setUpPreferences();
	    readFile();
	    context = getBaseContext();
	    paintGraph();
	    // TODO Auto-generated method stub
	}
	
	public void readFile(){
		double x, xLast;
		int m = 0;
		int num, lines, n;
		n = 0;
		num = 0;
		filePath = PATH + "/" + fileName + ".csv";
		File file = new File(filePath);
		
		while(num < numFile){
			if (file.exists()){
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
				
				series.add(x, Double.valueOf(data[2]));
				x = x + Double.valueOf(data[1]);
				if( (x - xLast) >= 1000){
					xLast = x;
					mRenderer.addXTextLabel(x,data[0]);
				}else{
					mRenderer.addXTextLabel(x,"");
				}
				
				//dataLine.addPoint(m, Double.parseDouble(data[0]));
				
				
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
			}
		num++;
		
		filePath = PATH + "/" + fileName + "(" + num + ")" + ".csv";
		file = new File(filePath);
		Log.e("timeline", filePath);
		}
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
    	
		postureSettings = getSharedPreferences("posturePrefs", MODE_PRIVATE);
		editor = postureSettings.edit();
		
		now.setToNow();
     	date = now.format("%m-%d-%Y");
		
	    fileName = userName + " Posture " + date;
	    numFile = postureSettings.getInt("numFile", 1);
	}
	
	
}
