package com.thommil.animalsgo.gl.libgl.gl2d.animation;

import com.thommil.animalsgo.gl.libgl.gl2d.Interpolation;

/**
 * Animation implementation based on interpolated vector translation
 *
 * This implementation CANNOT be shared among actors.
 *
 * @author thommil on 4/19/16.
 */
public class RotateAnimation extends Animation<RotateAnimation.KeyFrame> {

    protected int iteration = 0;

    private KeyFrame rotateKeyFrame;
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
    public RotateAnimation(float frameDuration, KeyFrame... keyFrames) {
        super(frameDuration, keyFrames);
    }

    /**
     * Simplified constructor (Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param playMode      The animation playmode
     * @param keyFrames     the objects representing the frames.
     */
    public RotateAnimation(float frameDuration, PlayMode playMode, KeyFrame... keyFrames) {
        super(frameDuration, playMode, keyFrames);
    }

    /**
     * *  Simplified constructor (PlayMode NORMAL)
     *
     * @param frameDuration the time between frames in seconds.
     * @param interpolator  The interpolator to use
     * @param keyFrames     the objects representing the frames.
     */
    public RotateAnimation(float frameDuration, Interpolation interpolator, KeyFrame... keyFrames) {
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
    public RotateAnimation(float frameDuration, PlayMode playMode, Interpolation interpolator, KeyFrame... keyFrames) {
        super(frameDuration, playMode, interpolator, keyFrames);
    }

    /**
     * Initialize the animation
     */
    @Override
    public void initialize() {
        this.iteration = 0;
        this.lastIteration = 0;
        this.rotateKeyFrame = new KeyFrame();
        this.tmpKeyFrame = new KeyFrame();
        this.lastKeyFrame = new KeyFrame();
        this.inversedKeyFrames = new KeyFrame[this.keyFrames.length];
        for(int inversedIndex=0, index = this.keyFrames.length - 1; inversedIndex < this.keyFrames.length; inversedIndex++, index--){
            this.inversedKeyFrames[inversedIndex] = new KeyFrame(360f - this.keyFrames[index].angle, this.keyFrames[index].interpolation);
        }
    }

    /**
     * Reset animation
     */
    @Override
    public void reset() {
        this.iteration = 0;
        this.lastIteration = 0;
        this.rotateKeyFrame.angle=0;
        this.tmpKeyFrame.angle=0;
        this.lastKeyFrame.angle=0;
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
        this.rotateKeyFrame.angle=0;
        this.iteration = (int)(stateTime / this.animationDuration);
        final float interpolatedStateTime = this.interpolator.apply(0, this.animationDuration, (stateTime % this.animationDuration) / this.animationDuration);

        int toIndex= 0;
        switch (playMode) {
            case NORMAL:
               if(this.iteration == 0) {
                   if (keyFrames.length > 1){
                       toIndex = Math.min(keyFrames.length - 1, (int) (interpolatedStateTime / frameDuration));
                       if(this.lastFrameNumber != toIndex){
                           this.lastKeyFrame.angle=0;
                       }
                   }
                   this.rotateKeyFrame.angle = keyFrames[toIndex].angle * keyFrames[toIndex].interpolation.apply((interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration);
                }
                else return this.rotateKeyFrame;
                break;
            case REVERSED:
                if(this.iteration == 0) {
                    if (keyFrames.length > 1){
                        toIndex = Math.min(keyFrames.length - 1, (int) (interpolatedStateTime / frameDuration));
                        if(this.lastFrameNumber != toIndex){
                            this.lastKeyFrame.angle=0;
                        }
                    }
                    this.rotateKeyFrame.angle = inversedKeyFrames[toIndex].angle * inversedKeyFrames[toIndex].interpolation.apply((interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration);
                }
                else return this.rotateKeyFrame;
                break;
            case LOOP:
                if (keyFrames.length > 1){
                    toIndex = (int) (interpolatedStateTime / frameDuration) % keyFrames.length;
                    if(this.lastFrameNumber != toIndex){
                        this.lastKeyFrame.angle=0;
                    }
                }
                else if(lastIteration != iteration){
                    this.lastKeyFrame.angle=0;
                }
                this.rotateKeyFrame.angle = keyFrames[toIndex].angle * keyFrames[toIndex].interpolation.apply((interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration);
                break;
            case LOOP_REVERSED:
                if (keyFrames.length > 1){
                    toIndex = (int) (interpolatedStateTime / frameDuration) % keyFrames.length;
                    if(this.lastFrameNumber != toIndex){
                        this.lastKeyFrame.angle=0;
                    }
                }
                else if(lastIteration != iteration){
                    this.lastKeyFrame.angle=0;
                }
                this.rotateKeyFrame.angle = inversedKeyFrames[toIndex].angle * inversedKeyFrames[toIndex].interpolation.apply((interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration);
                break;
            case LOOP_PINGPONG:
                if (keyFrames.length > 1){
                    toIndex = (int) (interpolatedStateTime / frameDuration) % keyFrames.length;
                    if(this.lastFrameNumber != toIndex){
                        this.lastKeyFrame.angle=0;
                    }
                }
                else if(lastIteration != iteration){
                    this.lastKeyFrame.angle=0;
                }
                if(this.iteration % 2 == 0){
                    this.rotateKeyFrame.angle = keyFrames[toIndex].angle * keyFrames[toIndex].interpolation.apply((interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration);
                }
                else{
                    this.rotateKeyFrame.angle = inversedKeyFrames[toIndex].angle * inversedKeyFrames[toIndex].interpolation.apply((interpolatedStateTime - (toIndex * this.frameDuration)) / this.frameDuration);
                }
                break;
            default:
                throw new RuntimeException(playMode.toString()+" playmode not supported");

        }

        this.tmpKeyFrame.angle = this.rotateKeyFrame.angle;
        this.rotateKeyFrame.angle =  this.rotateKeyFrame.angle - this.lastKeyFrame.angle;
        this.lastKeyFrame.angle = this.tmpKeyFrame.angle;
        this.lastFrameNumber = toIndex;
        this.lastIteration = iteration;

        return this.rotateKeyFrame;
    }

    /**
     * Defines a keyframe for a RotateAnimation
     */
    public static class KeyFrame{

        public float angle;
        public Interpolation interpolation;

        /**
         * Constructor with no rotation angle and LINEAR interpolation
         */
        public KeyFrame() {
            this(0, Interpolation.linear);
        }

        /**
         * Full constructor
         *
         * @param angle The roation angle in degrees
         * @param interpolation The interpolation to use
         */
        public KeyFrame(final float angle, final Interpolation interpolation) {
            this.angle = angle;
            this.interpolation = interpolation;
        }
    }
}
