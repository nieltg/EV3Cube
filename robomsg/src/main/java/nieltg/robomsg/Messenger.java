package nieltg.robomsg;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nieltg.robomsg.protocol.PayloadStream;

public class Messenger
{
	public static final UUID SERIAL_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");

	// See http://standards.ieee.org/regauth/oui/index.shtml
	public static final String LEGO_OUI = "00:16:53";

	public static boolean isCompatibleDevice (BluetoothDevice device)
	{
		return device.getAddress ().startsWith (LEGO_OUI);
	}

	private final ExecutorService mExecService = Executors.newSingleThreadExecutor ();

	private final Runnable mRunRead = new Runnable ()
	{
		@Override
		public void run ()
		{
			byte[] payload;

			try
			{
				while (true)
				{
					payload = mIs.readPayload ();
					if (payload == null) break;

					Message msg = new Message (payload);

					if (mListener != null)
						mListener.onReceive (msg);
				}
			}
			catch (IOException e)
			{
				if (mListener != null)
					mListener.onReceiveError (e);
			}

			disconnect ();

			if (mListener != null)
				mListener.onDisconnect ();
		}
	};

	private Thread mThreadRead;

	private Listener mListener;

	private BluetoothSocket mSocket;
	private PayloadStream.Input   mIs;
	private PayloadStream.Output  mOs;

	public Messenger ()
	{
	}

	public void setListener (Listener listener)
	{
		mListener = listener;
	}

	public void connect (final BluetoothDevice device)
	{
		if (!isCompatibleDevice (device))
			throw new IllegalArgumentException ("device is not compatible");

		mExecService.submit (new Runnable ()
		{
			@Override
			public void run ()
			{
				try
				{
					mSocket = device.createRfcommSocketToServiceRecord (SERIAL_UUID);
					mSocket.connect ();

					mIs = new PayloadStream.Input (mSocket.getInputStream ());
					mOs = new PayloadStream.Output (mSocket.getOutputStream ());

					if (mListener != null)
						mListener.onConnect ();

					mThreadRead = new Thread (mRunRead);
					mThreadRead.start ();
				}
				catch (IOException e)
				{
					if (mListener != null)
						mListener.onConnectError (e);
				}
			}
		});
	}

	public void sendMessage (final Message msg)
	{
		final byte[] payload = msg.encode ();

		mExecService.submit (new Runnable ()
		{
			@Override
			public void run ()
			{
				try
				{
					mOs.writePayload (payload);

					if (mListener != null)
						mListener.onSend (msg);
				}
				catch (IOException e)
				{
					if (mListener != null)
						mListener.onSendError (e);
				}
			}
		});
	}

	public void disconnect ()
	{
		mExecService.submit (new Runnable ()
		{
			@Override
			public void run ()
			{
				try
				{
					if (mIs != null)
						mIs.close ();
					if (mOs != null)
						mOs.close ();

					if (mSocket != null)
						mSocket.close ();
				}
				catch (IOException e)
				{
				}
			}
		});
	}

	public interface Listener
	{
		void onConnect ();
		void onConnectError (IOException e);

		void onReceive (Message msg);
		void onReceiveError (IOException e);

		void onSend (Message msg);
		void onSendError (IOException e);

		void onDisconnect ();

	}
}
