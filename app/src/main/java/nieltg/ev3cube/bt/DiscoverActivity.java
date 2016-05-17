package nieltg.ev3cube.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import nieltg.ev3cube.R;

public class DiscoverActivity extends AppCompatActivity
{
	private static final int REQUEST_ENABLE_BT = 1;

	private Button mScanButton;
	private ProgressBar mProgress;

	private DiscoveryAdapter mListAdapter;
	private BluetoothAdapter mBTAdapter;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver ()
	{
		@Override
		public void onReceive (Context context, Intent intent)
		{
			String action = intent.getAction ();

			switch (action)
			{
			case BluetoothDevice.ACTION_FOUND:
				BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
				mListAdapter.notifyDevice (device);
				break;

			case BluetoothAdapter.ACTION_STATE_CHANGED:
				int state = intent.getIntExtra (BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				if (state == BluetoothAdapter.STATE_OFF)
					finish ();
				break;

			case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
				mListAdapter.reset ();

				mProgress.setVisibility (View.VISIBLE);
				mScanButton.setText (R.string.stop);
				break;

			case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
				mProgress.setVisibility (View.INVISIBLE);
				mScanButton.setText (R.string.scan);
				break;
			}
		}
	};

	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_discover);

		setResult (Activity.RESULT_CANCELED, null);

		mBTAdapter = BluetoothAdapter.getDefaultAdapter ();
		if (mBTAdapter == null)
		{
			Toast.makeText (this, R.string.msg_no_bluetooth, Toast.LENGTH_LONG).show ();
			finish ();
		}

		RecyclerView discover = (RecyclerView) findViewById (R.id.devices_list);
		discover.setHasFixedSize (false);

		LinearLayoutManager llm = new LinearLayoutManager (this);
		llm.setOrientation (LinearLayoutManager.VERTICAL);
		discover.setLayoutManager (llm);

		mListAdapter = new DiscoveryAdapter (this);
		discover.setAdapter (mListAdapter);

		mListAdapter.setListener (new DiscoveryAdapter.Listener ()
		{
			@Override
			public void onDeviceChoosed (BluetoothDevice device)
			{
				Intent intent = new Intent ();
				intent.putExtra (BluetoothDevice.EXTRA_DEVICE, device);

				setResult (Activity.RESULT_OK, intent);
				finish ();
			}
		});

		mProgress = (ProgressBar) findViewById (R.id.progress);

		Button btn = (Button) findViewById (R.id.cancel);
		btn.setOnClickListener (new View.OnClickListener ()
		{
			@Override
			public void onClick (View v)
			{
				finish ();
			}
		});

		mScanButton = (Button) findViewById (R.id.scan);
		mScanButton.setOnClickListener (new View.OnClickListener ()
		{
			@Override
			public void onClick (View v)
			{
				if (mBTAdapter.isDiscovering ())
					mBTAdapter.cancelDiscovery ();
				else
					mBTAdapter.startDiscovery ();
			}
		});
	}

	@Override
	protected void onResume ()
	{
		super.onResume ();

		// Fixed: Register all receivers first, then startDiscovery ()
		// Prevent race-condition between evts and registerReceiver ()

		IntentFilter filter;

		filter = new IntentFilter (BluetoothDevice.ACTION_FOUND);
		registerReceiver (mReceiver, filter);
		filter = new IntentFilter (BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver (mReceiver, filter);
		filter = new IntentFilter (BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		registerReceiver (mReceiver, filter);
		filter = new IntentFilter (BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver (mReceiver, filter);

		if (mBTAdapter.isEnabled ())
			mBTAdapter.startDiscovery ();
		else
		{
			Intent intent = new Intent (BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult (intent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onPause ()
	{
		super.onPause ();

		mBTAdapter.cancelDiscovery ();
		unregisterReceiver (mReceiver);
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQUEST_ENABLE_BT:
				if (resultCode == Activity.RESULT_OK)
				{
					// No need to startDiscovery() here
					// It will be handled by onResume()
				}
				else
					finish ();
		}
	}
}
