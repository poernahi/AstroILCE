package com.hxklabs.astroilce;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.sysutil.ScalarInput;
import com.sony.scalar.hardware.avio.DisplayManager;
import com.sony.scalar.sysutil.didep.Gpelibrary;

import java.io.IOException;

public class MainActivity extends Activity {
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private CameraEx camera;

    private DisplayManager displayManager;
    private SurfaceView surfaceView;
    private TextView batteryTextView;
    private TextView messageTextView;

    private final KeyListener defaultKeyListener = new KeyListener() {
        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            switch (event.getScanCode()) {
                case ScalarInput.ISV_KEY_DELETE:
                case ScalarInput.ISV_KEY_SK2:
                    onBackPressed();
                default:
                    return false;
            }
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return false;
        }
    };
    private KeyListener activeKeyListener = defaultKeyListener;

    private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d("MainActivity.surfaceCreated");
            try {
                Gpelibrary.changeFrameBufferPixel(Gpelibrary.GS_FRAMEBUFFER_TYPE.RGBA4444);
                Camera c = camera.getNormalCamera();
                c.setPreviewDisplay(surfaceHolder);
                Log.d("Starting preview");
                c.startPreview();
            } catch (IOException e) {
                Log.e(e.getMessage());
                messageTextView.setText("Camera preview error");
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            // STUB
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            // STUB
        }
    };

    private void disableAutoReview() {
        Log.d("MainActivity.disableAutoReview");
        if (camera != null) {
            CameraEx.AutoPictureReviewControl reviewControl = new CameraEx.AutoPictureReviewControl();
            camera.setAutoPictureReviewControl(reviewControl);
            reviewControl.setPictureReviewTime(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalExceptionHandler.install();
        Log.d("MainActivity.onCreate");
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        batteryTextView = (TextView) findViewById(R.id.batteryTextView);
        messageTextView = (TextView) findViewById(R.id.messageTextView);

        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity.onResume");
        camera = CameraEx.open(0, null);
        Log.d("Camera opened");
        surfaceView.getHolder().addCallback(surfaceHolderCallback);
        disableAutoReview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity.onPause");
        surfaceView.getHolder().removeCallback(surfaceHolderCallback);
        camera.getNormalCamera().stopPreview();
        camera.setAutoPictureReviewControl(null);
        camera.release();
        camera = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!activeKeyListener.onKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        } else {
            return true;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!activeKeyListener.onKeyUp(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        } else {
            return true;
        }
    }
}