package org.kxml3.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StreamHelper
{
	public final static int	STREAM_BUFFER_LENGTH	= 128;

	public final static byte[] readAll(InputStream stream) throws IOException
	{
		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		write(ms, stream);

		byte[] result = ms.toByteArray();
		ms.close();
		ms = null;

		return result;
	}

	public final static int write(OutputStream os, InputStream ms, boolean isFlush)
			throws IOException
	{
		byte[] buf = new byte[STREAM_BUFFER_LENGTH];
		int len;
		int result = 0;
		while ((len = ms.read(buf)) != -1) {
			os.write(buf, 0, len);
			result += len;
		}

		if (isFlush) {
			os.flush();
		}

		buf = null;
		return result;
	}

	public final static int write(OutputStream os, InputStream ms) throws IOException
	{
		return write(os, ms, true);
	}
}
