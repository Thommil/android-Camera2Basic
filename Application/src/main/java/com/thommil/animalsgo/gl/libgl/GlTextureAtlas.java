package com.thommil.animalsgo.gl.libgl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GlTextureAtlas {

    private static final String TAG = "A_GO/GlTextureAtlas";

    private final Map<String, SubTexture> mSubTextureMap = new HashMap<>();
    private GlTexture mTexture;
    private GlTexture mGlTextureTemplate;

    public GlTextureAtlas(){
        this(null);

    }

    public GlTextureAtlas(final GlTexture glTextureTemplate){
        mGlTextureTemplate = glTextureTemplate;
    }

    public GlTextureAtlas allocate(){
        //Log.d(TAG, "allocate()");
        if(mTexture != null){
            mTexture.bind().allocate(true).configure();
        }
        return this;
    }

    public GlTexture getTexture() {
        return mTexture;
    }

    public GlTextureAtlas free(){
        //Log.d(TAG, "free()");
        if(mTexture != null){
            mTexture.free();
        }
        return this;
    }

    public GlTextureAtlas parseJON(final Context context, final JSONObject json) throws JSONException{
        //Log.d(TAG, "parseJON("+json+")");
        final JSONObject meta = json.getJSONObject("meta");
        generateTexture(context, meta.getString("image"));

        final JSONObject sprites = json.getJSONObject("frames");
        final Iterator<String> names = sprites.keys();
        while(names.hasNext()){
            final String name = names.next();
            final JSONObject sprite = sprites.getJSONObject(name);
            final JSONObject frame = sprite.getJSONObject("frame");
            final int x = frame.getInt("x");
            final int y = frame.getInt("y");
            final int width = frame.getInt("w");
            final int height = frame.getInt("h");
            mSubTextureMap.put(name, new SubTexture(name, x, y, width, height));
        }

        return this;
    }

    private void generateTexture(final Context context, final String textureFile) {
        //Log.d(TAG, "generateTextureMapFromXml("+textureFile+")");
        InputStream in = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling
            if(textureFile == null){
                mTexture = new GLTextureDecorator(mGlTextureTemplate);
            }
            else {
                in = context.getResources().getAssets().open(textureFile);
                mTexture = new GLTextureDecorator(BitmapFactory.decodeStream(in, null, options), mGlTextureTemplate);
            }
        }catch(IOException ioe){
            throw new RuntimeException("Texture load error : " + textureFile);
        }finally {
            try{
                if(in != null) {
                    in.close();
                }
            }catch (IOException ioe){
                Log.e(TAG, ioe.toString());
            }
        }
    }

    public SubTexture getSubTexture(final String name){
        return mSubTextureMap.get(name);
    }

    public static class SubTexture {
        final public String name;
        final public int x;
        final public int y;
        final public int width;
        final public int height;

        public SubTexture(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static class GLTextureDecorator extends GlTexture{

        private final GlTexture mGlTextureTemplate;
        private Bitmap mImage;

        public GLTextureDecorator(){
            this(null, new GlTexture(){});
        }

        public GLTextureDecorator(final GlTexture glTextureTemplate){
            this(null, glTextureTemplate);
        }

        public GLTextureDecorator(final Bitmap image, final GlTexture glTextureTemplate){
            mGlTextureTemplate = glTextureTemplate;
            mImage = image;
        }

        @Override
        public Bitmap getBitmap() {
            if(mImage == null){
                final Bitmap image = mGlTextureTemplate.getBitmap();
                if(image == null){
                    throw new RuntimeException("Image not found in atlas nor texture template getBitmap() function");
                }
                return image;
            }
            return mImage;
        }

        @Override
        public int getHeight() {
            return mImage.getHeight();
        }

        @Override
        public int getWidth() {
            return mImage.getWidth();
        }

        @Override
        public int getTarget() {
            return mGlTextureTemplate.getTarget();
        }

        @Override
        public int getFormat() {
            return mGlTextureTemplate.getFormat();
        }

        @Override
        public int getType() {
            return mGlTextureTemplate.getType();
        }

        @Override
        public int getCompressionFormat() {
            return mGlTextureTemplate.getCompressionFormat();
        }

        @Override
        public int getWrapMode(int axeId) {
            return mGlTextureTemplate.getWrapMode(axeId);
        }

        @Override
        public int getMagnificationFilter() {
            return mGlTextureTemplate.getMagnificationFilter();
        }

        @Override
        public int getMinificationFilter() {
            return mGlTextureTemplate.getMinificationFilter();
        }

        @Override
        public int getSize() {
            return mImage.getByteCount();
        }

        @Override
        public int getLevel() {
            return mGlTextureTemplate.getLevel();
        }

        @Override
        public GlTexture free() {
            if(mImage != null && !mImage.isRecycled()){
                mImage.recycle();;
            }
            mImage = null;
            return super.free();
        }
    }
}
