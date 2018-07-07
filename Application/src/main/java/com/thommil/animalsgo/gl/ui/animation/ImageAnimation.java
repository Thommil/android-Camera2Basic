package com.thommil.animalsgo.gl.ui.animation;

import com.thommil.animalsgo.gl.ui.libgdx.Interpolation;

/**
 * Animation implementation based on TextureRegion (sprite)
 *
 * This implementation CAN be shared among actors.
 *
 * @author thommil on 4/19/16.
 */
public class ImageAnimation extends Animation<ImageAnimation.KeyFrame> {

    protected int iteration = 0;

    /**
     * Simplified constructor (PlayMode NORMAL and Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param keyFrames     the objects representing the frames.
     */
    public ImageAnimation(float frameDuration, KeyFrame... keyFrames) {
        super(frameDuration, keyFrames);
    }

    /**
     * Simplified constructor (Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param playMode      The animation playmode
     * @param keyFrames     the objects representing the frames.
     */
    public ImageAnimation(float frameDuration, PlayMode playMode, KeyFrame... keyFrames) {
        super(frameDuration, playMode, keyFrames);
    }

    /**
     * *  Simplified constructor (PlayMode NORMAL)
     *
     * @param frameDuration the time between frames in seconds.
     * @param interpolator  The interpolator to use
     * @param keyFrames     the objects representing the frames.
     */
    public ImageAnimation(float frameDuration, Interpolation interpolator, KeyFrame... keyFrames) {
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
    public ImageAnimation(float frameDuration, PlayMode playMode, Interpolation interpolator, KeyFrame... keyFrames) {
        super(frameDuration, playMode, interpolator, keyFrames);
    }

    /**
     * Initialize the animation
     */
    @Override
    public void initialize() {
        this.iteration = 0;
    }

    /**
     * Reset animation
     */
    @Override
    public void reset() {
        this.iteration = 0;
    }

    /**
     * Gets the object state at a given time
     *
     * @param stateTime The time of animation state in seconds
     * @return the object state at the given time
     */
    @Override
    public KeyFrame getKeyFrame(float stateTime) {
        this.iteration = (int)(stateTime / this.animationDuration);
        final float interpolatedStateTime = this.interpolator.apply(0, this.animationDuration, (stateTime % this.animationDuration) / this.animationDuration);

        if (this.keyFrames.length == 1) return this.keyFrames[0];

        switch (this.playMode) {
            case NORMAL:
                if(this.iteration == 0) {
                    return this.keyFrames[Math.min(this.keyFrames.length - 1, (int) (interpolatedStateTime / this.frameDuration))];
                }
                return this.keyFrames[this.keyFrames.length - 1];
            case REVERSED:
                if(this.iteration == 0) {
                    return this.keyFrames[Math.max(this.keyFrames.length - (int)(interpolatedStateTime / this.frameDuration) - 1, 0)];
                }
                return this.keyFrames[0];
            case LOOP:
                return this.keyFrames[(int)(interpolatedStateTime / this.frameDuration) % this.keyFrames.length];
            case LOOP_REVERSED:
                return this.keyFrames[keyFrames.length - (int)(interpolatedStateTime / this.frameDuration) % this.keyFrames.length - 1];
            case LOOP_PINGPONG:
                if (this.iteration % 2 == 0) {
                    return this.keyFrames[(int) (interpolatedStateTime / this.frameDuration) % this.keyFrames.length];
                }
                else{
                    return this.keyFrames[keyFrames.length - (int)(interpolatedStateTime / this.frameDuration) % this.keyFrames.length - 1];
                }
            default:
                throw new RuntimeException(this.playMode.toString()+" playmode not supported");

        }
    }

    //TODO TextureRegion port
    //public static class KeyFrame extends TextureRegion{
    public static class KeyFrame{

        public float width;
        public float height;

        /**
         * @param texture The region texture
         * @param regionX The region X coord
         * @param regionY The region Y coord
         * @param regionWidth   The width of the texture region. May be negative to flip the sprite when drawn.
         * @param regionHeight  The height of the texture region. May be negative to flip the sprite when drawn.
         * @param width The key frame width
         * @param height The key frame height
         *
        public KeyFrame(GlTexture texture, int regionX, int regionY, int regionWidth, int regionHeight, float width, float height) {
            super(texture, regionX, regionY, regionWidth, regionHeight);
            this.width = width;
            this.height = height;
        }*/

    }
}
