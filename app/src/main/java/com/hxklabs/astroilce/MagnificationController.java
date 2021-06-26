package com.hxklabs.astroilce;

import android.hardware.Camera;
import android.util.Pair;

import com.sony.scalar.hardware.CameraEx;

import java.util.List;

public class MagnificationController {

    // Sony API uses arbitrary magnification number. For a6000, 100 is 1x and 200 is approx 16x
    private int currentMagnification = 100;
    private List<Integer> supportedMagnification;

    private final CrosshairView crosshairView;

    private final CameraEx.PreviewMagnificationListener magnificationListener = new CameraEx.PreviewMagnificationListener() {
        @Override
        public void onChanged(boolean enabled, int magFactor, int magLevel, Pair center, CameraEx cameraEx) {
            Log.d(String.format("Current magnification factor = %d, level = %d", magFactor, magLevel));
            if (enabled) {
                currentMagnification = magFactor;
            } else {
                currentMagnification = 100;
            }
        }

        @Override
        public void onInfoUpdated(boolean b, Pair pair, CameraEx cameraEx) {

        }
    };

    public MagnificationController(CrosshairView crosshairView) {
        this.crosshairView = crosshairView;
    }

    public void attachListener(CameraEx cameraEx) {
        cameraEx.setPreviewMagnificationListener(magnificationListener);

        Camera.Parameters parameters = cameraEx.getSupportedParameters();
        CameraEx.ParametersModifier modifier = cameraEx.createParametersModifier(parameters);
        supportedMagnification = modifier.getSupportedPreviewMagnification();
    }

    public void toggleMagnification(CameraEx cameraEx) {
        Log.d("MagnificationController.toggleMagnification");
        if (cameraEx != null) {
            for (int mag: supportedMagnification) {
                if (mag > currentMagnification) {
                    Log.d(String.format("Setting magnification to %d", mag));
                    cameraEx.setPreviewMagnification(mag, new Pair<>(0, 0));
                    crosshairView.hideBox();
                    return;
                }
            }
            Log.d("Cancel magnification");
            cameraEx.stopPreviewMagnification();
            crosshairView.showBox();
        }
    }

}
