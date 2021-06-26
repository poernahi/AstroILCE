package com.hxklabs.astroilce;

import android.view.KeyEvent;

public interface KeyListener {
    /**
     * Handle key up event
     * @param keyCode
     * @param event
     * @return true to consume event, false to pass the event to base class
     */
    boolean onKeyUp(int keyCode, KeyEvent event);

    /**
     * Handle key down event
     * @param keyCode
     * @param event
     * @return true to consume event, false to pass the event to base class
     */
    boolean onKeyDown(int keyCode, KeyEvent event);
}
