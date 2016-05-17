package nieltg.ev3cube.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nieltg.ev3cube.R;
import nieltg.robomsg.Messenger;

public class DiscoveryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private static final int TYPE_DEVICE = 1;
	private static final int TYPE_HEADER_BONDED = 2;
	private static final int TYPE_HEADER_DISCOV = 3;

	private Listener mListener;

	private final BluetoothAdapter mAdapter;
	private final LayoutInflater mInflate;

	private final List<BluetoothDevice> mBonded = new ArrayList<> ();
	private final List<BluetoothDevice> mDiscov = new ArrayList<> ();

	public DiscoveryAdapter (@NonNull Context context)
	{
		mAdapter = BluetoothAdapter.getDefaultAdapter ();
		if (mAdapter == null) throw new UnsupportedOperationException ("no bluetooth support");

		mInflate = LayoutInflater.from (context);
		setHasStableIds (true);
	}

	public void reset ()
	{
		mBonded.clear ();
		mDiscov.clear ();

		final Set<BluetoothDevice> devices = mAdapter.getBondedDevices ();

		for (BluetoothDevice device : devices)
		{
			if (Messenger.isCompatibleDevice (device))
				mBonded.add (device);
		}

		notifyDataSetChanged ();
	}

	public void notifyDevice (BluetoothDevice device)
	{
		if (Messenger.isCompatibleDevice (device))
		{
			if (!mDiscov.contains (device))
				mDiscov.add (device);
		}

		notifyDataSetChanged ();
	}

	public void setListener (Listener listener)
	{
		mListener = listener;
	}

	public Listener getListener ()
	{
		return mListener;
	}

	@Override
	public int getItemViewType (int position)
	{
		// if mBonded empty,
		//  [HEAD_SEARCH] Devs
		// else,
		// [HEAD_PAIRED] Devs [HEAD_SEARCH] Devs

		// Paired Devices

		if (!mBonded.isEmpty ())
			position--;
		if (position == -1)
			return TYPE_HEADER_BONDED;
		else
		if ((position >= 0) && (position < mBonded.size ()))
			return TYPE_DEVICE;

		position -= mBonded.size ();

		// Available Devices

		if (!mDiscov.isEmpty ())
			position--;
		if (position == -1)
			return TYPE_HEADER_DISCOV;
		else
		if ((position >= 0) && (position < mDiscov.size ()))
			return TYPE_DEVICE;

		return RecyclerView.INVALID_TYPE;
	}

	private BluetoothDevice getDeviceByPos (int position)
	{
		// Paired Devices

		if (!mBonded.isEmpty ())
			position--;
		if ((position >= 0) && (position < mBonded.size ()))
			return mBonded.get (position);

		position -= mBonded.size ();

		// Available Devices

		if (!mDiscov.isEmpty ())
			position--;
		if ((position >= 0) && (position < mDiscov.size ()))
			return mDiscov.get (position);

		return null;
	}

	@Override
	public long getItemId (int position)
	{
		// Unique ID layout
		// HHTT (hash: 32-bit; type: 32-bit)

		int viewType = getItemViewType (position);
		if (viewType == RecyclerView.INVALID_TYPE)
			return RecyclerView.NO_ID;

		long id = (long) viewType;

		if (viewType == TYPE_DEVICE)
		{
			BluetoothDevice dev = getDeviceByPos (position);
			if (dev != null) id = ((long) dev.hashCode () << 32) | id;
		}

		return id;
	}

	@Override
	public int getItemCount ()
	{
		int h = 0;

		if (!mBonded.isEmpty ()) h++;
		if (!mDiscov.isEmpty ()) h++;

		return (mBonded.size ()) + (mDiscov.size ()) + h;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
	{
		final View view;

		switch (viewType)
		{
		case TYPE_DEVICE:
			view = mInflate.inflate (R.layout.item_bt_device, parent, false);
			return new DiscoveryAdapter.Device (view);

		case TYPE_HEADER_BONDED:
		case TYPE_HEADER_DISCOV:
			view = mInflate.inflate (R.layout.item_bt_device_header, parent, false);
			return new DiscoveryAdapter.Header (view);
		}

		return null;
	}

	@Override
	public void onBindViewHolder (RecyclerView.ViewHolder holder, int position)
	{
		int viewType = getItemViewType (position);

		switch (viewType)
		{
		case TYPE_DEVICE:
			Device dev = (Device) holder;
			dev.setItem (getDeviceByPos (position));
			break;

		case TYPE_HEADER_BONDED:
		case TYPE_HEADER_DISCOV:
			Header sec = (Header) holder;
			sec.setItem (viewType);
			break;
		}
	}

	private class Header extends RecyclerView.ViewHolder
	{
		private final TextView mHeader;

		public Header (View itemView)
		{
			super (itemView);

			mHeader = (TextView) itemView.findViewById (R.id.bt_header);
		}

		public void setItem (int viewType)
		{
			switch (viewType)
			{
			case TYPE_HEADER_BONDED:
				mHeader.setText (R.string.section_bt_paired);
				break;

			case TYPE_HEADER_DISCOV:
				mHeader.setText (R.string.section_bt_available);
				break;
			}
		}
	}

	private class Device extends RecyclerView.ViewHolder
	{
		private final ImageView mIcon;
		private final TextView mName;
		private final TextView mAddress;

		private BluetoothDevice mDevice;

		public Device (View itemView)
		{
			super (itemView);

			itemView.setOnClickListener (new View.OnClickListener ()
			{
				@Override
				public void onClick (View v)
				{
					if (mListener != null)
						mListener.onDeviceChoosed (mDevice);
				}
			});

			mIcon = (ImageView) itemView.findViewById (R.id.bt_icon);
			mName = (TextView) itemView.findViewById (R.id.bt_name);
			mAddress = (TextView) itemView.findViewById (R.id.bt_address);
		}

		public void setItem (BluetoothDevice device)
		{
			mDevice = device;

			mName.setText (device.getName ());
			mAddress.setText (device.getAddress ());
		}
	}

	public static interface Listener
	{
		public void onDeviceChoosed (BluetoothDevice device);
	}
}
