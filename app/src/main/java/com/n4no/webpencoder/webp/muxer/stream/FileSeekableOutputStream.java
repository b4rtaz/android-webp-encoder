package com.n4no.webpencoder.webp.muxer.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class FileSeekableOutputStream implements SeekableOutputStream {

    private final FileOutputStream _outputStream;

    public FileSeekableOutputStream(File file) throws FileNotFoundException {
        _outputStream = new FileOutputStream(file);
    }

    @Override
    public void setPosition(int position) throws IOException {
        _outputStream.getChannel().position(position);
    }

    @Override
    public void write(byte[] bytes, int length) throws IOException {
        _outputStream.write(bytes, 0, length);
    }

    public void close() throws IOException {
        _outputStream.close();
    }
}
