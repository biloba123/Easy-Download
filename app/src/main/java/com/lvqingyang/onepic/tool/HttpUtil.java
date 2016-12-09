package com.lvqingyang.onepic.tool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LvQingYang
 * on 2016/10/22.
 */
public class HttpUtil {
    public static String sendHttpRequest(final String address)throws Exception{
        HttpURLConnection connection=null;
        try {
            URL url=new URL(address);
            connection=(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            InputStream in=connection.getInputStream();
            BufferedReader reader=new BufferedReader(new InputStreamReader(in));
            StringBuilder responseBuilder= new StringBuilder();
            String line;
            while ((line=reader.readLine())!=null){
                responseBuilder.append(line);
            }
            return responseBuilder.toString();
        } finally {
            if (connection!=null) {
               connection.disconnect();
            }
        }
    }
}
