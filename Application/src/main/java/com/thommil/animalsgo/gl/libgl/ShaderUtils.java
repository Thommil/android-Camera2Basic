package com.thommil.animalsgo.gl.libgl;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderUtils
{

    private static final String TAG = "A_GO/ShaderUtils";

    public static String getStringFromFileInAssets(Context ctx, String filename) throws IOException {
        return getStringFromFileInAssets(ctx, filename, true);
    }

    public static String getStringFromFileInAssets(Context ctx, String filename, boolean useNewline) throws IOException
    {
        InputStream is = ctx.getAssets().open(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
        {
            builder.append(line + (useNewline ? "\n" : ""));
        }
        is.close();
        return builder.toString();
    }
}
