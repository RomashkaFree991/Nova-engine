package com.nova.engine;

import java.io.File;

/** Loads and drives one NDK component. The .so must export this class's JNI lifecycle bridge. */
public final class NativeComponent implements AutoCloseable {
    private long handle;
    private final String targetName;
    private volatile double rotationY;
    private NativeComponent(long value,String target){handle=value;targetName=target;}

    /** Loads an absolute path. Use NativeLibraryManager for ABI selection and project lookup. */
    public static NativeComponent load(File library,String targetName)throws Exception{
        if(library==null||!library.isFile())throw new Exception("Native .so not found");
        System.load(library.getAbsolutePath());
        String target=targetName==null?"":targetName;
        long h=nativeCreate(target);
        if(h==0)throw new Exception("Nova_CreateComponent failed or ABI mismatch");
        return new NativeComponent(h,target);
    }
    public synchronized double update(float deltaTime,double elapsedTime,long frame){
        if(handle!=0)rotationY=nativeUpdate(handle,deltaTime,elapsedTime,frame);
        return rotationY;
    }
    public String targetName(){return targetName;}
    public double rotationY(){return rotationY;}
    @Override public synchronized void close(){if(handle!=0){nativeDestroy(handle);handle=0;}}
    private static native long nativeCreate(String targetName);
    private static native double nativeUpdate(long handle,float deltaTime,double elapsedTime,long frame);
    private static native void nativeDestroy(long handle);
}
