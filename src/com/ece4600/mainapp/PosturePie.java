package com.ece4600.mainapp;

import java.text.NumberFormat;
import java.util.Calendar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public class PosturePie {
	/*
	 * P1 = Standing
	 * P2 = Bending
	 * P3 = Sitting
	 * P4 = Lying Down
	 * 
	 * Colors from: http://www.color-hex.com/
	 */
	public static final int COLOR_P1 = Color.parseColor("#01AEBF"); // blue
	public static final int COLOR_P2 = Color.parseColor("#F83A3A"); // Magenta 
	public static final int COLOR_P3 = Color.parseColor("#99CC00"); // Green
	public static final int COLOR_P4 = Color.parseColor("#ffd700"); // Yellow
	
	public static final String strP1 = "Standing";
	public static final String strP2 = "Bending";
	public static final String strP3 = "Sitting";
	public static final String strP4 = "Laying";
	private static final int[] margins = {10, 10, 10, 10};
	public static double timeP1, timeP2, timeP3, timeP4,totalTime;
	
	
	private CategorySeries mSeries = new CategorySeries("");
	private DefaultRenderer mRenderer = new DefaultRenderer();
	private GraphicalView mChartView;
	
	public SharedPreferences postureSettings;
	public SharedPreferences.Editor editor;
	
	public void initialize(){
		timeP1= 25;
		timeP2= 25;
		timeP3= 25;
		timeP4= 25;
		
		totalTime = timeP1 + timeP2 + timeP3 + timeP4;
		
		mRenderer.setLabelsTextSize(10);
		mRenderer.setLegendTextSize(20);
		mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setMargins(margins);
		mRenderer.setDisplayValues(true);
		mRenderer.setZoomEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setInScroll(false);
		mRenderer.setFitLegend(true);
		//mRenderer.setScale(0.8f);
		
		mRenderer.setZoomButtonsVisible(false); 
	}

	public void updateData(double time1, double time2,  double time3, double time4){
		mSeries.clear();
		mRenderer.removeAllRenderers();
		
		
		timeP1 = time1;
		timeP2 = time2;
		timeP3 = time3;
		timeP4 = time4;
		
		totalTime = timeP1 + timeP2 + timeP3 + timeP4;
		
		mSeries.add(strP1, timeP1/totalTime);
		SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
		renderer.setColor(COLOR_P1);
		renderer.setChartValuesFormat(NumberFormat.getPercentInstance());
		mRenderer.addSeriesRenderer(renderer);
		
		
		
		mSeries.add(strP2, timeP2/totalTime);
		SimpleSeriesRenderer renderer2 = new SimpleSeriesRenderer();
		renderer2.setColor(COLOR_P2);
		renderer2.setChartValuesFormat(NumberFormat.getPercentInstance());
		mRenderer.addSeriesRenderer(renderer2);
		
		mSeries.add(strP3, timeP3/totalTime);
		SimpleSeriesRenderer renderer3 = new SimpleSeriesRenderer();
		renderer3.setChartValuesFormat(NumberFormat.getPercentInstance());
		renderer3.setColor(COLOR_P3);
		mRenderer.addSeriesRenderer(renderer3);
		
		mSeries.add(strP4, timeP4/totalTime);
		SimpleSeriesRenderer renderer4 = new SimpleSeriesRenderer();
		renderer4.setChartValuesFormat(NumberFormat.getPercentInstance());
		renderer4.setColor(COLOR_P4);
		mRenderer.addSeriesRenderer(renderer4);
	
	}
	
	public GraphicalView getView(Context context){
		mChartView = ChartFactory.getPieChartView(context,mSeries, mRenderer);
		return mChartView;
	}
	
	public void rePaint(){
		mChartView.repaint();
	}
	

}
