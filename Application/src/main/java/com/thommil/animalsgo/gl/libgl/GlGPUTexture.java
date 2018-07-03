package com.thommil.animalsgo.gl.libgl;

import java.nio.ByteBuffer;

public class GlGPUTexture extends GlTexture {

    private int mWidth = 0;
    private int mHeight = 0;

    @Override
    public ByteBuffer getBytes() {
        return null;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getSize() {
        switch (this.getType()) {
            case TYPE_UNSIGNED_SHORT_4_4_4_4:
                return mHeight * mWidth * 4 * Short.BYTES;
            case TYPE_UNSIGNED_SHORT_5_5_5_1:
                return mHeight * mWidth * 4 * Short.BYTES;
            case TYPE_UNSIGNED_SHORT_5_6_5:
                return mHeight * mWidth * 3 * Short.BYTES;
            default:
                return mHeight * mWidth * 4 * Integer.BYTES;
        }
    }

    @Override
    public int getWrapMode(int axeId) {
        return WRAP_CLAMP_TO_EDGE;
    }

    public void setWidth(final int width) {
        mWidth = width;
    }

    public void setHeight(final int height) {
        mHeight = height;
    }
}
