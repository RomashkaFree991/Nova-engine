package com.nova.engine;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

final class AiClient {
    private AiClient(){}
    static String complete(JSONObject provider, JSONArray messages) throws Exception {
        String base=provider.optString("baseUrl","").trim();
        while(base.endsWith("/"))base=base.substring(0,base.length()-1);
        if(base.isEmpty())throw new Exception("Укажи Base URL провайдера.");
        String model=provider.optString("model","").trim();
        try{return request(base,model,messages,provider);}
        catch(HttpException e){
            String error=e.body.toLowerCase();
            boolean modelUnavailable=(e.code==404&&error.contains("model"))||(e.code==403&&(error.contains("no access")||error.contains("model")||error.contains("token")));
            if(!modelUnavailable)throw e;
            String fallback=findFirstModel(base,provider);
            if(fallback==null||fallback.isEmpty())throw new Exception("Модель «"+model+"» недоступна. Открой настройки ИИ и укажи модель из списка провайдера.");
            return request(base,fallback,messages,provider);
        }
    }
    private static String request(String base,String model,JSONArray messages,JSONObject provider)throws Exception{
        String endpoint=base.endsWith("/chat/completions")?base:base+"/chat/completions";
        JSONObject body=new JSONObject();body.put("model",model);body.put("messages",messages);body.put("temperature",0.25);
        HttpURLConnection c=(HttpURLConnection)new URL(endpoint).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(25000);c.setReadTimeout(120000);c.setDoOutput(true);auth(c,provider);
        c.setRequestProperty("Content-Type","application/json; charset=utf-8");
        try(OutputStream o=c.getOutputStream()){o.write(body.toString().getBytes("UTF-8"));}
        int code=c.getResponseCode();InputStream in=code>=400?c.getErrorStream():c.getInputStream();String response=in==null?"":read(in);c.disconnect();
        if(code==403&&response.toLowerCase().contains("trial_paused"))throw new Exception("Провайдер временно приостановил бесплатный trial. Это ограничение аккаунта/API, а не ошибка Nova. Используй доступный paid plan или другого провайдера.");
        if(code>=400)throw new HttpException(code,response);
        JSONObject root=new JSONObject(response);JSONArray choices=root.optJSONArray("choices");
        if(choices==null||choices.length()==0)throw new Exception("Провайдер вернул пустой ответ.");
        JSONObject msg=choices.optJSONObject(0).optJSONObject("message");if(msg==null)throw new Exception("В ответе нет message.");return msg.optString("content","");
    }
    private static String findFirstModel(String base,JSONObject provider){
        HttpURLConnection c=null;try{
            String endpoint=base.endsWith("/v1")?base+"/models":base+"/models";
            c=(HttpURLConnection)new URL(endpoint).openConnection();c.setRequestMethod("GET");c.setConnectTimeout(10000);c.setReadTimeout(15000);auth(c,provider);
            if(c.getResponseCode()>=400)return null;String json=read(c.getInputStream());JSONObject root=new JSONObject(json);JSONArray data=root.optJSONArray("data");
            if(data!=null&&data.length()>0)return data.optJSONObject(0).optString("id","");
        }catch(Exception ignored){}finally{if(c!=null)c.disconnect();}return null;
    }
    private static void auth(HttpURLConnection c,JSONObject p){String key=p.optString("apiKey","").trim();if(!key.isEmpty())c.setRequestProperty("Authorization","Bearer "+key);}
    private static String read(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);in.close();return new String(out.toByteArray(),"UTF-8");}
    private static final class HttpException extends Exception{final int code;final String body;HttpException(int c,String b){super("HTTP "+c+": "+b.substring(0,Math.min(350,b.length())));code=c;body=b;}}
}
