package nieltg.ev3cube;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;

import nieltg.ev3cube.bt.DiscoverActivity;
import nieltg.ev3cube.ui.SenseActivity;
import nieltg.kociemba.CubeSolver;
import nieltg.kociemba.SolveException;
import nieltg.robomsg.Messenger;
import nieltg.robomsg.Message;

public class MainActivity extends AppCompatActivity
{
	public static final int REQUEST_BLUETOOTH = 1;

	public static Messenger messenger = null;
	public static String pending_msg = null;

	public final Messenger mMessenger = new Messenger ();

	private FloatingActionButton mFab;
	private ProgressDialog mProgressDialog;

	private boolean mIsResume = false;

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById (R.id.toolbar);
		setSupportActionBar (toolbar);
		getSupportActionBar ().setDisplayShowTitleEnabled (false);

		mFab = (FloatingActionButton) findViewById (R.id.fab);
		mFab.setOnClickListener (new View.OnClickListener ()
		{
			@Override
			public void onClick (View view)
			{
				Intent intent = new Intent (MainActivity.this, DiscoverActivity.class);
				startActivityForResult (intent, REQUEST_BLUETOOTH);
			}
		});

		messenger = mMessenger;
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case REQUEST_BLUETOOTH:

			if (resultCode == RESULT_OK)
			{
				BluetoothDevice device = data.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);

				showBluetoothProgress ();
				mMessenger.connect (device);
			}

			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
	{
		getMenuInflater ().inflate (R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
	{
		int id = item.getItemId ();

		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected (item);
	}

	@Override
	protected void onPause ()
	{
		super.onPause ();
		mIsResume = false;
	}

	@Override
	protected void onResume ()
	{
		super.onResume ();
		mIsResume = true;

		mMessenger.setListener (mListener);

		if (pending_msg != null)
		{
			Snackbar.make (mFab, pending_msg, Snackbar.LENGTH_LONG).show ();
			pending_msg = null;
		}
	}

	@Override
	protected void onDestroy ()
	{
		super.onDestroy ();
		mMessenger.disconnect ();
	}

	private void showBluetoothProgress ()
	{
		if (mProgressDialog == null)
			mProgressDialog = ProgressDialog.show (MainActivity.this, null, "Connecting...");
	}

	private void hideBluetoothProgress ()
	{
		if (mProgressDialog != null)
		{
			mProgressDialog.dismiss ();
			mProgressDialog = null;
		}
	}

	private final Messenger.Listener mListener = new Messenger.Listener ()
	{
		@Override
		public void onConnect ()
		{
			hideBluetoothProgress ();
			mFab.hide ();
		}

		@Override
		public void onConnectError (IOException e)
		{
			hideBluetoothProgress ();
			Snackbar.make (mFab, "Connection Error: " + e.getMessage (), Snackbar.LENGTH_LONG).show ();
		}

		@Override
		public void onReceive (Message msg)
		{
			Snackbar.make (mFab, "New message: " + msg.getTitle (), Snackbar.LENGTH_LONG).show ();

			switch (msg.getTitle ())
			{
			case "Ultrasonic":

				if (msg.getLogicContent () && mIsResume)
				{
					Intent intent = new Intent (MainActivity.this, SenseActivity.class);
					startActivity (intent);
				}

				break;
			}
		}

		@Override
		public void onReceiveError (IOException e)
		{
			Snackbar.make (mFab, "Receive Error: " + e.getMessage (), Snackbar.LENGTH_LONG).show ();
		}

		@Override
		public void onSend (Message msg)
		{
		}

		@Override
		public void onSendError (IOException e)
		{
			Snackbar.make (mFab, "Send Error: " + e.getMessage (), Snackbar.LENGTH_LONG).show ();
		}

		@Override
		public void onDisconnect ()
		{
			Snackbar.make (mFab, "Device is disconnected", Snackbar.LENGTH_LONG).show ();
			mFab.show ();
		}
	};
}
