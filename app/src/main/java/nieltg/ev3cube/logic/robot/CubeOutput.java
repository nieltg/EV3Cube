package nieltg.ev3cube.logic.robot;

import nieltg.ev3cube.logic.Mechanic;
import nieltg.ev3cube.logic.Track;

public class CubeOutput
{
	private final Track mTracking;
	private final Mechanic mMechanic;
	private final String[] mSolution;

	public CubeOutput (Track track, Mechanic mechanic, String solution)
	{
		if (track == null)
			throw new NullPointerException ("track is null");
		mTracking = track;

		if (mechanic == null)
			throw new NullPointerException ("mechanic is null");
		mMechanic = mechanic;

		if (solution == null)
			throw new NullPointerException ("solution is null");
		mSolution = solution.split (" ");
	}

	private int seqFromChar (char cel)
	{
		switch (cel)
		{
		case 'U': return Track.UP;
		case 'L': return Track.LEFT;
		case 'F': return Track.FRONT;
		case 'B': return Track.BACK;
		case 'R': return Track.RIGHT;
		case 'D': return Track.DOWN;
		}

		return -1;
	}

	private void applySequence (int face, boolean accent, int loop)
	{
		int pos = mTracking.search (face);

		switch (pos)
		{
		case 0:
			mTracking.rotateCCW ();
			mTracking.flip ();
			break;

		case 1:
			mTracking.flip ();
			break;

		case 2:
			mTracking.flip ();
			mTracking.flip ();
			break;

		case 3:
			mTracking.rotateCW ();
			mTracking.rotateCW ();
			mTracking.flip ();
			break;

		case 4:
			break;

		case 5:
			mTracking.rotateCW ();
			mTracking.flip ();
		}

		mTracking.lock ();

		for (int i = 0; i < loop; i++)
		{
			// Fixed: Accent in solution means rotate clockwise
			// instead of rotate counter-clockwise (mirroring)

			if (accent) mTracking.rotateCW ();
			else mTracking.rotateCCW ();
		}

		mTracking.unlock ();
	}

	public int[] begin ()
	{
		mTracking.startTracking ();

		for (String sol : mSolution)
		{
			int face = seqFromChar (sol.charAt (0));

			int loop = 1;
			boolean ccw = false;

			if (sol.length () > 1)
			{
				char x = sol.charAt (1);

				if (x == '2')
					loop++;
				else if (x == '\'')
					ccw = true;
			}

			applySequence (face, ccw, loop);
		}

		int[] buf = mTracking.stopTracking ();
		mMechanic.move (buf);

		return buf;
	}
}
