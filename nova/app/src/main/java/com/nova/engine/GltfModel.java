package com.nova.engine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Minimal glTF 2.0 static triangle mesh loader: first node/mesh primitive, POSITION, UV0 and base-color image. */
final class GltfModel {
    final float[] vertices; // xyzuv interleaved
    final Bitmap texture;
    private GltfModel(float[] v,Bitmap b){vertices=v;texture=b;}
    static GltfModel load(File file)throws Exception{
        byte[] fileBytes=read(file);JSONObject json;byte[] bin=null;
        if(file.getName().toLowerCase().endsWith(".glb")){
            ByteBuffer b=ByteBuffer.wrap(fileBytes).order(ByteOrder.LITTLE_ENDIAN);if(b.remaining()<20||b.getInt()!=0x46546C67)throw new Exception("неверный GLB header");int version=b.getInt();if(version!=2)throw new Exception("поддерживается GLB 2.0");b.getInt();String jsonText=null;
            while(b.remaining()>=8){int len=b.getInt(),type=b.getInt();if(len<0||len>b.remaining())throw new Exception("повреждён GLB chunk");byte[] chunk=new byte[len];b.get(chunk);if(type==0x4E4F534A)jsonText=new String(chunk,"UTF-8").trim();else if(type==0x004E4942)bin=chunk;}
            if(jsonText==null)throw new Exception("JSON chunk отсутствует");json=new JSONObject(jsonText);
        }else json=new JSONObject(new String(fileBytes,"UTF-8"));
        JSONArray buffers=json.optJSONArray("buffers");byte[][] bufferData=new byte[buffers==null?0:buffers.length()][];
        for(int i=0;i<bufferData.length;i++){JSONObject desc=buffers.optJSONObject(i);String uri=desc==null?null:desc.optString("uri",null);if(uri==null||uri.isEmpty()){if(i==0&&bin!=null)bufferData[i]=bin;else throw new Exception("buffer без uri/GLB BIN");}else if(uri.startsWith("data:")){int comma=uri.indexOf(',');bufferData[i]=Base64.decode(uri.substring(comma+1),Base64.DEFAULT);}else bufferData[i]=read(new File(file.getParentFile(),uri));}
        JSONObject mesh=firstMesh(json);JSONArray primitives=mesh.optJSONArray("primitives");if(primitives==null||primitives.length()==0)throw new Exception("mesh has no primitives");JSONObject prim=primitives.getJSONObject(0);if(prim.optInt("mode",4)!=4)throw new Exception("поддерживаются только TRIANGLES primitives");JSONObject attrs=prim.getJSONObject("attributes");int posIndex=attrs.optInt("POSITION",-1);if(posIndex<0)throw new Exception("POSITION attribute missing");float[] pos=readFloatAccessor(json,bufferData,posIndex,3);int count=pos.length/3;
        float[] uv=new float[count*2];int uvIndex=attrs.optInt("TEXCOORD_0",-1);if(uvIndex>=0){float[] raw=readFloatAccessor(json,bufferData,uvIndex,2);System.arraycopy(raw,0,uv,0,Math.min(uv.length,raw.length));}
        int[] indices=prim.has("indices")?readIndices(json,bufferData,prim.getInt("indices")):null;if(indices==null){indices=new int[count];for(int i=0;i<count;i++)indices[i]=i;}
        int triCount=indices.length-(indices.length%3);float[] vertices=new float[triCount*5];int n=0;for(int i=0;i<triCount;i++){int ix=indices[i];if(ix<0||ix>=count)throw new Exception("index out of range");vertices[n++]=pos[ix*3];vertices[n++]=pos[ix*3+1];vertices[n++]=pos[ix*3+2];vertices[n++]=uv[ix*2];vertices[n++]=1f-uv[ix*2+1];}
        Bitmap texture=loadTexture(file,json,bufferData,prim);return new GltfModel(vertices,texture);
    }
    private static JSONObject firstMesh(JSONObject root)throws Exception{JSONArray nodes=root.optJSONArray("nodes"),meshes=root.optJSONArray("meshes");if(meshes==null||meshes.length()==0)throw new Exception("no meshes");if(nodes!=null)for(int i=0;i<nodes.length();i++){JSONObject n=nodes.optJSONObject(i);if(n!=null&&n.has("mesh"))return meshes.getJSONObject(n.getInt("mesh"));}return meshes.getJSONObject(0);}
    private static float[] readFloatAccessor(JSONObject root,byte[][] buffers,int accessorIndex,int components)throws Exception{JSONObject ac=root.getJSONArray("accessors").getJSONObject(accessorIndex);int viewIndex=ac.getInt("bufferView");JSONObject view=root.getJSONArray("bufferViews").getJSONObject(viewIndex);int bi=view.optInt("buffer",0),stride=view.optInt("byteStride",components*4),offset=view.optInt("byteOffset",0)+ac.optInt("byteOffset",0),count=ac.getInt("count"),type=ac.optInt("componentType",5126);if(type!=5126)throw new Exception("POSITION/UV must use float componentType");ByteBuffer b=ByteBuffer.wrap(buffers[bi]).order(ByteOrder.LITTLE_ENDIAN);float[] out=new float[count*components];for(int i=0;i<count;i++)for(int c=0;c<components;c++)out[i*components+c]=b.getFloat(offset+i*stride+c*4);return out;}
    private static int[] readIndices(JSONObject root,byte[][] buffers,int index)throws Exception{JSONObject ac=root.getJSONArray("accessors").getJSONObject(index);JSONObject view=root.getJSONArray("bufferViews").getJSONObject(ac.getInt("bufferView"));int bi=view.optInt("buffer",0),off=view.optInt("byteOffset",0)+ac.optInt("byteOffset",0),count=ac.getInt("count"),type=ac.getInt("componentType");ByteBuffer b=ByteBuffer.wrap(buffers[bi]).order(ByteOrder.LITTLE_ENDIAN);int[] out=new int[count];for(int i=0;i<count;i++){int at=off+i*(type==5121?1:type==5123?2:4);if(type==5121)out[i]=b.get(at)&255;else if(type==5123)out[i]=b.getShort(at)&65535;else if(type==5125)out[i]=b.getInt(at);else throw new Exception("unsupported index componentType "+type);}return out;}
    private static Bitmap loadTexture(File source,JSONObject root,byte[][] buffers,JSONObject primitive){try{int mat=primitive.optInt("material",-1);if(mat<0)return null;JSONObject material=root.getJSONArray("materials").optJSONObject(mat);JSONObject pbr=material==null?null:material.optJSONObject("pbrMetallicRoughness");JSONObject base=pbr==null?null:pbr.optJSONObject("baseColorTexture");if(base==null)return null;int textureIndex=base.optInt("index",-1);JSONArray textures=root.optJSONArray("textures");if(textures==null||textureIndex<0)return null;int imageIndex=textures.getJSONObject(textureIndex).optInt("source",-1);JSONArray images=root.optJSONArray("images");if(images==null||imageIndex<0)return null;JSONObject image=images.getJSONObject(imageIndex);String uri=image.optString("uri","");if(uri.startsWith("data:")){byte[] data=Base64.decode(uri.substring(uri.indexOf(',')+1),Base64.DEFAULT);return BitmapFactory.decodeByteArray(data,0,data.length);}if(!uri.isEmpty())return BitmapFactory.decodeFile(new File(source.getParentFile(),uri).getAbsolutePath());int viewIndex=image.optInt("bufferView",-1);if(viewIndex>=0){JSONObject view=root.getJSONArray("bufferViews").getJSONObject(viewIndex);byte[] data=buffers[view.optInt("buffer",0)];int off=view.optInt("byteOffset",0),len=view.getInt("byteLength");return BitmapFactory.decodeByteArray(data,off,len);}return null;}catch(Exception ignored){return null;}}
    private static byte[] read(File f)throws Exception{try(FileInputStream in=new FileInputStream(f);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}}
}
