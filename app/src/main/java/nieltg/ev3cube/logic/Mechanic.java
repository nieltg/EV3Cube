package nieltg.ev3cube.logic;

import java.io.IOException;

import nieltg.robomsg.Message;
import nieltg.robomsg.Messenger;

public class Mechanic
{
	public static final int SPIN  = 0;
	public static final int R_CW  = 1;
	public static final int R_CCW = 2;
	public static final int FLIP  = 3;
	public static final int LOCK  = 4;
	public static final int ULOCK = 5;

	public static final int[] SEQ_SPIN = { SPIN };

	private final Messenger mMessenger;

	private Listener mListener;

	private int[] mSeq = null;
	private int mSeqIndex = 0;

	public Mechanic (Messenger messenger)
	{
		if (messenger == null)
			throw new NullPointerException ("messenger is null");
		mMessenger = messenger;

		mMessenger.setListener (mRoboListener);
	}

	public void setListener (Listener listener)
	{
		mListener = listener;
	}

	public void move (int[] buf)
	{
		mSeq = buf;
		mSeqIndex = 0;

		nextSequence ();
	}

	public void spin ()
	{
		move (SEQ_SPIN);
	}

	private void nextSequence ()
	{
		int i = mSeqIndex++;

		if (i < mSeq.length)
		{
			Message msg = new Message ();

			msg.setTitle ("Instruction");
			msg.setContent (mSeq[i]);

			mMessenger.sendMessage (msg);
		}
		else
		{
			mSeq = null;

			if (mListener != null)
				mListener.onSequenceComplete ();
		}
	}

	public interface Listener
	{
		void onSequenceComplete ();
	}

	private final Messenger.Listener mRoboListener = new Messenger.Listener ()
	{
		@Override
		public void onConnect ()
		{

		}

		@Override
		public void onConnectError (IOException e)
		{

		}

		@Override
		public void onReceive (Message msg)
		{
			switch (msg.getTitle ())
			{
			case "OK":
				nextSequence ();
				break;
			}
		}

		@Override
		public void onReceiveError (IOException e)
		{

		}

		@Override
		public void onSend (Message msg)
		{

		}

		@Override
		public void onSendError (IOException e)
		{

		}

		@Override
		public void onDisconnect ()
		{

		}
	};
}
