package com.n4no.webpencoder.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.n4no.webpencoder.R;
import com.n4no.webpencoder.webp.graphics.WebpBitmapEncoder;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements View.OnClickListener {

    private final static String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.encodeWebpButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.encodeWebpButton)
            onEncodeWebpButtonClicked();
    }

    public void onEncodeWebpButtonClicked() {
        Log.i(TAG, "Encode clicked.");
        encodeExamples();
    }

    private void encodeExamples() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap frame1 = BitmapFactory.decodeResource(getResources(), R.drawable.example_1, options);
        Bitmap frame2 = BitmapFactory.decodeResource(getResources(), R.drawable.example_2, options);
        File path = getNextFilePath();

        WebpBitmapEncoder encoder = null;
        try {
            encoder = new WebpBitmapEncoder(path);

            encoder.setLoops(0); // 0 = infinity.
            encoder.setDuration(90);

            encoder.writeFrame(frame1, 80);
            encoder.writeFrame(frame2, 80);
        }
        catch (IOException e) {
            Log.e(TAG, "Encoder failed.", e);
        }
        finally {
            try {
                if (encoder != null)
                    encoder.close();
            }
            catch (IOException e) {
                Log.e(TAG, "Can not close encoder.", e);
            }
        }

        Toast.makeText(getApplicationContext(), path.getName(), Toast.LENGTH_LONG).show();
    }

    private File getNextFilePath() {
        String env = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        String path = String.format("%s/webtest-%d.webp", env, System.currentTimeMillis());
        Log.d(TAG, path);
        return new File(path);
    }
}
