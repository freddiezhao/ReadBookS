package com.sina.book.util;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public final class StreamHelper
{
	public interface IStreamProgress
	{
		public void onProgress(Object tag, int max, int value);
	}

	public final static int	STREAM_BUFFER_LENGTH	= 128;

	public final static short readShort(InputStream in) throws IOException
	{
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	public final static short readShortLH(InputStream in) throws IOException
	{
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return (short) ((ch1 & 0xff) | ((ch2 & 0xff) << 8));
	}

	public final static void writeShort(OutputStream os, short val) throws IOException
	{
		os.write((val >>> 8) & 0xFF);
		os.write((val >>> 0) & 0xFF);
	}

	public final static int readInt(InputStream in) throws IOException
	{

		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public final static int readIntLH(InputStream in) throws IOException
	{
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return (ch1 & 0xff) | ((ch2 & 0xff) << 8) | ((ch3 & 0xff) << 16) | ((ch4 & 0xff) << 24);
	}

	public final static boolean readBoolean(InputStream in) throws IOException
	{
		int c = in.read();
		return c != 0;
	}

	public final static void writeInt(OutputStream out, int v) throws IOException
	{
		out.write((v >>> 24) & 0xFF);
		out.write((v >>> 16) & 0xFF);
		out.write((v >>> 8) & 0xFF);
		out.write((v >>> 0) & 0xFF);
	}

	public final static String readString(InputStream in, String enc)
			throws IOException, UnsupportedEncodingException
	{

		int strLen = readShort(in);
		byte[] strContent = new byte[strLen];
		readBuffer(in, strContent, null, null);

		return new String(strContent, enc);
	}

	public final static void writeString(OutputStream out, String text, String enc)
			throws IOException, UnsupportedEncodingException
	{

		int strLen = text.length();
		writeShort(out, (short) strLen);

		byte[] strContent = text.getBytes(enc);
		write(out, strContent);
	}

	public final static byte[] readAll(InputStream stream) throws IOException
	{

		ByteArrayOutputStream ms = new ByteArrayOutputStream();
		write(ms, stream);

		byte[] result = ms.toByteArray();
		ms.close();
		ms = null;

		return result;
	}

	public final static int readBuffer(InputStream stream, byte[] buffer,
			IStreamProgress handler, Object tag) throws IOException
	{

		int offset = 0;
		int num = 0;
		int len = buffer.length;

		do {
			int bufferLen = len - offset;

			num = stream.read(buffer, offset, bufferLen);
			if (num <= 0)
				return offset;

			offset += num;

			if (handler != null) {
				handler.onProgress(tag, buffer.length, offset);
			}
		} while (offset < len);

		return len;
	}

	public final static byte[] readBuffer(InputStream stream, int len,
			IStreamProgress handler, Object tag) throws IOException
	{

		byte[] result = new byte[len];

		boolean success = readBuffer(stream, result, handler, tag) != 0;
		if (success) {
			return result;
		} else {
			return null;
		}
	}

	public final static int write(OutputStream stream, byte[] buffer)
			throws IOException
	{
		stream.write(buffer);
		stream.flush();

		return buffer.length;
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
