package com.thommil.animalsgo.gl.ui;


import com.thommil.animalsgo.gl.libgl.GlFloatRect;
import com.thommil.animalsgo.gl.libgl.GlTexture;
import com.thommil.animalsgo.gl.ui.animation.ColorAnimation;
import com.thommil.animalsgo.gl.ui.animation.ImageAnimation;
import com.thommil.animalsgo.gl.ui.animation.RotateAnimation;
import com.thommil.animalsgo.gl.ui.animation.ScaleAnimation;
import com.thommil.animalsgo.gl.ui.animation.TranslateAnimation;
import com.thommil.animalsgo.gl.ui.libgdx.Color;
import com.thommil.animalsgo.gl.ui.libgdx.MathUtils;
import com.thommil.animalsgo.gl.ui.libgdx.NumberUtils;


public class Sprite{

    static public final int X1 = 0;
    static public final int Y1 = 1;
    static public final int C1 = 2;
    static public final int U1 = 3;
    static public final int V1 = 4;

    static public final int X2 = 5;
    static public final int Y2 = 6;
    static public final int C2 = 7;
    static public final int U2 = 8;
    static public final int V2 = 9;

    static public final int X3 = 10;
    static public final int Y3 = 11;
    static public final int C3 = 12;
    static public final int U3 = 13;
    static public final int V3 = 14;

    static public final int X4 = 15;
    static public final int Y4 = 16;
    static public final int C4 = 17;
    static public final int U4 = 18;
    static public final int V4 = 19;

    public static final int VERTEX_SIZE = 2 + 1 + 2;
    public static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

    public final int id;
    public GlTexture texture;
    public float x, y;
    public float u, v;
    public float u2, v2;
    public float width, height;
    public float originX, originY;
    public float rotation;
    public float scaleX = 1, scaleY = 1;
    public float color;
    public final Color tmpColor = new Color();
    public GlFloatRect bounds;

    protected int mRegionWidth, mRegionHeight;
    protected final float[] mVertices = new float[SPRITE_SIZE];
    protected boolean mDirty = true;

    public Sprite (final int id, final GlTexture texture) {
        this(id, texture, 0, 0, texture.getWidth(), texture.getHeight(), Math.abs(texture.getWidth()), Math.abs(texture.getHeight()));
    }
    public Sprite (final int id, final GlTexture texture, float width, float height) {
        this(id, texture, 0, 0, texture.getWidth(), texture.getHeight(), width, height);
    }

    public Sprite (final int id, final GlTexture texture, final int srcWidth, final int srcHeight) {
        this(id, texture, 0, 0, srcWidth, srcHeight, Math.abs(srcWidth), Math.abs(srcHeight));
    }

    public Sprite (final int id, final GlTexture texture, final int srcWidth, final int srcHeight, float width, float height) {
        this(id, texture, 0, 0, srcWidth, srcHeight, width, height);
    }

    public Sprite (final int id, final GlTexture texture, final int srcX, final int srcY, final int srcWidth, final int srcHeight) {
        this(id, texture, srcX, srcY, srcWidth, srcHeight, Math.abs(srcWidth), Math.abs(srcHeight));
    }

    public Sprite (final int id, final GlTexture texture, final int srcX, final int srcY, final int srcWidth, final int srcHeight, float width, float height){
        this.id = id;
        if (texture == null) throw new IllegalArgumentException("texture cannot be null.");
        this.texture = texture;
        this.setColor(Color.WHITE.toFloatBits());
        this.setRegion(srcX, srcY, srcWidth, srcHeight);
        this.setSize(width, height);
        this.setOrigin(width / 2, height / 2);
        this.setPosition(0,0);
    }

    /** Sets the position and size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale
     * are changed, it is slightly more efficient to set the bounds after those operations. */
    public void setBounds (float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (this.mDirty) return;

        final float x2 = x + width;
        final float y2 = y + height;
        this.mVertices[X1] = x;
        this.mVertices[Y1] = y;

        this.mVertices[X2] = x;
        this.mVertices[Y2] = y2;

        this.mVertices[X3] = x2;
        this.mVertices[Y3] = y2;

        this.mVertices[X4] = x2;
        this.mVertices[Y4] = y;

        if (this.rotation != 0 || this.scaleX != 1 || this.scaleY != 1) this.mDirty = true;
    }

    /** Sets the size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale are changed,
     * it is slightly more efficient to set the size after those operations. If both position and size are to be changed, it is
     * better to use {@link #setBounds(float, float, float, float)}. */
    public void setSize (float width, float height) {
        this.width = width;
        this.height = height;

        if (this.mDirty) return;

        final float x2 = this.x + width;
        final float y2 = this.y + height;
        this.mVertices[X1] = this.x;
        this.mVertices[Y1] = this.y;

        this.mVertices[X2] = this.x;
        this.mVertices[Y2] = y2;

        this.mVertices[X3] = x2;
        this.mVertices[Y3] = y2;

        this.mVertices[X4] = x2;
        this.mVertices[Y4] = this.y;

        if (this.rotation != 0 || this.scaleX != 1 || this.scaleY != 1) this.mDirty = true;
    }

    /** Sets the position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
     * to set the position after those operations. If both position and size are to be changed, it is better to use
     * {@link #setBounds(float, float, float, float)}. */
    public void setPosition (float x, float y) {
        translate(x - this.originX - this.x, y - this.originY - this.y);
    }

    /** Sets the position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
     * changed, it is slightly more efficient to translate after those operations. */
    public void translate (float xAmount, float yAmount) {
        this.x += xAmount;
        this.y += yAmount;

        if (this.mDirty) return;

        this.mVertices[X1] += xAmount;
        this.mVertices[Y1] += yAmount;

        this.mVertices[X2] += xAmount;
        this.mVertices[Y2] += yAmount;

        this.mVertices[X3] += xAmount;
        this.mVertices[Y3] += yAmount;

        this.mVertices[X4] += xAmount;
        this.mVertices[Y4] += yAmount;
    }

    /** Sets the origin in relation to the sprite's position for scaling and rotation. */
    public void setOrigin (float originX, float originY) {
        this.originX = originX;
        this.originY = originY;
        this.mDirty = true;
    }

    /** Place origin in the center of the sprite */
    public void setOriginCenter() {
        this.originX = this.width / 2;
        this.originY = this.height / 2;
        this.mDirty = true;
    }

    /** Sets the rotation of the sprite in degrees. Rotation is centered on the origin set in {@link #setOrigin(float, float)} */
    public void setRotation (float degrees) {
        this.rotation = degrees;
        this.mDirty = true;
    }

    /** Sets the rotation of the sprite in radiants. Rotation is centered on the origin set in {@link #setOrigin(float, float)} */
    public void setRotationRad (float radiants) {
        this.rotation = radiants * MathUtils.radDeg;
        this.mDirty = true;
    }

    /** @return the rotation of the sprite in degrees */
    public float getRotation () {
        return this.rotation;
    }

    /** @return the rotation of the sprite in radiants */
    public float getRotationRad () {
        return this.rotation / MathUtils.radDeg;
    }

    /** Sets the sprite's rotation in degrees relative to the current rotation. Rotation is centered on the origin set in
     * {@link #setOrigin(float, float)} */
    public void rotate (float degrees) {
        if (degrees == 0) return;
        this.rotation += degrees;
        this.mDirty = true;
    }

    /** Sets the sprite's rotation in radiants relative to the current rotation. Rotation is centered on the origin set in
     * {@link #setOrigin(float, float)} */
    public void rotateRad (float radiants) {
        if (radiants == 0) return;
        this.rotation += radiants * MathUtils.radDeg;
        this.mDirty = true;
    }

    /** Sets the sprite's scale for both X and Y uniformly. The sprite scales out from the origin. */
    public void setScale (float scaleXY) {
        this.scaleX = scaleXY;
        this.scaleY = scaleXY;
        this.mDirty = true;
    }

    /** Sets the sprite's scale for both X and Y. The sprite scales out from the origin.*/
    public void setScale (float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.mDirty = true;
    }

    /** Sets the sprite's scale relative to the current scale. for example: original scale 2 -> sprite.scale(4) -> final scale 6.
     * The sprite scales out from the origin.*/
    public void scale (float amount) {
        this.scaleX += amount;
        this.scaleY += amount;
        this.mDirty = true;
    }

    /** Sets the sprite's scale relative to the current scale. for example: original scale 2 -> sprite.scale(4) -> final scale 6.
     * The sprite scales out from the origin.*/
    public void scale (float xAmount, float yAmount) {
        this.scaleX += xAmount;
        this.scaleY += yAmount;
        this.mDirty = true;
    }

    /** Returns the packed mVertices, colors, and texture coordinates for this sprite. */
    public float[] getVertices () {
        if (this.mDirty) {
            this.mDirty = false;

            float localX = -this.originX;
            float localY = -this.originY;
            float localX2 = localX + this.width;
            float localY2 = localY + this.height;
            final float worldOriginX = this.x - localX;
            final float worldOriginY = this.y - localY;
            if (this.scaleX != 1 || this.scaleY != 1) {
                localX *= this.scaleX;
                localY *= this.scaleY;
                localX2 *= this.scaleX;
                localY2 *= this.scaleY;
            }
            if (this.rotation != 0) {
                final float cos = MathUtils.cosDeg(this.rotation);
                final float sin = MathUtils.sinDeg(this.rotation);
                final float localXCos = localX * cos;
                final float localXSin = localX * sin;
                final float localYCos = localY * cos;
                final float localYSin = localY * sin;
                final float localX2Cos = localX2 * cos;
                final float localX2Sin = localX2 * sin;
                final float localY2Cos = localY2 * cos;
                final float localY2Sin = localY2 * sin;

                final float x1 = localXCos - localYSin + worldOriginX;
                final float y1 = localYCos + localXSin + worldOriginY;
                this.mVertices[X1] = x1;
                this.mVertices[Y1] = y1;

                final float x2 = localXCos - localY2Sin + worldOriginX;
                final float y2 = localY2Cos + localXSin + worldOriginY;
                this.mVertices[X2] = x2;
                this.mVertices[Y2] = y2;

                final float x3 = localX2Cos - localY2Sin + worldOriginX;
                final float y3 = localY2Cos + localX2Sin + worldOriginY;
                this.mVertices[X3] = x3;
                this.mVertices[Y3] = y3;

                this.mVertices[X4] = x1 + (x3 - x2);
                this.mVertices[Y4] = y3 - (y2 - y1);
            } else {
                final float x1 = localX + worldOriginX;
                final float y1 = localY + worldOriginY;
                final float x2 = localX2 + worldOriginX;
                final float y2 = localY2 + worldOriginY;

                this.mVertices[X1] = x1;
                this.mVertices[Y1] = y1;

                this.mVertices[X2] = x1;
                this.mVertices[Y2] = y2;

                this.mVertices[X3] = x2;
                this.mVertices[Y3] = y2;

                this.mVertices[X4] = x2;
                this.mVertices[Y4] = y1;
            }
        }
        return this.mVertices;
    }

    public void setRegion (float u, float v, float u2, float v2) {
        final int texWidth = this.texture.getWidth(), texHeight = this.texture.getHeight();
        this.mRegionWidth = Math.round(Math.abs(u2 - u) * texWidth);
        this.mRegionHeight = Math.round(Math.abs(v2 - v) * texHeight);

        // For a 1x1 region, adjust UVs toward pixel center to avoid filtering artifacts on AMD GPUs when drawing very stretched.
        if (this.mRegionWidth == 1 && this.mRegionHeight == 1) {
            final float adjustX = 0.25f / texWidth;
            u += adjustX;
            u2 -= adjustX;
            final float adjustY = 0.25f / texHeight;
            v += adjustY;
            v2 -= adjustY;
        }

        this.u = u;
        this.v = v;
        this.u2 = u2;
        this.v2 = v2;

        this.mVertices[U1] = u;
        this.mVertices[V1] = v2;

        this.mVertices[U2] = u;
        this.mVertices[V2] = v;

        this.mVertices[U3] = u2;
        this.mVertices[V3] = v;

        this.mVertices[U4] = u2;
        this.mVertices[V4] = v2;
    }

    public void setU (float u) {
        this.u = u;
        this.mRegionWidth = Math.round(Math.abs(this.u2 - u) * this.texture.getWidth());
        this.mVertices[U1] = u;
        this.mVertices[U2] = u;
    }

    public void setV (float v) {
        this.v = v;
        this.mRegionHeight = Math.round(Math.abs(this.v2 - v) * this.texture.getHeight());
        this.mVertices[V2] = v;
        this.mVertices[V3] = v;
    }

    public void setU2 (float u2) {
        this.u2 = u2;
        this.mRegionWidth = Math.round(Math.abs(u2 - this.u) * this.texture.getWidth());
        this.mVertices[U3] = u2;
        this.mVertices[U4] = u2;
    }

    public void setV2 (float v2) {
        this.v2 = v2;
        this.mRegionHeight = Math.round(Math.abs(v2 - this.v) * this.texture.getHeight());
        this.mVertices[V1] = v2;
        this.mVertices[V4] = v2;
    }

    /** Set the sprite's flip state regardless of current condition
     * @param x the desired horizontal flip state
     * @param y the desired vertical flip state */
    public void setFlip (boolean x, boolean y) {
        flip((isFlipX() != x), (isFlipY() != y));
    }

    /** boolean parameters x,y are not setting a state, but performing a flip
     * @param x perform horizontal flip
     * @param y perform vertical flip */
    public void flip (boolean x, boolean y) {
        if (x) {
            float temp = this.u;
            this.u = this.u2;
            this.u2 = temp;
        }
        if (y) {
            float temp = this.v;
            this.v = this.v2;
            this.v2 = temp;
        }

        if (x) {
            float temp = this.mVertices[U1];
            this.mVertices[U1] = this.mVertices[U3];
            this.mVertices[U3] = temp;
            temp = this.mVertices[U2];
            this.mVertices[U2] = this.mVertices[U4];
            this.mVertices[U4] = temp;
        }
        if (y) {
            float temp = this.mVertices[V1];
            this.mVertices[V1] = this.mVertices[V3];
            this.mVertices[V3] = temp;
            temp = this.mVertices[V2];
            this.mVertices[V2] = this.mVertices[V4];
            this.mVertices[V4] = temp;
        }
    }

    public void scroll (float xAmount, float yAmount) {
        if (xAmount != 0) {
            final float u = (this.mVertices[U1] + xAmount) % 1;
            final float u2 = u + this.width / this.texture.getWidth();
            this.u = u;
            this.u2 = u2;
            this.mVertices[U1] = u;
            this.mVertices[U2] = u;
            this.mVertices[U3] = u2;
            this.mVertices[U4] = u2;
        }
        if (yAmount != 0) {
            final float v = (this.mVertices[V2] + yAmount) % 1;
            final float v2 = v + this.height / this.texture.getHeight();
            this.v = v;
            this.v2 = v2;
            this.mVertices[V1] = v2;
            this.mVertices[V2] = v;
            this.mVertices[V3] = v;
            this.mVertices[V4] = v2;
        }
    }

    /** @param width The width of the texture region. May be negative to flip the sprite when drawn.
     * @param height The height of the texture region. May be negative to flip the sprite when drawn. */
    public void setRegion (int x, int y, int width, int height) {
        final float invTexWidth = 1f / this.texture.getWidth();
        final float invTexHeight = 1f / this.texture.getHeight();
        this.setRegion(x * invTexWidth, y * invTexHeight, (x + width) * invTexWidth, (y + height) * invTexHeight);
        this.mRegionWidth = Math.abs(width);
        this.mRegionHeight = Math.abs(height);
    }

    public boolean isFlipX () {
        return this.u > this.u2;
    }

    public boolean isFlipY () {
        return this.v > this.v2;
    }

    public void setColor (Color color) {
        this.setColor(color.toFloatBits());
    }

    public void setColor (float r, float g, float b, float a) {
        int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
        this.setColor(NumberUtils.intToFloatColor(intBits));
    }

    public void setColor (float color) {
        this.color = color;
        this.mVertices[C1] = color;
        this.mVertices[C2] = color;
        this.mVertices[C3] = color;
        this.mVertices[C4] = color;
    }

    public Color getColor () {
        final int intBits = NumberUtils.floatToIntColor(color);
        this.tmpColor.r = (intBits & 0xff) / 255f;
        this.tmpColor.g = ((intBits >>> 8) & 0xff) / 255f;
        this.tmpColor.b = ((intBits >>> 16) & 0xff) / 255f;
        this.tmpColor.a = ((intBits >>> 24) & 0xff) / 255f;
        return this.tmpColor;
    }


    /**
     * Gets the bounding rectangle of this element for touch detection
     *
     * @return The bounding Rectangle
     */
    public GlFloatRect getBoundingRectangle() {
        float minx = this.mVertices[X1];
        float miny = this.mVertices[Y1];
        float maxx = this.mVertices[X1];
        float maxy = this.mVertices[Y1];

        minx = minx > this.mVertices[X2] ? this.mVertices[X2] : minx;
        minx = minx > this.mVertices[X3] ? this.mVertices[X3] : minx;
        minx = minx > this.mVertices[X4] ? this.mVertices[X4] : minx;

        maxx = maxx < this.mVertices[X2] ? this.mVertices[X2] : maxx;
        maxx = maxx < this.mVertices[X3] ? this.mVertices[X3] : maxx;
        maxx = maxx < this.mVertices[X4] ? this.mVertices[X4] : maxx;

        miny = miny > this.mVertices[Y2] ? this.mVertices[Y2] : miny;
        miny = miny > this.mVertices[Y3] ? this.mVertices[Y3] : miny;
        miny = miny > this.mVertices[Y4] ? this.mVertices[Y4] : miny;

        maxy = maxy < this.mVertices[Y2] ? this.mVertices[Y2] : maxy;
        maxy = maxy < this.mVertices[Y3] ? this.mVertices[Y3] : maxy;
        maxy = maxy < this.mVertices[Y4] ? this.mVertices[Y4] : maxy;

        if (this.bounds == null) this.bounds = new GlFloatRect();
        //this.bounds.x = minx;
        //this.bounds.y = miny;
        //this.bounds.width = maxx - minx;
        //this.bounds.height = maxy - miny;
        this.bounds.left = minx;
        this.bounds.bottom = miny;
        this.bounds.right = maxx;
        this.bounds.top = maxy;
        return this.bounds;
    }

    /**
     * Play a given image animation in current sprite actor at specified state time
     *
     * @param animation The image animation to use
     * @param stateTime The state time in seconds
     */
    public Sprite playAnimation(final ImageAnimation animation, final float stateTime){
        final ImageAnimation.KeyFrame keyFrame = animation.getKeyFrame(stateTime);
        this.setRegion(keyFrame.getU(), keyFrame.getV(), keyFrame.getU2(), keyFrame.getV2());
        if(keyFrame.width + keyFrame.height > 0) {
            this.setScale((keyFrame.width > 0) ? keyFrame.width / this.width : 1f, (keyFrame.height > 0) ? keyFrame.height / this.height : 1f);
        }
        return this;
    }

    /**
     * Play a given translate animation in current sprite actor at specified state time
     *
     * @param animation The translate animation to use
     * @param stateTime The state time in seconds
     */
    public Sprite playAnimation(final TranslateAnimation animation, final float stateTime){
        final TranslateAnimation.KeyFrame translation = animation.getKeyFrame(stateTime);
        this.translate(translation.x, translation.y);
        this.flip(translation.flipX ? !isFlipX() : false, translation.flipY ? !isFlipY() : false);
        return this;
    }

    /**
     * Play a given rotate animation in current sprite actor at specified state time
     *
     * @param animation The rotate animation to use
     * @param stateTime The state time in seconds
     */
    public Sprite playAnimation(final RotateAnimation animation, final float stateTime){
        final RotateAnimation.KeyFrame rotation = animation.getKeyFrame(stateTime);
        this.rotate(rotation.angle);
        return this;
    }

    /**
     * Play a given scale animation in current sprite actor at specified state time
     *
     * @param animation The scale animation to use
     * @param stateTime The state time in seconds
     */
    public Sprite playAnimation(final ScaleAnimation animation, final float stateTime){
        final ScaleAnimation.KeyFrame scale = animation.getKeyFrame(stateTime);
        this.scale(scale.x, scale.y);
        return this;
    }

    /**
     * Play a given color animation in current sprite actor at specified state time
     *
     * @param animation The color animation to use
     * @param stateTime The state time in seconds
     */
    public Sprite playAnimation(final ColorAnimation animation, final float stateTime){
        this.setColor(animation.getKeyFrame(stateTime));
        return this;
    }

}
