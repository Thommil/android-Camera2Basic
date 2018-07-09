package com.thommil.animalsgo.gl.libgl.gl2d.animation;


import com.thommil.animalsgo.gl.libgl.gl2d.Interpolation;

/**
 * Animation base class (based on LibGDX Animation)
 *
 * @author thommil on 4/19/16.
 */
public abstract class Animation<T> {

    /**
     * Based on LibGDX Animation play modes
     */
    public enum PlayMode {
        NORMAL,
        REVERSED,
        LOOP,
        LOOP_REVERSED,
        LOOP_PINGPONG
    }

    final protected T[] keyFrames;

    protected float frameDuration;
    protected float animationDuration;
    protected PlayMode playMode;
    protected Interpolation interpolator;

    /**
     *  Simplified constructor (PlayMode NORMAL and Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param keyFrames the objects representing the frames.
     */
    public Animation(final float frameDuration, T... keyFrames) {
        this(frameDuration, PlayMode.NORMAL, Interpolation.linear, keyFrames);
    }

    /**
     *  Simplified constructor (Linear interpolation)
     *
     * @param frameDuration the time between frames in seconds.
     * @param playMode The animation playmode
     * @param keyFrames the objects representing the frames.
     */
    public Animation(final float frameDuration, final PlayMode playMode, T... keyFrames) {
        this(frameDuration, playMode, Interpolation.linear, keyFrames);
    }

    /**
     **  Simplified constructor (PlayMode NORMAL)
     *
     * @param frameDuration the time between frames in seconds.
     * @param interpolator The interpolator to use
     * @param keyFrames the objects representing the frames.
     */
    public Animation(float frameDuration, final Interpolation interpolator , T... keyFrames) {
        this(frameDuration, PlayMode.NORMAL, interpolator, keyFrames);
    }

    /**
     *  Full constructor
     *
     * @param frameDuration the time between frames in seconds.
     * @param playMode The animation playmode
     * @param interpolator The interpolator to use
     * @param keyFrames the objects representing the frames.
     */
    public Animation(float frameDuration, final PlayMode playMode, final Interpolation interpolator , T... keyFrames) {
        this.frameDuration = frameDuration;
        this.animationDuration = keyFrames.length * frameDuration;
        this.keyFrames = keyFrames;
        this.playMode = playMode;
        this.interpolator = interpolator;
        this.initialize();
    }

    /**
     * Initialize the animation
     */
    public abstract void initialize();

    /**
     * Gets the object state at a given time
     *
     * @param stateTime The time of animation state in seconds
     * @return the object state at the given time
     */
    public abstract T getKeyFrame (float stateTime);

    /**
     * Reset animation
     */
    public abstract void reset();

    /**
     * Gets the object state at the given index in key frames
     *
     * @param index The ly frame index
     *
     * @return the object state at the given index
     */
    public T getKeyFrame (int index){
        return this.keyFrames[index];
    }

    /** Whether the animation would be finished if played without looping (PlayMode#NORMAL), given the state time.
     * @param stateTime
     * @return whether the animation is finished. */
    public boolean isAnimationFinished (float stateTime) {
        int frameNumber = (int)(stateTime / this.frameDuration);
        return this.keyFrames.length - 1 < frameNumber;
    }

    /** Sets duration a frame will be displayed.
     * @param frameDuration in seconds */
    public void setFrameDuration (float frameDuration) {
        this.frameDuration = frameDuration;
        this.animationDuration = this.keyFrames.length * frameDuration;
    }

    /** @return the duration of a frame in seconds */
    public float getFrameDuration () {
        return this.frameDuration;
    }

    /** @return the duration of the entire animation, number of frames times frame duration, in seconds */
    public float getAnimationDuration () {
        return this.animationDuration;
    }

    public PlayMode getPlayMode() {
        return this.playMode;
    }

    public void setPlayMode(PlayMode playMode) {
        this.playMode = playMode;
    }

    public Interpolation getInterpolator() {
        return this.interpolator;
    }

    public void setInterpolator(Interpolation interpolator) {
        this.interpolator = interpolator;
    }
}
