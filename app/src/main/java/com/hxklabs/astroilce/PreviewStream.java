package com.hxklabs.astroilce;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.hardware.CameraSequence;
import com.sony.scalar.hardware.DeviceBuffer;
import com.sony.scalar.hardware.DeviceMemory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PreviewStream {
    // Theses settings are only for programmatic preview stream and does not affect display
    // Max supported is probably 30 fps
    private static final int PREVIEW_FPS = 2 * 1000;
    private static final int PREVIEW_WIDTH = 640;
    // Set to zero for auto
    private static final int PREVIEW_HEIGHT = 480;
    // TODO: Supported format values
    private static final int PREVIEW_FORMAT = 256;
    private static final int PREVIEW_MAX_FRAME = 1;
    private static final int PREVIEW_JPEG_MAX_SIZE_KB = 200;
    // Larger number is higher compression. Tested values: 5, 8, 15
    private static final int PREVIEW_JPEG_COMPRESS_RATIO = 5;

    private static final ByteBuffer previewBuffer = ByteBuffer.allocateDirect(PREVIEW_JPEG_MAX_SIZE_KB * 1024);

    private final HandlerThread previewThread = new HandlerThread("PreviewThread");
    private Handler previewHandler;

    private CameraSequence cameraSequence;

    public interface PreviewStreamListener {
        void onPreviewProcessed(String message);
        void onPreviewError(String message);
    }
    private PreviewStreamListener listener;

    public void startPreviewProcessing(CameraEx cameraEx) {
        previewThread.start();
        previewHandler = new Handler(previewThread.getLooper());

        CameraSequence.Options options = new CameraSequence.Options();
        options.setOption(CameraSequence.Options.PREVIEW_FRAME_RATE, PREVIEW_FPS);
        options.setOption(CameraSequence.Options.PREVIEW_FRAME_WIDTH, PREVIEW_WIDTH);
        options.setOption(CameraSequence.Options.PREVIEW_FRAME_HEIGHT, PREVIEW_HEIGHT);
        options.setOption(CameraSequence.Options.PREVIEW_FRAME_FORMAT, PREVIEW_FORMAT);
        options.setOption(CameraSequence.Options.PREVIEW_FRAME_MAX_NUM, PREVIEW_MAX_FRAME);
        options.setOption(CameraSequence.Options.JPEG_COMPRESS_MAX_SIZE, PREVIEW_JPEG_MAX_SIZE_KB);
        options.setOption(CameraSequence.Options.JPEG_COMPRESS_RATE_DENOM, PREVIEW_JPEG_COMPRESS_RATIO);

        // Not supported in a6000
        //options.setOption(CameraSequence.Options.INTERIM_PRE_REVIEW_ENABLED, false);

        // JPEG magic parameters, not sure yet how these affect preview quality
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_1, -2147417855);
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_2, 131189);
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_3, 7100710);
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_4, 7962761);
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_5, 66052);
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_6, 16776944);
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_7, 513);
        //options.setOption(CameraSequence.Options.JPEG_COMPRESS_QUALITY_PARAM_8, -1667457892);

        try {
            cameraSequence = CameraSequence.open(cameraEx);
            cameraSequence.startPreviewSequence(options);
        } catch (Exception e) {
            Log.e("Error starting preview", e);
        }
    }

    public void stopPreviewProcessing() {
        previewHandler = null;
        previewThread.quit();

        try {
            cameraSequence.stopPreviewSequence();
        } catch (Exception e) {
            Log.e("Unable to stop preview sequence", e);
        }
        cameraSequence.release();
        cameraSequence = null;
    }

    public void processPreview() {
        if (previewHandler == null) {
            Log.e("PreviewStream: processPreview requested before handler is ready");
            return;
        }

        previewHandler.post(() -> {
            if (cameraSequence == null) {
                Log.e("CameraSequence not initialized before handling preview");
                return;
            }

            DeviceMemory[] memories = cameraSequence.getPreviewSequenceFrames(1);
            if (memories == null) {
                Log.e("getPreviewSequenceFrames returns null");
                if (listener != null) {
                    listener.onPreviewError("CamSeq preview - empty memories");
                }
                return;
            }

            DeviceBuffer deviceBuffer = (DeviceBuffer) memories[0];
            if (deviceBuffer == null) {
                Log.e("memories[0] from getPreviewSequenceFrames is null");
                if (listener != null) {
                    listener.onPreviewError("CamSeq preview - empty buffer");
                }
                return;
            }

            // Sony's DeviceBuffer does not follow java.nio channel semantic. The read(ByteBuffer, ...)
            // method does not update position and limit of the destination ByteBuffer. Only direct
            // buffers are supported.

            int size = deviceBuffer.getSize();
            Log.d(String.format("Received a preview sequence buffer with size=%d", size));

            if (size > previewBuffer.capacity()) {
                Log.w(String.format("Preview size over capacity: %d", size));
                size = previewBuffer.capacity();
            }
            previewBuffer.clear();
            deviceBuffer.read(previewBuffer, size, 0);
            releaseDeviceMemories(memories);
            previewBuffer.limit(size);

            File bufferDumpFile = new File(Environment.getExternalStorageDirectory(), "PREV.JPG");
            FileOutputStream fileOutputStream = null;
            FileChannel writeChannel = null;
            try {
                fileOutputStream = new FileOutputStream(bufferDumpFile, false);
                writeChannel = fileOutputStream.getChannel();
                while (previewBuffer.hasRemaining()) {
                    writeChannel.write(previewBuffer);
                }
                writeChannel.truncate(size);
                if (listener != null) {
                    listener.onPreviewProcessed(String.format("Stored a preview frame, size=%d", size));
                }
            } catch (IOException e) {
                Log.e("Failed dumping preview buffer", e);
            } finally {
                try {
                    if (writeChannel != null) {
                        writeChannel.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    Log.e("Failed closing file", e);
                }
            }
        });
    }

    public void setPreviewStreamListener(PreviewStreamListener listener) {
        this.listener = listener;
    }

    private static void releaseDeviceMemories(DeviceMemory[] memories) {
        for (DeviceMemory m: memories) {
            if (m != null) {
                m.release();
            }
        }
    }
}
