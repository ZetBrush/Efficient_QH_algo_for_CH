/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arman.efficientqhalgoforch;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.arman.efficientqhalgoforch.algo.GrahamScanParallel;
import com.arman.efficientqhalgoforch.algo.QuickHull;
import com.arman.efficientqhalgoforch.common.Point2DCloud;
import com.arman.efficientqhalgoforch.common.Utils;
import com.arman.efficientqhalgoforch.external.DoneListener;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

public class SuperAwesomeCardFragment extends Fragment implements OnChartValueSelectedListener {


	public static CopyOnWriteArrayList<DataHolder> mResults = new CopyOnWriteArrayList<>();
	public static CopyOnWriteArrayList<DataHolder> mGResults = new CopyOnWriteArrayList<>();
	public static int algorithmIndex = 0;
	private ScatterChart mChart;
	private TextView tvX, tvY;
	private Typeface tf;



	private static final String ARG_POSITION = "position";

	private int position;



	public static SuperAwesomeCardFragment newInstance(int position) {
		SuperAwesomeCardFragment f = new SuperAwesomeCardFragment();
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		position = getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (position == 2) {
			final View vv = inflater.inflate(R.layout.benchmark_lay, null);

			Button run = (Button) vv.findViewById(R.id.runAlgor);
			Button reset = (Button) vv.findViewById(R.id.cleardata);
			run.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText thrd;
					EditText points;
					EditText canvasY;
					EditText canvasX;
					thrd = (EditText) vv.findViewById(R.id.numberTreadstxt);
					points = (EditText) vv.findViewById(R.id.numberpoints);
					canvasX = (EditText) vv.findViewById(R.id.canvasWidth);
					canvasY = (EditText) vv.findViewById(R.id.canvasHeight);
					int threads = Integer.valueOf(thrd.getText().toString());
					int ponts = Integer.valueOf(points.getText().toString());
					int canvY = Integer.valueOf(canvasY.getText().toString());
					int canvX = Integer.valueOf(canvasX.getText().toString());

					if (threads < 1)
						threads = 1;
					if (ponts <= 2) ponts = 3;
					if (threads > ponts) threads = ponts - 1;
					if (threads > 25) threads = 25;
					if (canvX + 100 < ponts) canvX = canvX + 100;
					if (canvY + 100 < ponts) canvY = canvY + 100;

					final Point2DCloud point2DCloud = new Point2DCloud(getActivity(), ponts /* points */,
							Utils.WIDTH = canvY,
							Utils.HEIGHT = canvX, true);


					int animTime = 10;
					final int finalThreads = threads;
					final int finalPonts = ponts;
					QuickHull qh = new QuickHull(point2DCloud, threads, true, animTime, new DoneListener() {
						@Override
						public void jobDone(int id, float time) {
							mResults.add(new DataHolder(finalThreads, time, finalPonts));
							updateChart(mResults);

						}
					});
					qh.run();
					algorithmIndex = 2;


				}
			});

			reset.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (DataHolder hld : mResults) {
						mResults.remove(hld);
					}

					updateChart(mResults);

				}
			});
			doHardcore(vv, inflater);


			return vv;
		} else if (position == 3) {

			final View vv = inflater.inflate(R.layout.benchmark_lay, null);

			((TextView) vv.findViewById(R.id.nametxt)).setText("             GrahamScan\n Multithreaded Benchmark");


			Button run = (Button) vv.findViewById(R.id.runAlgor);
			Button resetgr = (Button) vv.findViewById(R.id.cleardata);
			run.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText thrd;
					EditText points;
					EditText canvasY;
					EditText canvasX;
					thrd = (EditText) vv.findViewById(R.id.numberTreadstxt);
					points = (EditText) vv.findViewById(R.id.numberpoints);
					canvasX = (EditText) vv.findViewById(R.id.canvasWidth);
					canvasY = (EditText) vv.findViewById(R.id.canvasHeight);
					int threads = Integer.valueOf(thrd.getText().toString());
					int ponts = Integer.valueOf(points.getText().toString());
					int canvY = Integer.valueOf(canvasY.getText().toString());
					int canvX = Integer.valueOf(canvasX.getText().toString());

					if (threads < 1)
						threads = 1;
					if (ponts <= 2) ponts = 3;
					if (threads > ponts) threads = ponts - 1;
					if (threads > 25) threads = 25;
					if (canvX + 100 < ponts) canvX = canvX + 100;
					if (canvY + 100 < ponts) canvY = canvY + 100;

					final Point2DCloud point2DCloud = new Point2DCloud(getActivity(), ponts /* points */,
							Utils.WIDTH = canvY,
							Utils.HEIGHT = canvX, true);


					int animTime = 10;
					final int finalThreads = threads;
					final int finalPonts = ponts;
					GrahamScanParallel gh = new GrahamScanParallel(point2DCloud, threads, true, animTime, new DoneListener() {
						@Override
						public void jobDone(int id, float time) {
							mGResults.add(new DataHolder(finalThreads, time, finalPonts));
							updateChart(mGResults);

						}
					});
					gh.run();
					algorithmIndex = 3;


				}
			});

			resetgr.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (DataHolder hld : mGResults) {
						mGResults.remove(hld);
					}
					updateChart(mGResults);
				}
			});
			doHardcore(vv, inflater);


			return vv;

		} else {

			if (position == 0) {


				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

				FrameLayout fl = new FrameLayout(getActivity());
				fl.setLayoutParams(params);

				final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
						.getDisplayMetrics());

				View v = (View) inflater.inflate(R.layout.intro, null);
				params.setMargins(margin, margin, margin, margin);
				v.setLayoutParams(params);
				v.setLayoutParams(params);
				//v.setBackgroundResource(R.drawable.background_card);


				fl.addView(v);
				//fl.addView(btn);
				return fl;
			} else if (position == 1) {
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

				FrameLayout fl = new FrameLayout(getActivity());
				fl.setLayoutParams(params);

				final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
						.getDisplayMetrics());

				View v = (View) inflater.inflate(R.layout.algo, null);
				params.setMargins(margin, margin, margin, margin);
				v.setLayoutParams(params);
				v.setLayoutParams(params);
				//v.setBackgroundResource(R.drawable.background_card);

				fl.addView(v);

				return fl;
			}

			}
		return null;
	}


	public void doHardcore(View v, final LayoutInflater inflater) {
		tvX = (TextView) v.findViewById(R.id.tvXMax);

		mChart = (ScatterChart) v.findViewById(R.id.chart1);
		mChart.setDescription("");

		tf = Typeface.createFromAsset((SuperAwesomeCardFragment.this).getActivity().getAssets(), "OpenSans-Regular.ttf");

		mChart.setOnChartValueSelectedListener(this);

		mChart.setDrawGridBackground(false);

		mChart.setTouchEnabled(true);
		mChart.setHighlightEnabled(true);

		// enable scaling and dragging
		mChart.setDragEnabled(true);
		mChart.setScaleEnabled(true);

		mChart.setMaxVisibleValueCount(200);
		mChart.setPinchZoom(true);



		Legend l = mChart.getLegend();
		l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
		l.setTypeface(tf);

		YAxis yl = mChart.getAxisLeft();
		yl.setTypeface(tf);

		mChart.getAxisRight().setEnabled(false);

		XAxis xl = mChart.getXAxis();
		xl.setTypeface(tf);
		xl.setDrawGridLines(false);

	}





	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.actionToggleValues: {
				for (DataSet<?> set : mChart.getData().getDataSets())
					set.setDrawValues(!set.isDrawValuesEnabled());

				mChart.invalidate();
				break;
			}
			case R.id.actionToggleHighlight: {
				if (mChart.isHighlightEnabled())
					mChart.setHighlightEnabled(false);
				else
					mChart.setHighlightEnabled(true);
				mChart.invalidate();
				break;
			}
			case R.id.actionTogglePinch: {
				if (mChart.isPinchZoomEnabled())
					mChart.setPinchZoom(false);
				else
					mChart.setPinchZoom(true);

				mChart.invalidate();
				break;
			}
			case R.id.actionToggleStartzero: {
				mChart.getAxisLeft().setStartAtZero(!mChart.getAxisLeft().isStartAtZeroEnabled());
				mChart.getAxisRight().setStartAtZero(!mChart.getAxisRight().isStartAtZeroEnabled());
				mChart.invalidate();
				break;
			}
			case R.id.actionToggleFilter: {

				Approximator a = new Approximator(Approximator.ApproximatorType.DOUGLAS_PEUCKER, 25);

				if (!mChart.isFilteringEnabled()) {
					mChart.enableFiltering(a);
				} else {
					mChart.disableFiltering();
				}
				mChart.invalidate();
				break;
			}
			case R.id.actionSave: {
				// mChart.saveToGallery("title"+System.currentTimeMillis());
				mChart.saveToPath("title" + System.currentTimeMillis(), "");
				break;
			}
			case R.id.animateX: {
				mChart.animateX(3000);
				break;
			}
			case R.id.animateY: {
				mChart.animateY(3000);
				break;
			}
			case R.id.animateXY: {

				mChart.animateXY(3000, 3000);
				break;
			}
		}
		return true;
	}


	public void updateChart(CopyOnWriteArrayList<DataHolder> holdder){


		ArrayList<Entry> yVals1 = new ArrayList<Entry>();
		ArrayList<String> xVals = new ArrayList<String>();
		for (int i=0; i< 25	;i++){
			xVals.add(i+"");
		}
		ArrayList<Entry> yValspoint = new ArrayList<Entry>();
		for (int i = 0; i < holdder.size(); i++) {

			yVals1.add(new Entry((holdder.get(i).getTime()), Integer.valueOf(xVals.get(xVals.indexOf( String.valueOf(holdder.get(i).getThread()))))));

		}

		ArrayList<ScatterDataSet> dataSets = new ArrayList<ScatterDataSet>();


		// create a dataset and give it a type
		ScatterDataSet set1 = new ScatterDataSet(yVals1, "Result");
		set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
		set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
		set1.setScatterShapeSize(8f);


		dataSets.add(set1); // add the datasets

		// create a data object with the datasets
		ScatterData data = new ScatterData(xVals, dataSets);
		data.setValueTypeface(tf);

		mChart.setData(data);
		mChart.invalidate();

	}



	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		Log.i("VAL SELECTED",
				"Value: " + e.getVal() + ", xIndex: " + e.getXIndex()
						+ ", DataSet index: " + dataSetIndex);
	}

	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub

	}



	/**
	 * Line
	 */

	public static float[] ItoFloat(LinkedList<Integer> d) {
		float[] floatArray = new float[d.size()+1];
		floatArray[0]=	0.f;
		for (int i = 1,j=0; i < floatArray.length; i++,j++) {
			floatArray[i] = (float) (d.get(j) / 1.0);
		}
		return floatArray;
	}

	public static float[] DtoFloat(LinkedList<Double> d) {
		float[] floatArray = new float[d.size()+1];
		for (int i = 0; i < floatArray.length; i++) {
			floatArray[i] = (float) (d.get(i) / 1.0);
		}
		return floatArray;
	}

	public static String[] DtoString(LinkedList<Double> d) {
		String[] s = new String[d.size()+1];
		s[0]="";
		for (int i = 1, j=0; i < s.length; i++,j++)
			s[i] = String.valueOf(d.get(j));

		return s;
	}

	private class DataHolder implements Comparable{
		int thread;
		float time;
		int points;

		public DataHolder(int thread, float time, int points) {
			this.thread = thread;
			this.time = time;
			this.points = points;
		}

		public int getThread() {
			return thread;
		}

		public void setThread(int thread) {
			this.thread = thread;
		}

		public float getTime() {
			return time;
		}

		public void setTime(float time) {
			this.time = time;
		}

		public int getPoints() {
			return points;
		}

		public void setPoints(int points) {
			this.points = points;
		}



		@Override
		public int compareTo(Object another) {
			if(((DataHolder)another).getPoints()>this.getPoints())
				return 1;
			else if(((DataHolder)another).getPoints()<this.getPoints())
			return -1;
			else return 0;
		}
	}
}
