Sneaky WebP encoder for Android with animation support.

Project does't contain VP8 codec because Android from version 4.x supports encode to WebP (but without animation). So, how it works? Project contains WebP muxer, but compression is still job of Android:

     Bitmap.compress(Bitmap.CompressFormat.WEBP, ...

## How create animated WebP image?

    Bitmap frame1 = ...
    Bitmap frame2 = ...
    WebpBitmapEncoder encoder = new WebpBitmapEncoder("out.webp");  
    
    encoder.setLoops(0); // 0 = infinity.  
    encoder.setDuration(90);  
    encoder.writeFrame(frame1, 80);  
    encoder.setDuration(90);
    encoder.writeFrame(frame2, 80);
    encoder.close();

## License

WebpEncoder is open-sourced software licensed under the [MIT license](http://opensource.org/licenses/MIT).