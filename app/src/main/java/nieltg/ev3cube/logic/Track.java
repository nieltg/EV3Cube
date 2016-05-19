package nieltg.ev3cube.logic;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Track implements Parcelable
{
	public static final int UP    = 1;
	public static final int LEFT  = 2;
	public static final int FRONT = 3;
	public static final int BACK  = 4;
	public static final int RIGHT = 5;
	public static final int DOWN  = 6;

	public static final int[] DEFAULT_TRACK = { UP, LEFT, FRONT, RIGHT, BACK, DOWN };

	// Sequence: U, L, F, R, B, D
	private final int[] mTrack = new int[6];

	private List<Integer> mRecord;
	private boolean mLock;

	public Track ()
	{
		System.arraycopy (DEFAULT_TRACK, 0, mTrack, 0, mTrack.length);
	}

	public void apply (int[] buf)
	{
		for (int move : buf)
		{
			switch (move)
			{
			case Mechanic.FLIP  : flip (); break;
			case Mechanic.R_CW  : rotateCW (); break;
			case Mechanic.R_CCW : rotateCCW (); break;
			case Mechanic.LOCK  : lock (); break;
			case Mechanic.ULOCK : unlock (); break;

			default:
				throw new IllegalArgumentException ("invalid move");
			}
		}
	}

	@Override
	public String toString ()
	{
		StringBuilder sb = new StringBuilder ();

		for (int track : mTrack)
		{
			switch (track)
			{
			case Track.UP    : sb.append ("UP "); break;
			case Track.LEFT  : sb.append ("LEFT "); break;
			case Track.FRONT : sb.append ("FRONT "); break;
			case Track.BACK  : sb.append ("BACK "); break;
			case Track.RIGHT : sb.append ("RIGHT "); break;
			case Track.DOWN  : sb.append ("DOWN "); break;
			default: sb.append ("[UNK] "); break;
			}
		}

		return sb.toString ();
	}

	public void startTracking ()
	{
		mRecord = new ArrayList<> ();
	}

	public int[] stopTracking ()
	{
		int[] buf = new int[mRecord.size ()];

		for (int i = 0; i < buf.length; i++)
			buf[i] = mRecord.get (i).byteValue ();
		return buf;
	}

	public int search (int face)
	{
		for (int i = 0; i < mTrack.length; i++)
		{
			if (mTrack[i] == face)
				return i;
		}

		return -1;
	}

	public boolean isLocked ()
	{
		return mLock;
	}

	public void flip ()
	{
		int tempo = mTrack[1];
		mTrack[1] = mTrack[2];
		mTrack[2] = mTrack[3];
		mTrack[3] = mTrack[4];
		mTrack[4] = tempo;

		if (mRecord != null)
			mRecord.add (Mechanic.FLIP);
	}

	public void rotateCCW ()
	{
		// Fixed: Don't change cube state if cube is locked! (1)

		if (!mLock)
		{
			int tempo = mTrack[0];
			mTrack[0] = mTrack[3];
			mTrack[3] = mTrack[5];
			mTrack[5] = mTrack[1];
			mTrack[1] = tempo;
		}

		if (mRecord != null)
			mRecord.add (Mechanic.R_CCW);
	}

	public void rotateCW ()
	{
		// Fixed: Don't change cube state if cube is locked! (2)

		if (!mLock)
		{
			int tempo = mTrack[0];
			mTrack[0] = mTrack[1];
			mTrack[1] = mTrack[5];
			mTrack[5] = mTrack[3];
			mTrack[3] = tempo;
		}

		if (mRecord != null)
			mRecord.add (Mechanic.R_CW);
	}

	public void lock ()
	{
		mLock = true;

		if (mRecord != null)
			mRecord.add (Mechanic.LOCK);
	}

	public void unlock ()
	{
		mLock = false;

		if (mRecord != null)
			mRecord.add (Mechanic.ULOCK);
	}

	@Override
	public int describeContents ()
	{
		return 0;
	}

	@Override
	public void writeToParcel (Parcel dest, int flags)
	{
		dest.writeIntArray (mTrack);
	}

	public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track> ()
	{
		@Override
		public Track createFromParcel (Parcel source)
		{
			return new Track (source);
		}

		@Override
		public Track[] newArray (int size)
		{
			return new Track[size];
		}
	};

	private Track (Parcel source)
	{
		source.readIntArray (mTrack);
	}
}
