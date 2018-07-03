package com.thommil.animalsgo.gl;


public abstract class PreviewPlugin extends Plugin {

    private static final String TAG = "A_GO/PreviewPlugin";

    @Override
    public int getType() {
        return TYPE_PREVIEW;
    }

    public abstract void draw(final int texId, final int width, final int height, final int orientation);
}
