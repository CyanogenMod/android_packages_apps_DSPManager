package com.bel.android.dspmanager.preference;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

public class EqualizerSurface extends SurfaceView {
	public static int MIN_FREQ = 10;
	public static int MAX_FREQ = 20000;
	public static int SAMPLING_RATE = 44100;
	public static int MIN_DB = -10;
	public static int MAX_DB = 10;
	public static final float CURVE_RESOLUTION = 1.25f;

	private int width;
	private int height;
	private int barwidth;

	/* Fixme: generalize with frequencies read from equalizer object */
	private final float[] levels = new float[6];
	private final Paint white, whiteCentered, gray, green, blue, purple, red;

	public EqualizerSurface(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		setWillNotDraw(false);

		white = new Paint();
		white.setColor(0xffffffff);
		white.setStyle(Style.STROKE);
		white.setTextSize(13);
		white.setAntiAlias(true);

		whiteCentered = new Paint(white);
		whiteCentered.setTextAlign(Paint.Align.CENTER);

		gray = new Paint();
		gray.setColor(0x22ffffff);
		gray.setStyle(Style.STROKE);

		green = new Paint();
		green.setColor(0x8800ff00);
		green.setStyle(Style.STROKE);
		green.setAntiAlias(true);
		green.setStrokeWidth(4);

		purple = new Paint();
		purple.setColor(0x88ff00ff);
		purple.setStyle(Style.STROKE);

		blue = new Paint();
		blue.setColor(0x880000ff);
		blue.setStyle(Style.FILL_AND_STROKE);
		blue.setStrokeWidth(1);
		blue.setAntiAlias(true);

		red = new Paint();
		red.setColor(0x88ff0000);
		red.setStyle(Style.FILL_AND_STROKE);
		red.setStrokeWidth(2);
		red.setAntiAlias(true);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		buildLayer();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		width = right - left;
		height = bottom - top;
		barwidth = (width/(levels.length+1)) / 4;
		green.setShader(new LinearGradient(0,0,0,height, 0xffbfff00, 0xff003300, Shader.TileMode.CLAMP));
	}

	public void setBand(int i, float value) {
		levels[i] = value;
		postInvalidate();
	}

	public float getBand(int i) {
		return levels[i];
	}

	@Override
	protected void onDraw(Canvas canvas) {
		/* clear canvas */
		canvas.drawRGB(0, 0, 0);

		/* Set the width of the bars according to canvas size */
		green.setStrokeWidth(barwidth);

		canvas.drawRect(0, 0, width-1, height-1, white);

		/* draw vertical lines */
		for (int freq = MIN_FREQ; freq < MAX_FREQ;) {
			if (freq < 100) {
				freq += 10;
			} else if (freq < 1000) {
				freq += 100;
			} else if (freq < 10000) {
				freq += 1000;
			} else {
				freq += 10000;
			}

			float x = projectX(freq) * width;
			canvas.drawLine(x, 0, x, height - 1, gray);
			if (freq == 100 || freq == 1000 || freq == 10000) {
				canvas.drawText(freq < 1000 ? "" + freq : freq/1000 + "k", x, height-1, white);
			}
		}

		/* draw horizontal lines */
		for (int dB = MIN_DB; dB <= MAX_DB; dB += 5) {
			float y = projectY(dB) * height;
			if (dB == 0) {
				canvas.drawLine(0, y, width - 1, y, red);
			} else {
				canvas.drawLine(0, y, width - 1, y, gray);
			}
			canvas.drawText(String.format("%d", Math.abs(dB)), 1, y - 1, white);
		}

		Biquad[] biquads = new Biquad[] {
				new Biquad(),
				new Biquad(),
				new Biquad(),
				new Biquad(),
				new Biquad(),
		};

		/* The filtering is realized with cascaded 2nd order high shelves, and each band
		 * is realized as a transition relative to the previous band.
		 * 1st band has no previous band, so it's just a fixed gain.
		 */
		float gain = (float) Math.pow(10, levels[0] / 20);
		for (int i = 0; i < biquads.length; i ++) {
			float freq = 15.625f * (float) Math.pow(4, i);
			biquads[i].setHighShelf(freq * 2f, SAMPLING_RATE, levels[i + 1] - levels[i], 1f);
		}

		float oldx = -1;
		float olddB = 0;
		//float olds = 0;
		for (float freq = MIN_FREQ / CURVE_RESOLUTION; freq < MAX_FREQ * CURVE_RESOLUTION; freq *= CURVE_RESOLUTION) {
			float omega = freq / SAMPLING_RATE * (float) Math.PI * 2;
			Complex z = new Complex((float) Math.cos(omega), (float) Math.sin(omega));

			/* Evaluate the response at frequency z */
			Complex z1 = z.mul(gain);
			Complex z2 = biquads[0].evaluateTransfer(z);
			Complex z3 = biquads[1].evaluateTransfer(z);
			Complex z4 = biquads[2].evaluateTransfer(z);
			Complex z5 = biquads[3].evaluateTransfer(z);
			Complex z6 = biquads[4].evaluateTransfer(z);

			/* Magnitude response, dB */
			float dB = lin2dB(z1.rho() * z2.rho() * z3.rho() * z4.rho() * z5.rho() * z6.rho());
			float newBb = projectY(dB) * height;

			/* Time delay response, s */
			//float s = (z1.theta() + z2.theta() + z3.theta() + z4.theta() + z5.theta() + z6.theta()) / (float) Math.PI / freq;
			//float news = projectY(s * 1000) * height;

			float newx = projectX(freq) * width;

			if (oldx != -1) {
				canvas.drawLine(oldx, olddB, newx, newBb, blue);
				//canvas.drawLine(oldx, olds, newx, news, purple);
			}
			oldx = newx;
			olddB = newBb;
			//olds = news;
		}

		for (int i = 0; i < levels.length; i ++) {
			float freq = 15.625f * (float) Math.pow(4, i);
			float x = projectX(freq) * width;
			float y = projectY(levels[i]) * height;
			canvas.drawLine(x, height/2, x, y, green);
			canvas.drawText(String.format("%1.1f", Math.abs(levels[i])), x, height/2, whiteCentered);
		}
	}

	private float projectX(float freq) {
		double pos = Math.log(freq);
		double minPos = Math.log(MIN_FREQ);
		double maxPos = Math.log(MAX_FREQ);
		pos = (pos - minPos) / (maxPos - minPos);
		return (float) pos;
	}

	private float projectY(float dB) {
		float pos = (dB - MIN_DB) / (MAX_DB - MIN_DB);
		return 1 - pos;
	}

	private float lin2dB(float rho) {
		return rho != 0 ? (float) (Math.log(rho) / Math.log(10) * 20) : -99f;
	}

	/**
	 * Find the closest control to press coordinate for adjustment
	 *
	 * @param px
	 * @return index of best match
	 */
	public int findClosest(float px) {
		int idx = 0;
		float best = 99999;
		for (int i = 0; i < levels.length; i ++) {
			float freq = 15.625f * (float) Math.pow(4, i);
			float cx = projectX(freq) * width;
			float distance = Math.abs(cx - px);

			if (distance < best) {
				idx = i;
				best = distance;
			}
		}

		return idx;
	}
}
