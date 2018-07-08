package com.thommil.animalsgo.gl.ui;

import com.thommil.animalsgo.gl.libgl.GlProgram;
import com.thommil.animalsgo.gl.libgl.GlTexture;
import com.thommil.animalsgo.gl.ui.libgdx.Color;
import com.thommil.animalsgo.gl.ui.libgdx.Matrix4;

/**
 * Custom Batch for simple Sprite based on LibGDX SpriteBatch
 *
 * @author thommil on 03/02/16.
 */
public class SpriteBatchRenderer{

    //protected final Mesh mesh;
    protected final float[] mVertices;
    protected int mVerticesSize;
    //protected final GlProgram mProgram;

    protected int idx = 0;
    protected GlTexture mLastTexture = null;
    protected float color = Color.WHITE.toFloatBits();
    protected GlTexture mTmpTexture;

    protected final Matrix4 combinedMatrix = new Matrix4();

    /**
     * Default constructor
     *
     * @param size The batch size
     */
    public SpriteBatchRenderer(final int size) {
        // TODO build
        //this.mesh = createMesh(size);
        this.mVertices = createVertices(size);
        //this.mProgram = createShader();
    }

    /**
     * Called at beginning of rendering
     */
    public void begin() {
        //this.mProgram.use();
        // TODO set matrix
        // this.mProgram.setUniformMatrix("u_projectionViewMatrix", this.combinedMatrix);
    }

    /**
     * Called at ending of rendering
     */
    public void end() {
        if (this.idx > 0) flush();
    }

    /**
     * Sets the color of the Sprite
     *
     * @param color The color of the sprite
     */
    public void setColor (float color) {
        this.color = color;
    }

    public void draw(GlTexture texture, float[] vertices, int offset, int count) {
        int remainingVertices = this.mVertices.length;
        if (this.mLastTexture == null || texture != this.mLastTexture) {
            this.flush();
            this.mTmpTexture = texture;
            this.mLastTexture = mTmpTexture;
        }
        else {
            remainingVertices -= this.idx;
            if (remainingVertices == 0) {
                this.flush();
                remainingVertices = this.mVertices.length;
            }
        }
        this.copyAndFlush(vertices, Math.min(remainingVertices, count), offset, count);
    }

    private void copyAndFlush(float[] vertices, int copyCount, int offset, int count){
        System.arraycopy(vertices, offset, this.mVertices, this.idx, copyCount);
        this.idx += copyCount;
        count -= copyCount;
        while (count > 0) {
            offset += copyCount;
            this.flush();
            copyCount = Math.min(this.mVertices.length, count);
            System.arraycopy(vertices, offset, this.mVertices, 0, copyCount);
            this.idx += copyCount;
            count -= copyCount;
        }
    }

    /**
     * Flushes the batch and renders all remaining mVertices
     */
    public void flush () {
        if (this.idx == 0) return;

        final int count = this.idx / Sprite.SPRITE_SIZE * 6;

        // TODO draw code
        //this.mLastTexture.bind();
        //this.mesh.setVertices(this.mVertices, 0, this.idx);
        //this.mesh.getIndicesBuffer().position(0);
        //this.mesh.getIndicesBuffer().limit(count);

        //this.mesh.render(this.mProgram, GL20.GL_TRIANGLES, 0, count);

        this.idx = 0;
    }

    // TODO create GLBuffer
    /**
     * Subclasses should override this method to use their specific Mesh
     *
    protected  Mesh createMesh(final int size){
        // 32767 is max index, so 32767 / 6 - (32767 / 6 % 3) = 5460.
        if (size > 5460) throw new IllegalArgumentException("Can't have more than 5460 sprites per batch: " + size);

        Mesh.VertexDataType vertexDataType = Mesh.VertexDataType.VertexArray;
        if (Gdx.gl30 != null) {
            vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO;
        }
        final Mesh mesh = new Mesh(vertexDataType, false, size * 4, size * 6, new VertexAttribute(VertexAttributes.Usage.Position, 2,
                ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        final int len = size * 6;
        final short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 2);
            indices[i + 4] = (short)(j + 3);
            indices[i + 5] = j;
        }
        mesh.setIndices(indices);

        return mesh;

     public void render (ShaderProgram shader, int primitiveType, int offset, int count, boolean autoBind) {
     if (count == 0) return;

     if (autoBind) bind(shader);

     if (isVertexArray) {
     if (indices.getNumIndices() > 0) {
     ShortBuffer buffer = indices.getBuffer();
     int oldPosition = buffer.position();
     int oldLimit = buffer.limit();
     buffer.position(offset);
     buffer.limit(offset + count);
     Gdx.gl20.glDrawElements(primitiveType, count, GL20.GL_UNSIGNED_SHORT, buffer);
     buffer.position(oldPosition);
     buffer.limit(oldLimit);
     } else {
     Gdx.gl20.glDrawArrays(primitiveType, offset, count);
     }
     } else {
     if (indices.getNumIndices() > 0) {
     if (count + offset > indices.getNumMaxIndices()) {
     throw new GdxRuntimeException("Mesh attempting to access memory outside of the index buffer (count: "
     + count + ", offset: " + offset + ", max: " + indices.getNumMaxIndices() + ")");
     }

     Gdx.gl20.glDrawElements(primitiveType, count, GL20.GL_UNSIGNED_SHORT, offset * 2);
     } else {
     Gdx.gl20.glDrawArrays(primitiveType, offset, count);
     }
     }

     if (autoBind) unbind(shader);
     }
    }*/

    /**
     * Releases all resources of this object.
     */
    public void free() {
        //this.mProgram.free();
    }


    /**
     * Subclasses should override this method to use their specific mVertices
     */
    protected  float[] createVertices(final int size){
        this.mVerticesSize = Sprite.VERTEX_SIZE;
        return new float[size * Sprite.SPRITE_SIZE];
    }
}