package nieltg.cvcube;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class CubeSense implements CameraBridgeViewBase.CvCameraViewListener2
{
	public static final Rect DEFAULT_BOUNDARY = new Rect (415, 100, 400, 400);
	public static final Size DEFAULT_SCANSIZE = new Size (50, 50);

	public static final int COLOR_N = -1;
	public static final int COLOR_0 = 0;  // White
	public static final int COLOR_1 = 1;  // Green
	public static final int COLOR_2 = 2;  // Red
	public static final int COLOR_3 = 3;  // Blue
	public static final int COLOR_4 = 4;  // Orange
	public static final int COLOR_5 = 5;  // Yellow

	private final Scalar[] mColors = new Scalar[6];
	private final Scalar mColor1 = new Scalar (0, 255, 0);
	private final Scalar mColor2 = new Scalar (0, 0, 255);

	private final Rect[] mRects = new Rect[9];
	private final Rect[] mPrevs = new Rect[9];
	private final int[] mCCodes = new int[9];

	private Rect mRect;
	private Mat mHue, mSat;

	public CubeSense ()
	{
		setBoundary (DEFAULT_BOUNDARY, DEFAULT_SCANSIZE);

		Rect cprev = new Rect (20, 50, 100, 100);
		generateRect2 (cprev, new Size (30, 30), mPrevs);

		mColors[0] = new Scalar (255, 255, 255);
		mColors[1] = new Scalar (  0, 255,   0);
		mColors[2] = new Scalar (255,   0,   0);
		mColors[3] = new Scalar (  0,   0, 255);
		mColors[4] = new Scalar (255, 128,   0);
		mColors[5] = new Scalar (255, 255,   0);
	}

	public void setBoundary (int x, int y, int w, int h, int w2, int h2)
	{
		Rect rect = new Rect (x, y, w, h);
		Size size = new Size (w2, h2);
		setBoundary (rect, size);
	}

	public void setBoundary (int x, int y, int w, int h)
	{
		Rect rect = new Rect (x, y, w, h);
		setBoundary (rect, DEFAULT_SCANSIZE);
	}

	public void setBoundary (Rect rect, Size size)
	{
		mRect = rect;
		generateRect2 (rect, size, mRects);
	}

	public int[] capture ()
	{
		int[] buf = new int[mCCodes.length];
		System.arraycopy (mCCodes, 0, buf, 0, buf.length);

		return buf;
	}

	private void generateRect2 (Rect m, Size cs, Rect[] out)
	{
		Point tl = m.tl ();
		Point br = m.br ();

		double x1 = tl.x;
		double x2 = tl.x + 0.5 * (m.width - cs.width);
		double x3 = tl.x + m.width - cs.width;
		double y1 = tl.y;
		double y2 = tl.y + 0.5 * (m.height - cs.height);
		double y3 = tl.y + m.height - cs.height;

		out[0] = new Rect (new Point (x1, y1), cs);
		out[1] = new Rect (new Point (x2, y1), cs);
		out[2] = new Rect (new Point (x3, y1), cs);
		out[3] = new Rect (new Point (x1, y2), cs);
		out[4] = new Rect (new Point (x2, y2), cs);
		out[5] = new Rect (new Point (x3, y2), cs);
		out[6] = new Rect (new Point (x1, y3), cs);
		out[7] = new Rect (new Point (x2, y3), cs);
		out[8] = new Rect (new Point (x3, y3), cs);
	}

	@Override
	public void onCameraViewStarted (int width, int height)
	{
		mHue = new Mat ();
		mSat = new Mat ();
	}

	@Override
	public void onCameraViewStopped ()
	{
		mHue.release ();
		mSat.release ();
	}

	private byte detectHSV (int hue, int sat)
	{
		if (sat < 50)
			return COLOR_0;  // White
		if ((hue >= 0) && (hue <= 37))
			return COLOR_3;  // Blue
		if ((hue >= 38) && (hue <= 60))
			return COLOR_1;  // Green
		if ((hue >= 61) && (hue <= 90))
			return COLOR_5;  // Yellow
		if ((hue >= 91) && (hue <= 117))
			return COLOR_4;  // Orange
		if ((hue >= 118) && (hue <= 130))
			return COLOR_2;  // Red

		return COLOR_N;
	}

	@Override
	public Mat onCameraFrame (CameraBridgeViewBase.CvCameraViewFrame inputFrame)
	{
		Mat rgba = inputFrame.rgba ();

		for (int i = 0; i < mRects.length; i++)
		{
			Rect rect = mRects[i];

			Mat crop = rgba.submat (rect);
			Imgproc.cvtColor (crop, crop, Imgproc.COLOR_BGR2HSV);

			Core.extractChannel (crop, mHue, 0);
			Core.extractChannel (crop, mSat, 1);

			int hue = (int) Core.mean (mHue).val[0];
			int sat = (int) Core.mean (mSat).val[0];
			mCCodes[i] = detectHSV (hue, sat);

			// Log.d ("CubeSense", "CELL " + i + ": " + hue + " " + sat);
			Imgproc.rectangle (rgba, rect.br (), rect.tl (), mColor1, 4);
		}
		Imgproc.rectangle (rgba, mRect.br (), mRect.tl (), mColor2, 4);

		for (int i = 0; i < mPrevs.length; i++)
		{
			Rect rect = mPrevs[i];
			int ccode = mCCodes[i];

			if ((ccode < 0) || (ccode > mColors.length)) break;
			Imgproc.rectangle (rgba, rect.br (), rect.tl (), mColors[ccode], -1);
		}

		return rgba;
	}
}
