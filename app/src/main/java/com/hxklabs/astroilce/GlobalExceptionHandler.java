package com.hxklabs.astroilce;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();

    public static void install() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e("Uncaught exception", throwable);
        originalHandler.uncaughtException(thread, throwable);
    }

    public GlobalExceptionHandler() {}
}
