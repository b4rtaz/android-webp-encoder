package com.n4no.webpencoder.webp.muxer;

/**
 * @author Bartlomiej Tadych, b4rtaz
 */
public class WebpChunk {

	public final WebpChunkType type;

	public int x;
	public int y;
	public int width;
	public int height;
	public int loops;
	public int duration;
	public int background;

	public byte[] payload;
	public boolean isLossless;

	public boolean hasAnim;
	public boolean hasXmp;
	public boolean hasExif;
	public boolean hasAlpha;
	public boolean hasIccp;

	public boolean useAlphaBlending;
	public boolean disposeToBackgroundColor;

	public WebpChunk(WebpChunkType t) {
		type = t;
	}
}
