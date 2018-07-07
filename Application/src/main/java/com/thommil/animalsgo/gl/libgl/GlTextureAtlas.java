package com.thommil.animalsgo.gl.libgl;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;

import com.thommil.animalsgo.R;
import com.thommil.animalsgo.gl.libgl.GlTexture;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GlTextureAtlas {

    private static final String TAG = "A_GO/GlTextureAtlas";

    //private final Map<String, Sprite> mSpriteMap = new HashMap<>();
    private GlTexture mTexture;

    public GlTextureAtlas(final Context context, final int xmlResourceId, final int imageResourceId, final GlTexture glTextureTemplate){
        setSpriteMapFromId(context, xmlResourceId);
        setTextureFromId(context, imageResourceId, glTextureTemplate);
    }

    public void free(){
        if(mTexture != null){
            mTexture.free();
        }
    }

    private void setSpriteMapFromId(final Context context, final int xmlResourceId) {
        Log.d(TAG, "setSpriteMapFromId("+xmlResourceId+")");
        try {
            final XmlPullParser parser = context.getResources().getXml(xmlResourceId);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, null, "TextureAtlas");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if (name.equals("SubTexture")) {
                    addSprite(parser);
                }
                else{
                    skip(parser);
                }

            }
        }catch(IOException ioe){
            throw new RuntimeException("Failed to load xml atlas : " + ioe);
        }catch(XmlPullParserException xfpe){
            throw new RuntimeException("Failed to load xml atlas : " + xfpe);
        }
    }

    private void addSprite(final XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "SubTexture");
        final String name = parser.getAttributeValue(null, "name");

        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, null, "SubTexture");
    }
    /*

    <TextureAtlas imagePath="Untitled-1.png">
	<SubTexture name="20180519_134314.jpg instance 1" x="0" y="0" width="219" height="135" pivotX="0" pivotY="0"/>
     */


    private void skip(final XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private void setTextureFromId(final Context context, final int imageResourceId, final GlTexture glTextureTemplate) {
        Log.d(TAG, "setTextureFromId("+imageResourceId+")");
        InputStream in = null;
        try {
            in = context.getResources().openRawResource(imageResourceId);
            mTexture = new GLTextureDecorator(BitmapFactory.decodeStream(in), glTextureTemplate);
        }finally {
            try{
                in.close();
            }catch (IOException ioe){
                Log.e(TAG, ioe.toString());
            }
        }
    }


    private static class GLTextureDecorator extends GlTexture{

        private GlTexture mGlTextureTemplate;
        private Bitmap mImage;

        public GLTextureDecorator(final Bitmap image, final GlTexture glTextureTemplate){
            mGlTextureTemplate = glTextureTemplate;
            mImage = image;
        }



    }
}
