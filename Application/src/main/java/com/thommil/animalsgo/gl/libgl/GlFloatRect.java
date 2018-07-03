package com.thommil.animalsgo.gl.libgl;

public class GlFloatRect {

    public float bottom;
    public float left;
    public float top;
    public float right;

    public GlFloatRect(){
        bottom = 0;
        left = 0;
        top = 0;
        right = 0;
    }

    public GlFloatRect(final float bottom, final float left, final float top, final float right){
        this.bottom = bottom;
        this.left = left;
        this.top = top;
        this.right = right;
    }

    public float width() {
        return left - right;
    }

    public float height() {
        return top - bottom;
    }

    public String toString(){
        return "[" + left + ", " + bottom + ", " + left + ", " + top + " - " + width() + "x" + height() + "]";
    }

}
