package com.n4no.webpencoder.webp.graphics;

import android.graphics.Bitmap;

import com.n4no.webpencoder.webp.muxer.stream.FileSeekableOutputStream;
import com.n4no.webpencoder.webp.muxer.WebpContainerWriter;
import com.n4no.webpencoder.webp.muxer.WebpMuxer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class WebpBitmapEncoder {

    private final FileSeekableOutputStream _outputStream;
    private final WebpContainerWriter _writer;
    private final WebpMuxer _muxer;
    private boolean _isFirstFrame = true;

    public WebpBitmapEncoder(File file) throws FileNotFoundException {
        if (file == null) throw new NullPointerException("file");

        _outputStream = new FileSeekableOutputStream(file);
        _writer = new WebpContainerWriter(_outputStream);
        _muxer = new WebpMuxer(_writer);
    }

    public void setLoops(int loops) {
        _muxer.setLoops(loops);
    }

    public void setDuration(int duration) {
        _muxer.setDuration(duration);
    }

    public void writeFrame(Bitmap frame, int compress) throws IOException {
        if (frame == null) throw new NullPointerException("frame");

        if (_isFirstFrame) {
            _isFirstFrame = false;
            _muxer.setWidth(frame.getWidth());
            _muxer.setHeight(frame.getHeight());
        }

        ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
        frame.compress(Bitmap.CompressFormat.WEBP, compress, outBuffer);
        ByteArrayInputStream inBuffer = new ByteArrayInputStream(outBuffer.toByteArray());
        _muxer.writeFirstFrameFromWebm(inBuffer);
        outBuffer.close();
        inBuffer.close();
    }

    public void close() throws IOException {
        _muxer.close();
    }
}
