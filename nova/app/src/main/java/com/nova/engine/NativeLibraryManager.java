package com.nova.engine;

import android.content.Context;
import android.os.Build;
import java.io.File;
import java.util.Arrays;

/** Finds a plugin built for a device-supported ABI and loads it from a Nova project's native folder. */
public final class NativeLibraryManager {
    private NativeLibraryManager(){}
    public static NativeComponent load(Context context,File projectDir,String libraryBaseName,String targetObject)throws Exception{
        if(libraryBaseName==null||!libraryBaseName.matches("[A-Za-z0-9_]+"))throw new Exception("Invalid native library name");
        File nativeDir=new File(projectDir,"native");
        String[] abis=Build.SUPPORTED_ABIS;
        for(String abi:abis){File candidate=new File(new File(nativeDir,abi),"lib"+libraryBaseName+".so");if(candidate.isFile())return NativeComponent.load(candidate,targetObject);}
        File legacy=new File(nativeDir,"lib"+libraryBaseName+".so");
        if(legacy.isFile())return NativeComponent.load(legacy,targetObject);
        throw new Exception("No lib"+libraryBaseName+".so for device ABIs "+Arrays.toString(abis)+". Put it under native/<abi>/.");
    }
}
