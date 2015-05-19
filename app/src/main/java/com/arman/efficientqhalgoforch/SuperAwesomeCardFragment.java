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

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.DialogInterface;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arman.efficientqhalgoforch.algo.QuickHull;
import com.arman.efficientqhalgoforch.common.Point2DCloud;
import com.arman.efficientqhalgoforch.common.Utils;
import com.arman.efficientqhalgoforch.external.DoneListener;
import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.bounce.BounceEaseOut;
import com.db.chart.view.animation.easing.cubic.CubicEaseOut;
import com.db.chart.view.animation.easing.elastic.ElasticEaseOut;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;
import com.db.chart.view.animation.style.DashAnimation;

import java.text.DecimalFormat;
import java.util.LinkedList;

public class SuperAwesomeCardFragment extends Fragment {
	final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
	final TimeInterpolator exitInterpolator = new AccelerateInterpolator();
	public static LinkedList<Double> timeResultQuick = new LinkedList<>();
	public static LinkedList<Integer> threadCountQuick = new LinkedList<>();
	public static LinkedList<Double> timeResultGraham = new LinkedList<>();
	public static LinkedList<Integer> threadCountGraham = new LinkedList<>();
	public static int algorithmIndex = 0;


	/**
	 * Play
	 */
	static ImageButton mPlayBtn;

	/**
	 * Order
	 */
	private static ImageButton mOrderBtn;
	private final static int[] beginOrder = {0, 1, 2, 3, 4, 5, 6};
	private final static int[] middleOrder = {3, 2, 4, 1, 5, 0, 6};
	private final static int[] endOrder = {6, 5, 4, 3, 2, 1, 0};
	private static float mCurrOverlapFactor;
	private static int[] mCurrOverlapOrder;
	private static float mOldOverlapFactor;
	private static int[] mOldOverlapOrder;


	/**
	 * Ease
	 */
	private static ImageButton mEaseBtn;
	private static BaseEasingMethod mCurrEasing;
	private static BaseEasingMethod mOldEasing;


	/**
	 * Enter
	 */
	private static ImageButton mEnterBtn;
	private static float mCurrStartX;
	private static float mCurrStartY;
	private static float mOldStartX;
	private static float mOldStartY;


	/**
	 * Alpha
	 */
	private static ImageButton mAlphaBtn;
	private static int mCurrAlpha;
	private static int mOldAlpha;


	private Handler mHandler;

	private final Runnable mEnterEndAction = new Runnable() {
		@Override
		public void run() {
			mPlayBtn.setEnabled(true);
		}
	};

	private final Runnable mExitEndAction = new Runnable() {
		@Override
		public void run() {
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mOldOverlapFactor = mCurrOverlapFactor;
					mOldOverlapOrder = mCurrOverlapOrder;
					mOldEasing = mCurrEasing;
					mOldStartX = mCurrStartX;
					mOldStartY = mCurrStartY;
					mOldAlpha = mCurrAlpha;
					updateLineChart();

				}
			}, 500);
		}
	};

	private boolean mNewInstance;

	private final static int LINE_MAX = 25;
	private final static int LINE_MIN = 1;

	private  static String[] lineLabels = DtoString(timeResultQuick);
	private  static float[][] lineValues = {ItoFloat(threadCountQuick),
			ItoFloat(threadCountGraham)};
	private static LineChartView mLineChart;
	private Paint mLineGridPaint;
	private TextView mLineTooltip;


	private static final String ARG_POSITION = "position";

	private int position;

	public void initscores(){
		lineLabels = DtoString(timeResultQuick);
		lineValues = new float[][]{ItoFloat(threadCountQuick),ItoFloat(threadCountGraham)};
		}

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
			mNewInstance = false;
			mCurrOverlapFactor = 1;
			mCurrEasing = new QuintEaseOut();
			mCurrStartX = -1;
			mCurrStartY = 0;
			mCurrAlpha = -1;

			mOldOverlapFactor = 1;
			mOldEasing = new QuintEaseOut();
			mOldStartX = -1;
			mOldStartY = 0;
			mOldAlpha = -1;
			mHandler = new Handler();

			Button run = (Button) vv.findViewById(R.id.runAlgor);
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
					QuickHull qh = new QuickHull(point2DCloud, threads, true, animTime, new DoneListener() {
						@Override
						public void jobDone(int id) {
							initscores();
							updateValues(new LineChartView(getActivity()));
							updateLineChart();
						}
					});
					qh.run();
					algorithmIndex = 2;
					threadCountQuick.add(threads);
					threadCountGraham.add(threads);
				}
			});
			doHardcore(vv, inflater);


			return vv;
		} else if (position == 3) {

			View v = inflater.inflate(R.layout.benchmark_lay, null);
			mNewInstance = false;
			mCurrOverlapFactor = 1;
			mCurrEasing = new QuintEaseOut();
			mCurrStartX = -1;
			mCurrStartY = 0;
			mCurrAlpha = -1;

			mOldOverlapFactor = 1;
			mOldEasing = new QuintEaseOut();
			mOldStartX = -1;
			mOldStartY = 0;
			mOldAlpha = -1;
			mHandler = new Handler();
			((TextView) v.findViewById(R.id.nametxt)).setText("               GrahamScan\n Multithreaded Benchmark");


			algorithmIndex = 3;

			doHardcore(v, inflater);


			return v;


		} else {


			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			FrameLayout fl = new FrameLayout(getActivity());
			fl.setLayoutParams(params);

			final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources()
					.getDisplayMetrics());

			TextView v = new TextView(getActivity());
			params.setMargins(margin, margin, margin, margin);
			v.setLayoutParams(params);
			v.setLayoutParams(params);
			v.setGravity(Gravity.CENTER);
			v.setBackgroundResource(R.drawable.background_card);
			v.setText("CARD " + (position + 1));
			Button btn = new Button(getActivity());
			btn.setText("Run");

			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Point2DCloud point2DCloud = new Point2DCloud(getActivity(), 20 /* points */,
							Utils.WIDTH = 700,
							Utils.HEIGHT = 700, true);


					int animTime = 10;
					QuickHull qh = new QuickHull(point2DCloud, 6, true, animTime, new DoneListener() {
						@Override
						public void jobDone(int id) {

						}
					});
					qh.run();

				}


			});

			fl.addView(v);
			fl.addView(btn);
			return fl;
		}
	}

	public void doHardcore(View v, final LayoutInflater inflater) {

		final OnEntryClickListener lineEntryListener = new OnEntryClickListener() {
			@Override
			public void onClick(int setIndex, int entryIndex, Rect rect) {

				if (mLineTooltip == null)
					showLineTooltip(inflater, setIndex, entryIndex, rect);
				else
					dismissLineTooltip(inflater, setIndex, entryIndex, rect);
			}
		};

		final View.OnClickListener lineClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLineTooltip != null)
					dismissLineTooltip(inflater, -1, -1, null);
			}
		};
		initMenu(v);
		initLineChart(v, lineEntryListener, lineClickListener);
		updateLineChart();
	}


	/**
	 * Line
	 */



	private void initLineChart(View v, OnEntryClickListener lineEntryListener, View.OnClickListener lineClickListener) {

		mLineChart = (LineChartView) (v.findViewById(R.id.linechart));
		mLineChart.setOnEntryClickListener(lineEntryListener);
		mLineChart.setOnClickListener(lineClickListener);

		mLineGridPaint = new Paint();
		mLineGridPaint.setColor(this.getResources().getColor(R.color.line_grid));
		mLineGridPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
		mLineGridPaint.setStyle(Paint.Style.STROKE);
		mLineGridPaint.setAntiAlias(true);
		mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

	}


	private void updateLineChart() {

		mLineChart.reset();

		LineSet dataSet = new LineSet();
		dataSet.addPoints(lineLabels, lineValues[0]);
		dataSet.setDots(true)
				.setDotsColor(this.getResources().getColor(R.color.line_bg))
				.setDotsRadius(Tools.fromDpToPx(5))
				.setDotsStrokeThickness(Tools.fromDpToPx(2))
				.setDotsStrokeColor(this.getResources().getColor(R.color.line))
				.setLineColor(this.getResources().getColor(R.color.line))
				.setLineThickness(Tools.fromDpToPx(3))
				.beginAt(1).endAt(lineLabels.length - 1);
		mLineChart.addData(dataSet);

		dataSet = new LineSet();
		dataSet.addPoints(lineLabels, lineValues[1]);
		dataSet.setLineColor(this.getResources().getColor(R.color.line))
				.setLineThickness(Tools.fromDpToPx(3))
				.setSmooth(true)
				.setDashed(true);
		mLineChart.addData(dataSet);

		mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
				.setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
				.setXAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE)
				.setYAxis(false)
				.setYLabels(YController.LabelPosition.OUTSIDE)
				.setAxisBorderValues(LINE_MIN, LINE_MAX, 2)
				.setLabelsFormat(new DecimalFormat("##'u'"))
				.show(getAnimation(true).setEndAction(mEnterEndAction))
		//.show()
		;

		mLineChart.animateSet(1, new DashAnimation());
	}


	@SuppressLint("NewApi")
	private void showLineTooltip(LayoutInflater inflater, int setIndex, int entryIndex, Rect rect) {

		mLineTooltip = (TextView) inflater.inflate(R.layout.circular_tooltip, null);
		mLineTooltip.setText(Integer.toString((int) lineValues[setIndex][entryIndex]));

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Tools.fromDpToPx(35), (int) Tools.fromDpToPx(35));
		layoutParams.leftMargin = rect.centerX() - layoutParams.width / 2;
		layoutParams.topMargin = rect.centerY() - layoutParams.height / 2;
		mLineTooltip.setLayoutParams(layoutParams);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			mLineTooltip.setPivotX(layoutParams.width / 2);
			mLineTooltip.setPivotY(layoutParams.height / 2);
			mLineTooltip.setAlpha(0);
			mLineTooltip.setScaleX(0);
			mLineTooltip.setScaleY(0);
			mLineTooltip.animate()
					.setDuration(150)
					.alpha(1)
					.scaleX(1).scaleY(1)
					.rotation(360)
					.setInterpolator(enterInterpolator);
		}

		mLineChart.showTooltip(mLineTooltip);
	}


	@SuppressLint("NewApi")
	private void dismissLineTooltip(final LayoutInflater inflator, final int setIndex, final int entryIndex, final Rect rect) {

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mLineTooltip.animate()
					.setDuration(100)
					.scaleX(0).scaleY(0)
					.alpha(0)
					.setInterpolator(exitInterpolator).withEndAction(new Runnable() {
				@Override
				public void run() {
					mLineChart.removeView(mLineTooltip);
					mLineTooltip = null;
					if (entryIndex != -1)
						showLineTooltip(inflator, setIndex, entryIndex, rect);
				}
			});
		} else {
			mLineChart.dismissTooltip(mLineTooltip);
			mLineTooltip = null;
			if (entryIndex != -1)
				showLineTooltip(inflator, setIndex, entryIndex, rect);
		}
	}


	private void updateValues(LineChartView chartView) {

		chartView.updateValues(0, lineValues[1]);
		chartView.updateValues(1, lineValues[0]);
		chartView.notifyDataUpdate();
	}

	private Animation getAnimation(boolean newAnim) {
		if (newAnim)
			return new Animation()
					.setAlpha(mCurrAlpha)
					.setEasing(mCurrEasing)
					.setOverlap(mCurrOverlapFactor, mCurrOverlapOrder)
					.setStartPoint(mCurrStartX, mCurrStartY);
		else
			return new Animation()
					.setAlpha(mOldAlpha)
					.setEasing(mOldEasing)
					.setOverlap(mOldOverlapFactor, mOldOverlapOrder)
					.setStartPoint(mOldStartX, mOldStartY);
	}


	private void initMenu(View v) {

		mPlayBtn = (ImageButton) (v.findViewById(R.id.play));
		mPlayBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPlayBtn.setImageResource(R.drawable.play);
				mPlayBtn.setBackgroundResource(R.drawable.button);
				mPlayBtn.setEnabled(false);

				mLineChart.dismissAllTooltips();
				mLineTooltip = null;


				if (mNewInstance) {
					mLineChart.dismiss(getAnimation(false).setEndAction(null));

				} else {
					updateValues(mLineChart);

				}
				mNewInstance = !mNewInstance;
			}
		});


		mOrderBtn = (ImageButton) (v.findViewById(R.id.order));
		mOrderBtn.setOnClickListener(new View.OnClickListener() {
			private int index = 1;

			@Override
			public void onClick(View v) {
				setOverlap(index++);
				index = onClickChange(index, 4);
			}
		});


		mEaseBtn = (ImageButton) (v.findViewById(R.id.ease));
		mEaseBtn.setOnClickListener(new View.OnClickListener() {
			private int index = 1;

			@Override
			public void onClick(View v) {
				setEasing(index++);
				index = onClickChange(index, 4);
			}
		});


		mEnterBtn = (ImageButton) (v.findViewById(R.id.enter));
		mEnterBtn.setOnClickListener(new View.OnClickListener() {
			private int index = 1;

			@Override
			public void onClick(View v) {
				setEnterPosition(index++);
				index = onClickChange(index, 9);
			}
		});


		mAlphaBtn = (ImageButton) (v.findViewById(R.id.alpha));
		mAlphaBtn.setOnClickListener(new View.OnClickListener() {
			private int index = 1;

			@Override
			public void onClick(View v) {
				setAlpha(index++);
				index = onClickChange(index, 3);
			}
		});
	}

	private void setOverlap(int index) {

		switch (index) {
			case 0:
				mCurrOverlapFactor = 1;
				mCurrOverlapOrder = beginOrder;
				mOrderBtn.setImageResource(R.drawable.ordere);
				break;
			case 1:
				mCurrOverlapFactor = .5f;
				mCurrOverlapOrder = beginOrder;
				mOrderBtn.setImageResource(R.drawable.orderf);
				break;
			case 2:
				mCurrOverlapFactor = .5f;
				mCurrOverlapOrder = endOrder;
				mOrderBtn.setImageResource(R.drawable.orderl);
				break;
			case 3:
				mCurrOverlapFactor = .5f;
				mCurrOverlapOrder = middleOrder;
				mOrderBtn.setImageResource(R.drawable.orderm);
				break;
			default:
				break;
		}
	}

	private int onClickChange(int index, int limit) {
		mPlayBtn.setBackgroundResource(R.color.button_hey);
		if (index >= limit)
			index = 0;
		mNewInstance = true;
		return index;
	}

	private void setEasing(int index) {

		switch (index) {
			case 0:
				mCurrEasing = new CubicEaseOut();
				mEaseBtn.setImageResource(R.drawable.ease_cubic);
				break;
			case 1:
				mCurrEasing = new QuintEaseOut();
				mEaseBtn.setImageResource(R.drawable.ease_quint);
				break;
			case 2:
				mCurrEasing = new BounceEaseOut();
				mEaseBtn.setImageResource(R.drawable.ease_bounce);
				break;
			case 3:
				mCurrEasing = new ElasticEaseOut();
				mEaseBtn.setImageResource(R.drawable.ease_elastic);
			default:
				break;
		}
	}

	private void setEnterPosition(int index) {

		switch (index) {
			case 0:
				mCurrStartX = -1f;
				mCurrStartY = 0f;
				mEnterBtn.setImageResource(R.drawable.enterb);
				break;
			case 1:
				mCurrStartX = 0f;
				mCurrStartY = 0f;
				mEnterBtn.setImageResource(R.drawable.enterbl);
				break;
			case 2:
				mCurrStartX = 0f;
				mCurrStartY = -1f;
				mEnterBtn.setImageResource(R.drawable.enterl);
				break;
			case 3:
				mCurrStartX = 0f;
				mCurrStartY = 1f;
				mEnterBtn.setImageResource(R.drawable.entertl);
				break;
			case 4:
				mCurrStartX = -1f;
				mCurrStartY = 1f;
				mEnterBtn.setImageResource(R.drawable.entert);
				break;
			case 5:
				mCurrStartX = 1f;
				mCurrStartY = 1f;
				mEnterBtn.setImageResource(R.drawable.entertr);
				break;
			case 6:
				mCurrStartX = 1f;
				mCurrStartY = -1f;
				mEnterBtn.setImageResource(R.drawable.enterr);
				break;
			case 7:
				mCurrStartX = 1f;
				mCurrStartY = 0f;
				mEnterBtn.setImageResource(R.drawable.enterbr);
				break;
			case 8:
				mCurrStartX = .5f;
				mCurrStartY = .5f;
				mEnterBtn.setImageResource(R.drawable.enterc);
				break;
			default:
				break;
		}
	}


	@SuppressLint("NewApi")
	private void setAlpha(int index) {

		switch (index) {
			case 0:
				mCurrAlpha = -1;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					mAlphaBtn.setImageAlpha(255);
				else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
					mAlphaBtn.setAlpha(1f);
				break;
			case 1:
				mCurrAlpha = 2;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					mAlphaBtn.setImageAlpha(115);
				else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
					mAlphaBtn.setAlpha(.6f);
				break;
			case 2:
				mCurrAlpha = 1;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					mAlphaBtn.setImageAlpha(55);
				else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
					mAlphaBtn.setAlpha(.3f);
				break;
			default:
				break;
		}
	}

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
}
