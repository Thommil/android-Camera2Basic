package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;
import android.util.Log;

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
                        GLES20.glVertexAttribPointer(chunk.handle, chunk.components,
                                data.datatype, false, data.stride, chunk.offset);
                    }
                    else{
                        GLES20.glVertexAttribPointer(attributes[chunkIndex], chunk.components,
                                data.datatype, false, data.stride, chunk.offset);
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
                    data.buffer.position(chunk.position);
                    if (useAllAttributes) {
                        GLES20.glVertexAttribPointer(chunk.handle, chunk.components,
                                data.datatype, false, data.stride, data.buffer);
                    }
                    else{
                        GLES20.glVertexAttribPointer(attributes[chunkIndex], chunk.components,
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

    public static void drawElements(final GlProgram program, final GlBuffer data, final GlBuffer indices){
        drawElements(program, data, indices, null);
    }

    public static void drawElements(final GlProgram program, final GlBuffer data, final GlBuffer indices, final int[] attributes){
        switch (data.mode){
            case GlBuffer.MODE_VAO: {
                data.bind();
                indices.bind();
                GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, data.count, indices.datatype, 0);
                indices.unbind();
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
                        GLES20.glVertexAttribPointer(chunk.handle, chunk.components,
                                data.datatype, false, data.stride, chunk.offset);
                    }
                    else{
                        GLES20.glVertexAttribPointer(attributes[chunkIndex], chunk.components,
                                data.datatype, false, data.stride, chunk.offset);
                    }
                    chunkIndex++;
                }

                indices.bind();
                GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, data.count, indices.datatype, 0);
                indices.unbind();

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
                    data.buffer.position(chunk.position);
                    if (useAllAttributes) {
                        GLES20.glVertexAttribPointer(chunk.handle, chunk.components,
                                data.datatype, false, data.stride, data.buffer);
                    }
                    else{
                        GLES20.glVertexAttribPointer(attributes[chunkIndex], chunk.components,
                                data.datatype, false, data.stride, data.buffer);
                    }
                    chunkIndex++;
                }

                indices.buffer.position(0);
                GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, data.count, indices.datatype, indices.buffer);

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
}
