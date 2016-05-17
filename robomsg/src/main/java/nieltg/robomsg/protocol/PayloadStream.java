package nieltg.robomsg.protocol;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PayloadStream
{
	public static final int MAX_PAYLOAD = 0xffff;

	private PayloadStream ()
	{
	}

	public static class Input extends FilterInputStream
	{
		public Input (InputStream in)
		{
			super (in);
		}

		public byte[] readPayload () throws IOException
		{
			int a = read ();
			if (a == -1) return null;

			int b = read ();
			if (b == -1) return null;

			int len = a + (b << 8);
			byte[] buf = new byte[len];

			int c = read (buf);
			if (c != len) return null;

			return buf;
		}
	}

	public static class Output extends FilterOutputStream
	{
		public Output (OutputStream out)
		{
			super (out);
		}

		public void writePayload (byte[] payload) throws IOException
		{
			int len = payload.length & MAX_PAYLOAD;
			if (len != payload.length)
				throw new IOException ("payload is too big");

			write (len);
			write (len >> 8);

			write (payload, 0, len);
		}
	}
}
