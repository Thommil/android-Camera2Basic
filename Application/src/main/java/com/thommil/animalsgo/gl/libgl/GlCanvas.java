package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;

public class GlCanvas {

    public static void drawArrays(final GlProgram program, final GlBuffer data){
        drawArrays(program, data, null);
    }

    public static void drawArrays(final GlProgram program, final GlBuffer data, final int[] attributes){
        switch (data.mode){
            case GlBuffer.MODE_VAO: {
                data.bind();

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, data.count);

                data.unbind();
                break;
            }
            case GlBuffer.MODE_VBO: {
                final boolean useAllAttributes = (attributes == null);
                if (useAllAttributes) {
                    program.enableAttributes();
                } else {
                    for (final int attribute : attributes) {
                        program.enableAttribute(attribute);
                    }
                }
                data.bind();

                int chunkIndex = 0;
                for(final GlBuffer.Chunk chunk : data.chunks){
                    if (useAllAttributes) {
                        GLES20.glVertexAttribPointer(chunk.handle, data.chunks[chunkIndex].components,
                                data.datatype, false, data.stride, data.chunks[chunkIndex].offset);
                    }
                    else{
                        GLES20.glVertexAttribPointer(attributes[chunkIndex], data.chunks[chunkIndex].components,
                                data.datatype, false, data.stride, data.chunks[chunkIndex].offset);
                    }
                    chunkIndex++;
                }

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, data.count);

                data.unbind();
                if (useAllAttributes) {
                    program.disableAttributes();
                } else {
                    for (final int attribute : attributes) {
                        program.disableAttribute(attribute);
                    }
                }
                break;
            }
            default: {
                final boolean useAllAttributes = (attributes == null);
                if (useAllAttributes) {
                    program.enableAttributes();
                } else {
                    for (final int attribute : attributes) {
                        program.enableAttribute(attribute);
                    }
                }

                int chunkIndex = 0;
                for(final GlBuffer.Chunk chunk : data.chunks){
                    data.buffer.position(data.chunks[chunkIndex].position);
                    if (useAllAttributes) {
                        GLES20.glVertexAttribPointer(chunk.handle, data.chunks[chunkIndex].components,
                                data.datatype, false, data.stride, data.buffer);
                    }
                    else{
                        GLES20.glVertexAttribPointer(attributes[chunkIndex], data.chunks[chunkIndex].components,
                                data.datatype, false, data.stride, data.buffer);
                    }
                    chunkIndex++;
                }

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, data.count);

                if (useAllAttributes) {
                    program.disableAttributes();
                } else {
                    for (final int attribute : attributes) {
                        program.disableAttribute(attribute);
                    }
                }
            }
        }
    }

    /*
    mProgram.use().enableAttributes();
        mCameraTexture.bind();

        mSquareImageBuffer.buffer.rewind();
        GLES20.glVertexAttribPointer(mPositionAttributeHandle, mSquareImageBuffer.chunks[0].components,
                mSquareImageBuffer.datatype, false, mSquareImageBuffer.stride, mSquareImageBuffer.buffer);
        mSquareImageBuffer.buffer.position(mSquareImageBuffer.chunks[1].position);
        GLES20.glVertexAttribPointer(mTextureCoordinatesAttributeHandle, mSquareImageBuffer.chunks[1].components,
                mSquareImageBuffer.datatype, false, mSquareImageBuffer.stride, mSquareImageBuffer.buffer);
        GLES20.glUniform1i(mTextureUniforHandle, 0);
        GLES20.glUniform2f(mViewSizeUniformHandle, viewport.width(), viewport.height());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mSquareImageBuffer.count);

        mCameraTexture.unbind();
        mProgram.disableAttributes();
     */
}
