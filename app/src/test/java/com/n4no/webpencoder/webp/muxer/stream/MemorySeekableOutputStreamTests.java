package com.n4no.webpencoder.webp.muxer.stream;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class MemorySeekableOutputStreamTests {

    @Test
    public void writeTest() throws Exception {
        MemorySeekableOutputStream s = new MemorySeekableOutputStream();

        s.write(new byte[] { 1, 2, 3 }, 3);

        byte[] b1 = s.toArray();

        Assert.assertEquals(1, b1[0]);
        Assert.assertEquals(2, b1[1]);
        Assert.assertEquals(3, b1[2]);

        s.write(new byte[] { 4, 5 }, 2);

        byte[] b2 = s.toArray();

        Assert.assertEquals(1, b1[0]);
        Assert.assertEquals(2, b1[1]);
        Assert.assertEquals(3, b1[2]);
        Assert.assertEquals(4, b2[3]);
        Assert.assertEquals(5, b2[4]);

        s.setPosition(2);
        s.write(new byte[] { 9, 10, 11 }, 2);

        byte[] b3 = s.toArray();

        Assert.assertEquals(1, b3[0]);
        Assert.assertEquals(2, b3[1]);
        Assert.assertEquals(9, b3[2]);
        Assert.assertEquals(10, b3[3]);
        Assert.assertEquals(5, b3[4]);
    }
}
