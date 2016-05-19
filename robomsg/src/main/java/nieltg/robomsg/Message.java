package nieltg.robomsg;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

import nieltg.robomsg.protocol.PayloadStream;

public class Message
{
	public static final byte[] MESSAGE_HEADER = { 0x01, 0x00, (byte)0x81, (byte)0x9E };
	public static final Charset ASCII_CHARSET = Charset.forName ("US-ASCII");

	public static final int MAX_TITLE   = 0xff;
	public static final int MAX_CONTENT = 0xffff;

	private byte[] mBufTitle;
	private byte[] mBufContent;

	public Message ()
	{
	}

	public Message (byte[] blob)
	{
		// Fixed: Use ByteOrder.LITTLE_ENDIAN to process EV3 message blobs
		ByteBuffer buf = ByteBuffer.wrap (blob).order (ByteOrder.LITTLE_ENDIAN);

		// Part 1: Message header

		byte[] magic = new byte[4];
		buf.get (magic);

		if (!Arrays.equals (magic, MESSAGE_HEADER))
			throw new IllegalArgumentException ("blob is not an EV3 message");

		// Part 2: Mailbox title

		int title_len = buf.get ();
		mBufTitle = new byte[title_len];

		buf.get (mBufTitle);

		// Part 3: Message content

		int content_len = buf.getShort () & MAX_CONTENT;
		mBufContent = new byte[content_len];

		buf.get (mBufContent);
	}

	public byte[] encode ()
	{
		byte[] buf_int = new byte[PayloadStream.MAX_PAYLOAD];
		ByteBuffer buf = ByteBuffer.wrap (buf_int).order (ByteOrder.LITTLE_ENDIAN);

		// Part 1: Message header

		buf.put (MESSAGE_HEADER);

		// Part 2: Mailbox title

		buf.put ((byte) mBufTitle.length);
		buf.put (mBufTitle);

		// Part 3: Message content

		buf.putShort ((short) mBufContent.length);
		buf.put (mBufContent);

		return Arrays.copyOfRange (buf_int, 0, buf.position ());
	}

	public void setTitle (String title)
	{
		byte[] src = title.getBytes (ASCII_CHARSET);

		int tmp = src.length + 1;
		int len = tmp & MAX_TITLE;
		if (len != tmp)
			throw new IllegalArgumentException ("title is too big");

		mBufTitle = new byte[len];
		System.arraycopy (src, 0, mBufTitle, 0, src.length);
	}

	public String getTitle ()
	{
		int len = mBufTitle.length - 1;

		// Fixed: Don't skip bytes in mBufTitle because
		// we don't put size parameter inside mBufTitle

		return new String (mBufTitle, 0, len, ASCII_CHARSET);
	}

	public void setContent (float data)
	{
		mBufContent = new byte[4];
		ByteBuffer.wrap (mBufContent).order (ByteOrder.LITTLE_ENDIAN).putFloat (data);
	}

	public float getNumberContent ()
	{
		return ByteBuffer.wrap (mBufContent).order (ByteOrder.LITTLE_ENDIAN).getFloat ();
	}

	public void setContent (String data)
	{
		byte[] src = data.getBytes (ASCII_CHARSET);

		int tmp = src.length + 1;
		int len = tmp & MAX_CONTENT;
		if (len != tmp)
			throw new IllegalArgumentException ("content is too big");

		mBufContent = new byte[len];
		System.arraycopy (src, 0, mBufContent, 0, src.length);
	}

	public String getTextContent ()
	{
		int len = mBufContent.length - 1;
		return new String (mBufContent, 0, len, ASCII_CHARSET);
	}

	public void setContent (boolean data)
	{
		mBufContent = new byte[1];
		mBufContent[0] = (byte) (data ? 1 : 0);
	}

	public boolean getLogicContent ()
	{
		return mBufContent[0] != 0;
	}
}
