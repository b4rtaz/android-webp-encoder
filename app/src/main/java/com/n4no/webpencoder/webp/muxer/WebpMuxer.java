package com.n4no.webpencoder.webp.muxer;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class WebpMuxer {

    private WebpContainerWriter _writer;

    private boolean _isFirstFrame = true;
    private int _loops = -1;
    private int _duration = -1;
    private int _width;
    private int _height;

    public WebpMuxer(WebpContainerWriter writer) {
        _writer = writer;
    }

    public void setWidth(int width) {
        _width = width;
    }

    public void setHeight(int height) {
        _height = height;
    }

    public void setLoops(int loops) {
        _loops = loops;
    }

    public void setDuration(int duration) {
        _duration = duration;
    }

    public void writeFirstFrameFromWebm(InputStream inputStream) throws IOException {
        WebpContainerReader reader = new WebpContainerReader(inputStream, false);
        reader.readHeader();
        WebpChunk chunk = readFirstChunkWithPayload(reader);
        reader.close();

        writeFrame(chunk.payload, chunk.isLossless);
    }

    public void writeFrame(byte[] payload, boolean isLossless) throws IOException {
        if (_isFirstFrame) {
            _isFirstFrame = false;
            writeHeader();
        }

        if (hasAnim())
            writeAnmf(payload, isLossless);
        else
            writeVp8(payload, isLossless);
    }

    public void close() throws IOException {
        _writer.close();
    }

    //

    private boolean hasAnim() {
        return _loops >= 0 && _duration > 0;
    }

    private WebpChunk readFirstChunkWithPayload(WebpContainerReader reader) throws IOException {
        WebpChunk chunk;
        while ((chunk = reader.read()) != null) {
            if (chunk.payload != null)
                return chunk;
        }
        throw new IOException("Can not find chunk with payload.");
    }

    private void writeHeader() throws IOException {
        _writer.writeHeader();

        WebpChunk vp8x = new WebpChunk(WebpChunkType.VP8X);
        vp8x.hasAnim = hasAnim();
        vp8x.hasAlpha = false;
        vp8x.hasXmp = false;
        vp8x.hasExif = false;
        vp8x.hasIccp = false;
        vp8x.width = _width - 1;
        vp8x.height = _height - 1;
        _writer.write(vp8x);

        if (vp8x.hasAnim) {
            WebpChunk anim = new WebpChunk(WebpChunkType.ANIM);
            anim.background = -1;
            anim.loops = _loops;
            _writer.write(anim);
        }
    }

    private void writeAnmf(byte[] payload, boolean isLossless) throws IOException {
        WebpChunk anmf = new WebpChunk(WebpChunkType.ANMF);
        anmf.x = 0;
        anmf.y = 0;
        anmf.width = _width - 1;
        anmf.height = _height - 1;
        anmf.duration = _duration;

        anmf.isLossless = isLossless;
        anmf.payload = payload;

        anmf.useAlphaBlending = false;
        anmf.disposeToBackgroundColor = false;

        _writer.write(anmf);
    }

    private void writeVp8(byte[] payload, boolean isLossless) throws IOException {
        WebpChunk vp8 = new WebpChunk(isLossless
                ? WebpChunkType.VP8L
                : WebpChunkType.VP8);
        vp8.isLossless = isLossless;
        vp8.payload = payload;

        _writer.write(vp8);
    }
}
