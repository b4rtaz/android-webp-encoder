package com.n4no.webpencoder.webp.muxer;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class WebpContainerReader {

	private final InputStream _inputStream;
	private final boolean _debug;
	private int _fileSize;
	private int _offset;

	public WebpContainerReader(InputStream inputStream, boolean debug) {
		_inputStream = inputStream;
		_debug = debug;
	}

	public void close() throws IOException {
	}

	public void readHeader() throws IOException {
		byte[] fcc = new byte[4];

		read(fcc, 4);
		if (!isFourCc(fcc, 'R', 'I', 'F', 'F'))
			throw new IOException("Expected RIFF file.");

		_fileSize = readUInt32() + 8 - 1;

		read(fcc, 4);
		if (!isFourCc(fcc, 'W', 'E', 'B', 'P'))
			throw new IOException("Expected Webp file.");
	}

	public WebpChunk read() throws IOException {
		byte[] fcc = new byte[4];

		if (read(fcc, 4) > 0) {
			if (isFourCc(fcc, 'V', 'P', '8', ' '))
				return readVp8();
			if (isFourCc(fcc, 'V', 'P', '8', 'L'))
				return readVp8l();
			if (isFourCc(fcc, 'V', 'P', '8', 'X'))
				return readVp8x();
			if (isFourCc(fcc, 'A', 'N', 'I', 'M'))
				return readAnim();
			if (isFourCc(fcc, 'A', 'N', 'M', 'F'))
				return readAnmf();
			if (isFourCc(fcc, 'I', 'C', 'C', 'P'))
				return readIccp();
			if (isFourCc(fcc, 'A', 'L', 'P', 'H'))
				return readAlph();

			throw new IOException(String.format("Not supported FourCC: %c.%c.%c.%c.",
					fcc[0], fcc[1], fcc[2], fcc[3]));
		}

		if (_fileSize != _offset)
			throw new IOException(String.format("Header has wrong file size: %d, expected: %d", 
					_fileSize, _offset));
		return null;
	}

	private WebpChunk readAlph() throws IOException {
		int chunkSize = readUInt32();
		WebpChunk chunk = new WebpChunk(WebpChunkType.ALPH);
		chunk.alphaData = readPayload(chunkSize);
		chunk.hasAlpha = true;
		return chunk;
	}

	private WebpChunk readIccp() throws IOException {
		int chunkSize = readUInt32();
		WebpChunk chunk = new WebpChunk(WebpChunkType.ICCP);

		readPayload(chunkSize);
		// no need to store the payload to this chunk as Animated Webp does not require ICCP

		return chunk;
	}

	private WebpChunk readVp8x() throws IOException {
		int chunkSize = readUInt32();
		if (chunkSize != 10)
			throw new IOException("Expected 10 bytes for VP8X.");

		WebpChunk chunk = new WebpChunk(WebpChunkType.VP8X);

		byte[] flags = new byte[4];
		read(flags, 4);
		BitSet bs = BitSet.valueOf(flags);

		bs.get(0); 					 // R reserved
		chunk.hasAnim	= bs.get(1); // A Animation
		chunk.hasXmp	= bs.get(2); // X XMP
		chunk.hasExif	= bs.get(3); // E Exif
		chunk.hasAlpha	= bs.get(4); // L Alpha
		chunk.hasIccp	= bs.get(5); // I ICCP

		chunk.width = readUInt24();
		chunk.height = readUInt24();

		debug(String.format("VP8X: size = %dx%d", chunk.width, chunk.height));
		return chunk;
	}

	private byte[] readPayload(int bytes) throws IOException {
		byte[] payload = new byte[bytes];
		if (read(payload, bytes) != bytes)
			throw new IOException("Can not read all bytes.");
		return payload;
	}

	private WebpChunk readVp8() throws IOException {
		int chunkSize = readUInt32();

		WebpChunk chunk = new WebpChunk(WebpChunkType.VP8);
		chunk.isLossless = false;
		chunk.payload = readPayload(chunkSize);

		debug(String.format("VP8: bytes = %d", chunkSize));
		return chunk;
	}

	private WebpChunk readVp8l() throws IOException {
		int chunkSize = readUInt32();

		WebpChunk chunk = new WebpChunk(WebpChunkType.VP8L);
		chunk.isLossless = true;
//		chunkSize is not telling the correct size of payload to read.
//		chunk.payload = readPayload(chunkSize);
//		So reading all bytes remained (Max 1024 bytes)
		chunk.payload = readAllBytes();

		debug(String.format("VP8L: bytes = %d", chunkSize));
		return chunk;
	}

	private WebpChunk readAnim() throws IOException {
		int chunkSize = readUInt32();
		if (chunkSize != 6)
			throw new IOException("Expected 6 bytes for ANIM.");

		WebpChunk chunk = new WebpChunk(WebpChunkType.ANIM);
		chunk.background = readUInt32();
		chunk.loops = readUInt16();

		debug(String.format("ANIM: loops = %d", chunk.loops));
		return chunk;
	}

	private WebpChunk readAnmf() throws IOException {
		int chunkSize = readUInt32();

		WebpChunk chunk = new WebpChunk(WebpChunkType.ANMF);
		chunk.x = readUInt24();
		chunk.y = readUInt24();
		chunk.width = readUInt24();
		chunk.height = readUInt24();
		chunk.duration = readUInt24();

		byte[] flags = new byte[1];
		read(flags, 1);
		BitSet bs = BitSet.valueOf(flags);
		chunk.useAlphaBlending = bs.get(1);
		chunk.disposeToBackgroundColor = bs.get(0);

		byte[] cch = new byte[4];
		read(cch, 4);
		if (isFourCc(cch, 'V', 'P', '8', 'L'))
			chunk.isLossless = true;
		else if (isFourCc(cch, 'V', 'P', '8', ' '))
			chunk.isLossless = false;
		else
			throw new IOException("Not supported ANMF payload.");

		readUInt32(); // Payload size.
		int payloadSize = chunkSize - 24;
		chunk.payload = readPayload(payloadSize);

		debug(String.format("ANMF: size = %dx%d, offset = %dx%d, duration = %d, bytes = %d",
				chunk.width, chunk.height, chunk.x, chunk.y, chunk.duration, payloadSize));
		return chunk;
	}

	//

	private final int read(byte[] buffer, int bytes) throws IOException {
		int count = _inputStream.read(buffer, 0, bytes);
		_offset += count;
		return count;
	}

	private final int readUint(int bytes) throws IOException {
		byte[] b = new byte[] { 0, 0, 0, 0 };
		read(b, bytes);
		return ByteBuffer.wrap(b, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	private final int readUInt32() throws IOException {
		return readUint(4);
	}

	private final int readUInt24() throws IOException {
		return readUint(3);
	}

	private final int readUInt16() throws IOException {
		return readUint(2);
	}

	private boolean isFourCc(byte[] h, char a, char b, char c, char d) {
		return h[0] == a && h[1] == b && h[2] == c && h[3] == d;
	}

	private byte[] readAllBytes() throws IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int numRead;
			while ((numRead = _inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, numRead);
				_offset += numRead;
			}
			return outputStream.toByteArray();
		}
	}
	private void debug(String message) {
		if (_debug)
			System.out.println(message);
	}
}
