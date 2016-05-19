package nieltg.ev3cube.logic.robot;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Arrays;

import nieltg.cvcube.CubeSense;
import nieltg.ev3cube.logic.Mechanic;
import nieltg.ev3cube.logic.Track;

public class CubeInput
{
	private static final Handler mHandler = new Handler (Looper.getMainLooper ());

	// Sequence: F, R, B, L, U, D
	public static final char[] SEQ_FACE = { 'F', 'R', 'B', 'L', 'U', 'D' };

	public static final int[][] SEQ_MECHANIC = {
			{Mechanic.FLIP},
			{Mechanic.FLIP},
			{Mechanic.FLIP},
			{Mechanic.R_CW, Mechanic.FLIP},
			{Mechanic.FLIP, Mechanic.FLIP, Mechanic.R_CW, Mechanic.R_CW},
			{}
	};

	public static final int[][] SEQ_BOUNDARY = {
			{415, 80, 400, 400},
			{405, 80, 400, 400},
			{405, 80, 400, 400},
			{405, 80, 400, 400},
			{405, 60, 400, 400},
			{380, 30, 400, 400}
	};

	public static final int COLOR_RETRY = 3;

	private final Mechanic mMechanic;
	private final CubeSense mSense;
	private final Track mTrack;

	private int[][] mBufColor = new int[6][9];

	private int mColorRetry = COLOR_RETRY;
	private String mFinalResult;

	private Listener mListener;

	private int mIndex = 0;

	public CubeInput (Mechanic mechanic, CubeSense sense)
	{
		this (new Track (), mechanic, sense);
	}

	public CubeInput (Track track, Mechanic mechanic, CubeSense sense)
	{
		if (track == null)
			throw new NullPointerException ();
		mTrack = track;

		if (mechanic == null)
			throw new NullPointerException ();
		mMechanic = mechanic;

		mechanic.setListener (mMechListener);

		if (sense == null)
			throw new NullPointerException ("sense is null");
		mSense = sense;
	}

	public void setListener (Listener listener)
	{
		mListener = listener;
	}

	public Track getTrack ()
	{
		return mTrack;
	}

	public void begin ()
	{
		mHandler.post (mRunBoundary);
	}

	private String convert ()
	{
		// Sequence: F, R, B, L, U, D

		char[] replace = new char[6];

		for (int i = 0; i < mBufColor.length; i++)
			replace[mBufColor[i][4]] = SEQ_FACE[i];

		// Sequence: F, R, B, L, U, D

		String[] face_sc = new String[6];

		for (int i = 0; i < mBufColor.length; i++)
		{
			StringBuilder sb = new StringBuilder ();

			for (int col : mBufColor[i])
				sb.append (replace[col]);

			face_sc[i] = sb.toString ();
		}

		// Id: 0, 1, 2, 3, 4, 5
		// In: F, R, B, L, U, D
		// To: U, R, F, D, L, B

		StringBuilder sb = new StringBuilder ();

		sb.append (face_sc[4]);
		sb.append (face_sc[1]);
		sb.append (face_sc[0]);
		sb.append (face_sc[5]);
		sb.append (face_sc[3]);
		sb.append (face_sc[2]);

		Log.w ("CUBEINPUT::Final", sb.toString ());
		return sb.toString ();
	}

	private boolean isColorValid (int[] color)
	{
		for (int col : color)
		{
			if (col == CubeSense.COLOR_N)
				return false;
		}

		return true;
	}

	private Runnable mRunBoundary = new Runnable ()
	{
		@Override
		public void run ()
		{
			try
			{
				int[] b = SEQ_BOUNDARY[mIndex];
				mSense.setBoundary (b[0], b[1], b[2], b[3]);
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				// Workaround: fix looping!
			}

			mHandler.postDelayed (mRunNext, 3000);
		}
	};

	private Runnable mRunNext = new Runnable ()
	{
		@Override
		public void run ()
		{
			if (mIndex < SEQ_MECHANIC.length)
			{
				int[] cap = mSense.capture ();
				Log.w ("CUBEINPUT::Capture", Arrays.toString (cap));

				if (!isColorValid (cap))
				{
					if ((--mColorRetry) < 0)
						// No attempt allowed anymore
						if (mListener != null)
							mListener.onScanError ();
						else
							// Rescan color
							begin ();

					return;
				}

				mBufColor[mIndex] = cap;

				// Fixed: set 'mColorRetry' to default value
				// Only if program has received valid colors
				mColorRetry = COLOR_RETRY;

				int i = mIndex++;
				int[] seq = SEQ_MECHANIC[i];

				mTrack.apply (seq);
				mMechanic.move (seq);
			}
			else
			{
				mFinalResult = convert ();

				if (mListener != null)
					mListener.onScanComplete (mFinalResult);
			}
		}
	};

	private Mechanic.Listener mMechListener = new Mechanic.Listener ()
	{
		@Override
		public void onSequenceComplete ()
		{
			begin ();
		}
	};

	public interface Listener
	{
		void onScanError ();
		void onScanComplete (String facelets);
	}
}
