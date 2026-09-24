package com.nova.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/** OBJ triangle loader with a predictable memory guard and camera-friendly normalization. */
final class ObjModel {
    private static final int MAX_TRIANGLES=100000;
    private ObjModel(){}
    static float[] load(File file)throws Exception{
        ArrayList<float[]> positions=new ArrayList<>();ArrayList<Float> out=new ArrayList<>();
        try(BufferedReader r=new BufferedReader(new FileReader(file),32768)){
            String line;int lineNo=0,triangles=0;
            while((line=r.readLine())!=null){lineNo++;line=line.trim();
                if(line.startsWith("v ")){String[] p=line.substring(2).trim().split("\\s+");if(p.length<3)throw new Exception("строка "+lineNo+": vertex должен иметь x y z");positions.add(new float[]{Float.parseFloat(p[0]),Float.parseFloat(p[1]),Float.parseFloat(p[2])});}
                else if(line.startsWith("f ")){String[] p=line.substring(2).trim().split("\\s+");if(p.length<3)continue;int[] face=new int[p.length];for(int i=0;i<p.length;i++){String idx=p[i].split("/")[0];int n=Integer.parseInt(idx);face[i]=n>0?n-1:positions.size()+n;if(face[i]<0||face[i]>=positions.size())throw new Exception("строка "+lineNo+": индекс вершины вне файла");}for(int i=1;i<face.length-1;i++){if(++triangles>MAX_TRIANGLES)throw new Exception("OBJ слишком большой: максимум "+MAX_TRIANGLES+" треугольников");put(out,positions.get(face[0]));put(out,positions.get(face[i]));put(out,positions.get(face[i+1]));}}
            }
        }
        if(out.isEmpty())throw new Exception("OBJ не содержит граней (f)");
        float[] result=new float[out.size()];for(int i=0;i<result.length;i++)result[i]=out.get(i);normalize(result);return result;
    }
    private static void put(ArrayList<Float> out,float[] v){out.add(v[0]);out.add(v[1]);out.add(v[2]);}
    private static void normalize(float[] v){float minX=Float.MAX_VALUE,minY=Float.MAX_VALUE,minZ=Float.MAX_VALUE,maxX=-Float.MAX_VALUE,maxY=-Float.MAX_VALUE,maxZ=-Float.MAX_VALUE;for(int i=0;i<v.length;i+=3){minX=Math.min(minX,v[i]);maxX=Math.max(maxX,v[i]);minY=Math.min(minY,v[i+1]);maxY=Math.max(maxY,v[i+1]);minZ=Math.min(minZ,v[i+2]);maxZ=Math.max(maxZ,v[i+2]);}float cx=(minX+maxX)*.5f,cy=(minY+maxY)*.5f,cz=(minZ+maxZ)*.5f;float extent=Math.max(maxX-minX,Math.max(maxY-minY,maxZ-minZ));float scale=extent>0?2f/extent:1f;for(int i=0;i<v.length;i+=3){v[i]=(v[i]-cx)*scale;v[i+1]=(v[i+1]-cy)*scale;v[i+2]=(v[i+2]-cz)*scale;}}
}
