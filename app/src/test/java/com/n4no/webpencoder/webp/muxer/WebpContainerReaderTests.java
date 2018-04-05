package com.n4no.webpencoder.webp.muxer;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class WebpContainerReaderTests {

    @Test
    public void readLosslessImage() throws Exception {
        File bananaFile = new File(getClass().getClassLoader().getResource("banana_lossless.webp").getPath());
        FileInputStream inputStream = new FileInputStream(bananaFile);
        WebpContainerReader reader = new WebpContainerReader(inputStream, true);

        reader.readHeader();

        WebpChunk chunk1 = reader.read();
        Assert.assertEquals(WebpChunkType.VP8X, chunk1.type);
        Assert.assertEquals(true, chunk1.hasAlpha);
        Assert.assertEquals(true, chunk1.hasAnim);
        Assert.assertEquals(990 - 1, chunk1.width);
        Assert.assertEquals(1050 - 1, chunk1.height);

        WebpChunk chunk2 = reader.read();
        Assert.assertEquals(WebpChunkType.ANIM, chunk2.type);
        Assert.assertEquals(0, chunk2.loops);

        WebpChunk chunk3 = reader.read();
        Assert.assertEquals(WebpChunkType.ANMF, chunk3.type);
        Assert.assertEquals(true, chunk3.isLossless);
        Assert.assertNotNull(chunk3.payload);

        while (reader.read() != null);

        reader.close();
    }

    @Test
    public void treeRewindTest() throws Exception {
        rewindTest("tree.webp");
    }

    @Test
    public void worldRewindTest() throws Exception {
        rewindTest("world.webp");
    }

    private void rewindTest(final String fileName) throws Exception {
        File treeFile = new File(getClass().getClassLoader().getResource(fileName).getPath());
        FileInputStream inputStream = new FileInputStream(treeFile);
        WebpContainerReader reader = new WebpContainerReader(inputStream, true);

        reader.readHeader();
        while (reader.read() != null);
        reader.close();
    }
}
