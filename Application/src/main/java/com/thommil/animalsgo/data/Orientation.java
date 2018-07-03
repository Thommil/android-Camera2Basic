package com.thommil.animalsgo.data;

import com.thommil.animalsgo.Settings;

/**
 * Orientation tooling (based on portrait mode only)
 */
public class Orientation {

    // Orientation values
    public static final int ORIENTATION_0 = 0;
    public static final int ORIENTATION_90 = 90;
    public static final int ORIENTATION_180 = 180;
    public static final int ORIENTATION_270 = 270;

    public static final int MODE_PORTRAIT = 0x01;
    public static final int MODE_LANDSCAPE = 0x02;

    private int mOrientation = ORIENTATION_0;
    private int mMode = MODE_PORTRAIT;
    private boolean mIsHorizontal = false;

    public Orientation(){
        mOrientation = ORIENTATION_0;
        mMode = MODE_PORTRAIT;
    }

    public Orientation(final int value){
        setValue(value);
    }

    public Orientation(final float x, final float y, final float z){
        setValue(x, y, z);
    }

    public int getOrientation(){
        return mOrientation;
    }

    public int getMode(){
        return mMode;
    }

    public void setValue(final int value){
        switch(value){
            case ORIENTATION_90 :
                mOrientation = ORIENTATION_90;
                mMode = MODE_LANDSCAPE;
                break;
            case ORIENTATION_180 :
                mOrientation = ORIENTATION_180;
                mMode = MODE_PORTRAIT;
                break;
            case ORIENTATION_270 :
                mOrientation = ORIENTATION_270;
                mMode = MODE_LANDSCAPE;
                break;
            default :
                mOrientation = ORIENTATION_0;
                mMode = MODE_PORTRAIT;
                break;
        }
    }

    public void setValue(final float x, final float y, final float z){
        final float absX = Math.abs(x);
        final float absY = Math.abs(y);
        final float absZ = Math.abs(z);

        if(mIsHorizontal && absZ < Settings.LANSCAPE_MODE_VERTICAL_TRESHOLDS[1]){
            mIsHorizontal = false;
        }
        else if(!mIsHorizontal && absZ > Settings.LANSCAPE_MODE_VERTICAL_TRESHOLDS[0]){
            mIsHorizontal = true;
        }
        //Horizontal --> landscape
        if(!mIsHorizontal){
            if(absY > absX){
                mMode = MODE_PORTRAIT;
                if(y > 0){
                    mOrientation = ORIENTATION_0;
                }
                else{
                    mOrientation = ORIENTATION_180;
                }
            }
            else{
                mMode = MODE_LANDSCAPE;
                if(x > 0){
                    mOrientation = ORIENTATION_90;
                }
                else{
                    mOrientation = ORIENTATION_270;
                }
            }
        }
    }

    public String toString(){
        return "Value : " + mOrientation + ", Mode : "+ ((mMode == MODE_PORTRAIT) ? "portrait":"landscape");
    }
}
