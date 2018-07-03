package com.thommil.animalsgo.gl;


public abstract class UIPlugin extends Plugin {

    private static final String TAG = "A_GO/UIPlugin";

    @Override
    public int getType() {
        return TYPE_UI;
    }

    //public abstract void draw(final int texId, final int width, final int height, final int orientation);
}
