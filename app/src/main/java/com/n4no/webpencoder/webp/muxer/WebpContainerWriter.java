package com.n4no.webpencoder.webp.muxer;

import android.util.Log;

import com.n4no.webpencoder.webp.muxer.stream.SeekableOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class WebpContainerWriter {

	private final SeekableOutputStream _outputStream;
	private int _offset;

	public WebpContainerWriter(SeekableOutputStream outputStream) {
		_outputStream = outputStream;
	}

	public void writeHeader() throws IOException {
		write(new byte[] { 'R', 'I', 'F', 'F' });
		writeUInt32(0);
		write(new byte[] { 'W', 'E', 'B', 'P' });
	}

	public void close() throws IOException {
		int fileSize = _offset - 8;
		_outputStream.setPosition(4);
		writeUInt32(fileSize);
	}

	public void write(WebpChunk chunk) throws IOException {
		System.out.println(chunk.type.toString());
		switch (chunk.type) {
		case VP8:
			writePayloadChunk(chunk, new byte[] { 'V', 'P', '8', ' ' });
			break;
		case VP8L:
			writePayloadChunk(chunk, new byte[] { 'V', 'P', '8', 'L' });
			break;
		case VP8X:
			writeVp8x(chunk);
			break;
		case ANIM:
			writeAnim(chunk);
			break;
		case ANMF:
			writeAnmf(chunk);
			break;
		default:
			throw new IOException("Not supported chunk type.");
		}
	}

	private void writePayloadChunk(WebpChunk chunk, byte[] fourCc) throws IOException {
		write(fourCc, 4);
		writeUInt32(chunk.payload.length);
		write(chunk.payload);
	}

	private void writeVp8x(WebpChunk chunk) throws IOException {
		write(new byte[] { 'V', 'P', '8', 'X' });
		writeUInt32(10);

		BitSet bs = new BitSet(32);

		bs.set(0, chunk.hasIccp);
		bs.set(4, chunk.hasAlpha);
		bs.set(2, chunk.hasExif);
		bs.set(3, chunk.hasXmp);
		bs.set(1, chunk.hasAnim);

		write(bitSetToBytes(bs, 4));
		writeUInt24(chunk.width);
		writeUInt24(chunk.height);
	}

	private void writeAnim(WebpChunk chunk) throws IOException {
		write(new byte[] { 'A', 'N', 'I', 'M' });
		writeUInt32(6);

		writeUInt32(chunk.background);
		writeUInt16(chunk.loops);
	}

	private void writeAnmf(WebpChunk chunk) throws IOException {
		write(new byte[] { 'A', 'N', 'M', 'F' });

		// if ALPH chunk present, get size
		int AlphaSize = 0;
		if(chunk.alphaData != null){
			AlphaSize = 4 + 4 + chunk.alphaData.length;
			// 4 bytes (ALPH header) + 4 bytes (chunk size) + length of Alpha BitStream
		}

		writeUInt32(chunk.payload.length + 24 + AlphaSize);

		writeUInt24(chunk.x); // 3 bytes (3)
		writeUInt24(chunk.y); // 3 bytes (6)
		writeUInt24(chunk.width); // 3 bytes (9)
		writeUInt24(chunk.height); // 3 bytes (12)
		writeUInt24(chunk.duration); // 3 bytes (15)

		BitSet bs = new BitSet(8);
		bs.set(1, chunk.useAlphaBlending);
		bs.set(0, chunk.disposeToBackgroundColor);
		write(bitSetToBytes(bs, 1)); // 1 byte (16)

		// Insert ALPH chunk
		if(chunk.alphaData != null){
			writeAlph(chunk.alphaData);
		}

		if (chunk.isLossless)
			write(new byte[] { 'V', 'P', '8', 'L' }); // 4 bytes (20)
		else
			write(new byte[] { 'V', 'P', '8', ' ' });
		writeUInt32(chunk.payload.length); // 4 bytes (24)
		write(chunk.payload);
	}

	private void writeAlph(byte[] alphaData) throws IOException {
		write(new byte[] { 'A', 'L', 'P', 'H' }); // 4
		writeUInt32(alphaData.length); // 4
		write(alphaData); // x
	}

	//

	private void write(byte[] bytes) throws IOException {
		write(bytes, bytes.length);
	}

	private void write(byte[] bytes, int length) throws IOException {
		_outputStream.write(bytes, length);
		_offset += length;
	}

	private void writeUInt(int value, int bytes) throws IOException {
		byte[] b = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
		write(b, bytes);
	}

	private void writeUInt16(int value) throws IOException {
		writeUInt(value, 2);
	}
	
	private void writeUInt24(int value) throws IOException {
		writeUInt(value, 3);
	}

	private void writeUInt32(int value) throws IOException {
		writeUInt(value, 4);
	}

	private byte[] bitSetToBytes(BitSet bs, int bytes) {
		byte[] b = new byte[bytes];
		byte[] a = bs.toByteArray();
		for (int i = 0; i < a.length; i++)
			b[i] = a[i];
		return b;
	}
}
