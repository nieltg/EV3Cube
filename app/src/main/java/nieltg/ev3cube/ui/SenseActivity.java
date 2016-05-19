package nieltg.ev3cube.ui;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import nieltg.cvcube.CubeSense;

import nieltg.ev3cube.MainActivity;
import nieltg.ev3cube.R;
import nieltg.ev3cube.logic.Mechanic;
import nieltg.ev3cube.logic.robot.CubeInput;

public class SenseActivity extends Activity
{
	private static final String TAG = "SenseActivity";

	private CameraBridgeViewBase mOpenCvCameraView;
	private boolean mIsJavaCamera = true;
	private MenuItem mItemSwitchCamera = null;

	private CubeSense mSense;
	private CubeInput mCubeInput;
	private Mechanic mMechanic;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		getWindow ().addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView (R.layout.activity_sense);

		mMechanic = new Mechanic (MainActivity.messenger);
		mSense = new CubeSense ();

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById (R.id.cvsurface);

		mOpenCvCameraView.setVisibility (SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener (mSense);
	}

	@Override
	public void onPause ()
	{
		super.onPause ();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView ();
	}

	@Override
	public void onResume ()
	{
		super.onResume ();
		if (!OpenCVLoader.initDebug ())
		{
			Log.d (TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync (OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		} else
		{
			Log.d (TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected (LoaderCallbackInterface.SUCCESS);
		}
	}

	private final CubeInput.Listener mInputListener = new CubeInput.Listener ()
	{
		@Override
		public void onScanComplete (String facelets)
		{
			Intent intent = new Intent (SenseActivity.this, MoveActivity.class);

			Log.w ("CUBESENSE::Track", mCubeInput.getTrack ().toString ());

			intent.putExtra (MoveActivity.EXTRA_FACELETS, facelets);
			intent.putExtra (MoveActivity.EXTRA_TRACKING, mCubeInput.getTrack ());

			startActivity (intent);
			finish ();
		}

		@Override
		public void onScanError ()
		{
			MainActivity.pending_msg = "Unable to scan";
			finish ();
		}
	};

	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback (this)
	{
		@Override
		public void onManagerConnected (int status)
		{
			switch (status)
			{
			case LoaderCallbackInterface.SUCCESS:
				Log.i (TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView ();

				mCubeInput = new CubeInput (mMechanic, mSense);
				mCubeInput.setListener (mInputListener);

				mCubeInput.begin ();
				break;

			default:
				super.onManagerConnected (status);
				break;
			}
		}
	};
}
