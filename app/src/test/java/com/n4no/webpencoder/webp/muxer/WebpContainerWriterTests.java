package com.n4no.webpencoder.webp.muxer;

import com.n4no.webpencoder.webp.muxer.stream.MemorySeekableOutputStream;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class WebpContainerWriterTests {

    @Test
    public void treeTest() throws Exception {
        cloneTest("tree.webp");
    }

    @Test
    public void worldTest() throws Exception {
        cloneTest("world.webp");
    }

    private void cloneTest(final String fileName) throws Exception {
        String sourcePath = getClass().getClassLoader().getResource(fileName).getPath();
        File sourceFile = new File(sourcePath);
        byte[] sourceBytes = Files.readAllBytes(sourceFile.toPath());

        ByteArrayInputStream inputStream = new ByteArrayInputStream(sourceBytes);
        WebpContainerReader reader = new WebpContainerReader(inputStream, true);

        MemorySeekableOutputStream outputStream = new MemorySeekableOutputStream();
        WebpContainerWriter writer = new WebpContainerWriter(outputStream);

        reader.readHeader();
        writer.writeHeader();
        WebpChunk chunk;
        while ((chunk = reader.read()) != null)
            writer.write(chunk);

        reader.close();
        writer.close();

        byte[] outputBytes = outputStream.toArray();
        Assert.assertArrayEquals(sourceBytes, outputBytes);
    }
}
