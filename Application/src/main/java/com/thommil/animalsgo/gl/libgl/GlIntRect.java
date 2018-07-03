package com.thommil.animalsgo.gl.libgl;

public class GlIntRect{

    public int bottom;
    public int left;
    public int top;
    public int right;

    public GlIntRect(){
        bottom = 0;
        left = 0;
        top = 0;
        right = 0;
    }

    public GlIntRect(final int bottom, final int left, final int top, final int right){
        this.bottom = bottom;
        this.left = left;
        this.top = top;
        this.right = right;
    }

    public int width() {
        return right - left;
    }

    public int height() {
        return top - bottom;
    }

    public String toString(){
        return "[" + left + ", " + bottom + ", " + right + ", " + top + " - " + width() + "x" + height() + "]";
    }

}
