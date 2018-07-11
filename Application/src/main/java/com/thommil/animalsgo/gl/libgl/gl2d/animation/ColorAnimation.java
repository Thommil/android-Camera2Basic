package com.thommil.animalsgo.gl.libgl.gl2d.animation;

import com.thommil.animalsgo.gl.libgl.GlColor;
import com.thommil.animalsgo.gl.libgl.gl2d.Interpolation;

/**
 * Animation implementation based on interpolated vector translation.
 *
 * This implementation CAN be shared among actors.
 *
 * @author thommil on 4/19/16.
 */
public class ColorAnimation extends Animation<float[]> {

    protected int iteration = 0;

    private GlColor tmpKeyFrame;

    protected int lastIteration=0;

    /**
     * Simplified constructor (PlayMode NORMAL and Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param keyFrames     the objects representing the frames.
     */
    public ColorAnimation(float frameDuration, float[]... keyFrames) {
        super(frameDuration, keyFrames);
    }

    /**
     * Simplified constructor (Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param playMode      The animation playmode
     * @param keyFrames     the objects representing the frames.
     */
    public ColorAnimation(float frameDuration, PlayMode playMode, float[]... keyFrames) {
        super(frameDuration, playMode, keyFrames);
    }

    /**
     * *  Simplified constructor (PlayMode NORMAL)
     *
     * @param frameDuration the time between frames in seconds.
     * @param interpolator  The interpolator to use
     * @param keyFrames     the objects representing the frames.
     */
    public ColorAnimation(float frameDuration, Interpolation interpolator, float[]... keyFrames) {
        super(frameDuration, interpolator, keyFrames);
    }

    /**
     * Full constructor
     *
     * @param frameDuration the time between frames in seconds.
     * @param playMode      The animation playmode
     * @param interpolator  The interpolator to use
     * @param keyFrames     the objects representing the frames.
     */
    public ColorAnimation(float frameDuration, PlayMode playMode, Interpolation interpolator, float[]... keyFrames) {
        super(frameDuration, playMode, interpolator, keyFrames);
    }

    /**
     * Initialize the animation
     */
    @Override
    public void initialize() {
        this.tmpKeyFrame = new GlColor();
    }

    /**
     * Reset animation
     */
    @Override
    public void reset() {
        //this.tmpKeyFrame.set((this.playMode == PlayMode.LOOP_REVERSED || this.playMode == PlayMode.REVERSED) ? this.keyFrames[this.keyFrames.length - 1] : this.keyFrames[0]);
    }

    /**
     * Gets the object state at a given time
     *
     * @param stateTime The time of animation state in seconds
     * @return the object state at the given time
     */
    @Override
    public float[] getKeyFrame(float stateTime) {
        /*this.iteration = (int) (stateTime / this.animationDuration);
        final float interpolatedStateTime = this.interpolator.apply(0, this.animationDuration, (stateTime % this.animationDuration) / this.animationDuration);

        if (this.keyFrames.length == 1) return this.keyFrames[0];

        int keyIndex;
        switch (this.playMode) {
            case NORMAL:
                if (this.iteration == 0) {
                    keyIndex = (int) (interpolatedStateTime / this.frameDuration);
                    if(keyIndex < (this.keyFrames.length - 1)) {
                        this.tmpKeyFrame.set(this.keyFrames[keyIndex]);
                        return this.tmpKeyFrame.lerp(this.keyFrames[keyIndex+1], (interpolatedStateTime - (keyIndex * this.frameDuration)) / this.frameDuration);
                    }
                }
                return this.keyFrames[this.keyFrames.length - 1];
            case REVERSED:
                if (this.iteration == 0) {
                    keyIndex = this.keyFrames.length - (int)(interpolatedStateTime / this.frameDuration) - 1;
                    if(keyIndex > 0) {
                        this.tmpKeyFrame.set(this.keyFrames[keyIndex]);
                        return this.tmpKeyFrame.lerp(this.keyFrames[keyIndex-1], (interpolatedStateTime - ((this.keyFrames.length - keyIndex - 1) * this.frameDuration)) / this.frameDuration);
                    }
                }
                return this.keyFrames[0];
            case LOOP:
                keyIndex = (int) (interpolatedStateTime / this.frameDuration) % this.keyFrames.length;
                if(keyIndex < (this.keyFrames.length - 1)) {
                    this.tmpKeyFrame.set(this.keyFrames[keyIndex]);
                    return this.tmpKeyFrame.lerp(this.keyFrames[keyIndex+1], (interpolatedStateTime - (keyIndex * this.frameDuration)) / this.frameDuration);
                }
                else return this.keyFrames[this.keyFrames.length - 1];
            case LOOP_REVERSED:
                keyIndex = this.keyFrames.length - (int)(interpolatedStateTime / this.frameDuration) % this.keyFrames.length  - 1;
                if(keyIndex > 0) {
                    this.tmpKeyFrame.set(this.keyFrames[keyIndex]);
                    return this.tmpKeyFrame.lerp(this.keyFrames[keyIndex-1], (interpolatedStateTime - ((this.keyFrames.length - keyIndex - 1) * this.frameDuration)) / this.frameDuration);
                }
                else return this.keyFrames[0];
            case LOOP_PINGPONG:
                if (this.iteration % 2 == 0) {
                    keyIndex = (int) (interpolatedStateTime / this.frameDuration) % this.keyFrames.length;
                    if(keyIndex < (this.keyFrames.length - 1)) {
                        this.tmpKeyFrame.set(this.keyFrames[keyIndex]);
                        return this.tmpKeyFrame.lerp(this.keyFrames[keyIndex+1], (interpolatedStateTime - (keyIndex * this.frameDuration)) / this.frameDuration);
                    }
                    else return this.keyFrames[this.keyFrames.length - 1];
                } else {
                    keyIndex = this.keyFrames.length - (int)(interpolatedStateTime / this.frameDuration) % this.keyFrames.length  - 1;
                    if(keyIndex > 0) {
                        this.tmpKeyFrame.set(this.keyFrames[keyIndex]);
                        return this.tmpKeyFrame.lerp(this.keyFrames[keyIndex-1], (interpolatedStateTime - ((this.keyFrames.length - keyIndex - 1) * this.frameDuration)) / this.frameDuration);
                    }
                    else return this.keyFrames[0];
                }
            default:
                throw new RuntimeException(this.playMode.toString() + " playmode not supported");

        }*/
        return null;
    }
}
