package com.thommil.animalsgo.utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourcesLoader {

    public static JSONObject jsonFromAsset(final Context context, final String path) throws IOException, JSONException{
        JSONObject json = null;
        InputStream in = null;
        try{
            in = context.getAssets().open(path);;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            final StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                stringBuilder.append(line);
            }
            json = new JSONObject(stringBuilder.toString());
        }finally {
            if(in != null){
                in.close();
            }
        }
        return json;
    }
}
