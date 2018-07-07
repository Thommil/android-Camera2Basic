package com.thommil.animalsgo.gl.ui.animation;


import com.thommil.animalsgo.gl.ui.libgdx.Interpolation;
import com.thommil.animalsgo.gl.ui.libgdx.Vector2;

/**
 * Animation implementation based on interpolated vector scaling
 *
 * This implementation CANNOT be shared among actors.
 *
 * @author thommil on 4/19/16.
 */
public class ScaleAnimation extends Animation<ScaleAnimation.KeyFrame> {

    protected int iteration = 0;

    private KeyFrame scaleKeyFrame;
    private KeyFrame lastKeyFrame;
    private KeyFrame tmpKeyFrame;
    private KeyFrame[] inversedKeyFrames;

    protected int lastIteration=0;
    private int lastFrameNumber;

    /**
     * Simplified constructor (PlayMode NORMAL and Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param keyFrames     the objects representing the frames.
     */
    public ScaleAnimation(float frameDuration, KeyFrame... keyFrames) {
        super(frameDuration, keyFrames);
    }

    /**
     * Simplified constructor (Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param playMode      The animation playmode
     * @param keyFrames     the objects representing the frames.
     */
    public ScaleAnimation(float frameDuration, PlayMode playMode, KeyFrame... keyFrames) {
        super(frameDuration, playMode, keyFrames);
    }

    /**
     * *  Simplified constructor (PlayMode NORMAL)
     *
     * @param frameDuration the time between frames in seconds.
     * @param interpolator  The interpolator to use
     * @param keyFrames     the objects representing the frames.
     */
    public ScaleAnimation(float frameDuration, Interpolation interpolator, KeyFrame... keyFrames) {
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
    public ScaleAnimation(float frameDuration, PlayMode playMode, Interpolation interpolator, KeyFrame... keyFrames) {
        super(frameDuration, playMode, interpolator, keyFrames);
    }

    /**
     * Initialize the animation
     */
    @Override
    public void initialize() {
        this.iteration = 0;
        this.lastIteration = 0;
        this.scaleKeyFrame = new KeyFrame();
        this.tmpKeyFrame = new KeyFrame();
        this.lastKeyFrame = new KeyFrame();
        this.inversedKeyFrames = new KeyFrame[this.keyFrames.length];
        for(int inversedIndex=0, index = this.keyFrames.length - 1; inversedIndex < this.keyFrames.length; inversedIndex++, index--){
            this.inversedKeyFrames[inversedIndex] = new KeyFrame(-this.keyFrames[index].x, - this.keyFrames[index].y, this.keyFrames[index].interpolation);
        }
    }

    /**
     * Reset animation
     */
    @Override
    public void reset() {
        this.iteration = 0;
        this.lastIteration = 0;
        this.scaleKeyFrame.set(0,0);
        this.tmpKeyFrame.set(0,0);
        this.lastKeyFrame.set(0,0);
        this.lastFrameNumber = (this.playMode == PlayMode.LOOP_REVERSED || this.playMode == PlayMode.REVERSED) ? this.keyFrames.length - 1 : 0;
    }

    /**
     * Gets the object state at a given time
     *
     * @param stateTime The time of animation state in seconds
     * @return the object state at the given time
     */
    @Override
    public KeyFrame getKeyFrame(float stateTime) {
        this.scaleKeyFrame.set(0,0);
        this.iteration = (int)(stateTime / this.animationDuration);
        final float interpolatedStateTime = this.interpolator.apply(0, this.animationDuration, (stateTime % this.animationDuration) / this.animationDuration);

        int toIndex= 0;
        switch (this.playMode) {
            case NORMAL:
               if(this.iteration == 0) {
                   if (this.keyFrames.length > 1){
                       toIndex = Math.min(this.keyFrames.length - 1, (int) (interpolatedStateTime / this.frameDuration));
                       if(this.lastFrameNumber != toIndex){
                           this.lastKeyFrame.set(0,0);
                       }
                   }
                   this.scaleKeyFrame.interpolate(this.keyFrames[toIndex],(interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration, this.keyFrames[toIndex].interpolation);
                }
                else{
                   return this.scaleKeyFrame;
               }
                break;
            case REVERSED:
                if(this.iteration == 0) {
                    if (this.keyFrames.length > 1){
                        toIndex = Math.min(this.keyFrames.length - 1, (int) (interpolatedStateTime / this.frameDuration));
                        if(this.lastFrameNumber != toIndex){
                            this.lastKeyFrame.set(0,0);
                        }
                    }
                    this.scaleKeyFrame.interpolate(this.inversedKeyFrames[toIndex],(interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration, this.inversedKeyFrames[toIndex].interpolation);
                }
                else{
                    return this.scaleKeyFrame;
                }
                break;
            case LOOP:
                if (this.keyFrames.length > 1){
                    toIndex = (int) (interpolatedStateTime / this.frameDuration) % this.keyFrames.length;
                    if(this.lastFrameNumber != toIndex){
                        this.lastKeyFrame.set(0,0);
                    }
                }
                else if(this.lastIteration != this.iteration){
                    this.lastKeyFrame.set(0,0);
                }
                this.scaleKeyFrame.interpolate(this.keyFrames[toIndex],(interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration, this.keyFrames[toIndex].interpolation);
                break;
            case LOOP_REVERSED:
                if (this.keyFrames.length > 1){
                    toIndex = (int) (interpolatedStateTime / this.frameDuration) % this.keyFrames.length;
                    if(this.lastFrameNumber != toIndex){
                        this.lastKeyFrame.set(0,0);
                    }
                }
                else if(this.lastIteration != this.iteration){
                    this.lastKeyFrame.set(0,0);
                }
                this.scaleKeyFrame.interpolate(this.inversedKeyFrames[toIndex],(interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration, this.inversedKeyFrames[toIndex].interpolation);
                break;
            case LOOP_PINGPONG:
                if (this.keyFrames.length > 1){
                    toIndex = (int) (interpolatedStateTime / this.frameDuration) % this.keyFrames.length;
                    if(this.lastFrameNumber != toIndex){
                        this.lastKeyFrame.set(0,0);
                    }
                }
                else if(this.lastIteration != this.iteration){
                    this.lastKeyFrame.set(0,0);
                }
                if(this.iteration % 2 == 0){
                    this.scaleKeyFrame.interpolate(this.keyFrames[toIndex],(interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration, this.keyFrames[toIndex].interpolation);
                }
                else{
                    this.scaleKeyFrame.interpolate(inversedKeyFrames[toIndex],(interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration, this.inversedKeyFrames[toIndex].interpolation);
                }
                break;
            default:
                throw new RuntimeException(this.playMode.toString()+" playmode not supported");

        }

        this.tmpKeyFrame.set(this.scaleKeyFrame);
        this.scaleKeyFrame.sub(this.lastKeyFrame);
        this.lastKeyFrame.set(this.tmpKeyFrame);
        this.lastFrameNumber = toIndex;
        this.lastIteration = this.iteration;

        return this.scaleKeyFrame;
    }

    /**
     * Defines a keyframe for a TranslateAnimation
     */
    public static class KeyFrame extends Vector2{

        public Interpolation interpolation;

        /**
         * Constructs a new keyframe at (0,0) and linear interpolation
         */
        public KeyFrame() {
            super(0,0);
            this.interpolation = Interpolation.linear;
        }

        /**
         * Constructs a keyframe with the given components and interpolation
         *
         * @param x The x-component
         * @param y The y-component
         * @param interpolation The interpolation used for this keyframe
         */
        public KeyFrame(final float x, final float y, final Interpolation interpolation) {
            super(x, y);
            this.interpolation = interpolation;
        }

        /**
         * Constructs a vector from the given vector and interpolation
         *
         * @param v The vector
         * @param interpolation The interpolation used for this keyframe
         */
        public KeyFrame(Vector2 v, final Interpolation interpolation) {
            super(v);
            this.interpolation = interpolation;
        }
    }
}
