package nieltg.ev3cube.ui;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import nieltg.ev3cube.MainActivity;
import nieltg.ev3cube.R;
import nieltg.ev3cube.logic.Mechanic;
import nieltg.ev3cube.logic.Track;
import nieltg.ev3cube.logic.robot.CubeOutput;
import nieltg.kociemba.CubeSolver;
import nieltg.kociemba.SolveException;

public class MoveActivity extends AppCompatActivity
{
	public static final String EXTRA_FACELETS = "nieltg.ev3cube.ui.MoveActivity.FACELETS";
	public static final String EXTRA_TRACKING = "nieltg.ev3cube.ui.MoveActivity.TRACKING";

	private static final Handler mHandler = new Handler (Looper.getMainLooper ());

	private final CubeSolver mCubeSolver = new CubeSolver ();

	private Mechanic mMechanic;
	private TextView mInfoText;

	private boolean mIsDone = false;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_move);

		mInfoText = (TextView) findViewById (R.id.info);
		mCubeSolver.setListener (mSolveListener);

		mInfoText.setText ("Preparing...");
		mCubeSolver.prepare (getCacheDir ().getAbsolutePath ());
	}

	private CubeSolver.Listener mSolveListener = new CubeSolver.Listener ()
	{
		@Override
		public void onPrepare ()
		{
			mHandler.post (new Runnable ()
			{
				@Override
				public void run ()
				{
					mInfoText.setText ("Solving...");

					String facelets = getIntent ().getStringExtra (EXTRA_FACELETS);
					mCubeSolver.solve (facelets);
				}
			});
		}

		@Override
		public void onDestroy ()
		{
		}

		@Override
		public void onSolve (final String solution)
		{
			mHandler.post (new Runnable ()
			{
				@Override
				public void run ()
				{
					mInfoText.setText (solution);

					mMechanic = new Mechanic (MainActivity.messenger);
					mMechanic.setListener (mMechListener);

					Track track = getIntent ().getParcelableExtra (EXTRA_TRACKING);

					CubeOutput co = new CubeOutput (track, mMechanic, solution);
					int[] buf = co.begin ();

					// DEBUG

					StringBuilder sb = new StringBuilder ();

					for (int c : buf)
					{
						switch (c)
						{
						case Mechanic.FLIP:
							sb.append ("F ");
							break;
						case Mechanic.LOCK:
							sb.append ("L ");
							break;
						case Mechanic.R_CCW:
							sb.append ("CCW ");
							break;
						case Mechanic.R_CW:
							sb.append ("CW ");
							break;
						case Mechanic.ULOCK:
							sb.append ("U ");
							break;
						}
					}

					mInfoText.setText (solution + " | " + sb.toString ());
					Log.w ("CUBEMOVE::Koecimba", solution);
					Log.w ("CUBEMOVE::Mechanic", sb.toString ());
				}
			});


		}

		@Override
		public void onSolveError (final SolveException e)
		{
			mHandler.post (new Runnable ()
			{
				@Override
				public void run ()
				{
					mInfoText.setText ("Solve error...");

					MainActivity.pending_msg = "Solve Error: " + e.getMessage ();
					finish ();
				}
			});
		}
	};

	private Mechanic.Listener mMechListener = new Mechanic.Listener ()
	{
		@Override
		public void onSequenceComplete ()
		{
			mHandler.post (new Runnable ()
			{
				@Override
				public void run ()
				{
					if (mIsDone)
					{
						finish ();
						return;
					}

					mIsDone = true;

					mMechanic.spin ();
					mInfoText.setText ("Done~");
				}
			});
		}
	};
}
