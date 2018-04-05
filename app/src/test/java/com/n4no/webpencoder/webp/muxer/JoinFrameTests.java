package com.n4no.webpencoder.webp.muxer;

import com.n4no.webpencoder.webp.muxer.stream.MemorySeekableOutputStream;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class JoinFrameTests {

    @Test
    public void joinTest() throws Exception {
        File frame1File = new File(getClass().getClassLoader().getResource("frame1.webp").getPath());
        File frame2File = new File(getClass().getClassLoader().getResource("frame2.webp").getPath());
        FileInputStream frame1Stream = new FileInputStream(frame1File);
        FileInputStream frame2Stream = new FileInputStream(frame2File);

        MemorySeekableOutputStream outputStream = new MemorySeekableOutputStream();
        // FileSeekableOutputStream outputStream = new FileSeekableOutputStream(new File("D:/joinframetest.webp"));
        WebpContainerWriter writer = new WebpContainerWriter(outputStream);
        WebpMuxer encoder = new WebpMuxer(writer);
        encoder.setWidth(400);
        encoder.setHeight(300);
        encoder.setLoops(0);

        encoder.setDuration(500);
        encoder.writeFirstFrameFromWebm(frame1Stream);

        encoder.setDuration(1000);
        encoder.writeFirstFrameFromWebm(frame2Stream);

        encoder.close();

        frame1Stream.close();
        frame2Stream.close();

        Assert.assertNotNull(outputStream.toArray());
    }
}
