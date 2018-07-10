package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;

public class GlDrawableBuffer<T> extends GlBuffer<T>{

    public GlDrawableBuffer(Chunk<T> ...chunks) {
        super(chunks);
    }

    public void draw(final GlProgram program) {
        switch (this.mode){
            case GlBuffer.MODE_VAO: {
                bind();

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.count);

                unbind();
                break;
            }
            case GlBuffer.MODE_VBO: {
                program.enableAttributes();

                this.bind();

                if(this.vertexAttribHandles != null) {
                    for (int index = 0; index < this.vertexAttribHandles.length; index++) {
                        GLES20.glVertexAttribPointer(this.vertexAttribHandles[index], this.chunks[index].components,
                                this.chunks[index].datatype, this.chunks[index].normalized, this.stride, this.chunks[index].offset);
                    }
                }

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.count);

                unbind();
                program.disableAttributes();
                break;
            }
            default: {
                program.enableAttributes();

                if(this.vertexAttribHandles != null) {
                    for (int index = 0; index < this.vertexAttribHandles.length; index++) {
                        this.buffer.position(this.chunks[index].position);
                        GLES20.glVertexAttribPointer(this.vertexAttribHandles[index], this.chunks[index].components,
                                this.chunks[index].datatype, this.chunks[index].normalized, this.stride, this.buffer);
                    }
                }

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.count);

                program.disableAttributes();
            }
        }
    }
}
