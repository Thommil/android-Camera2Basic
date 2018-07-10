package com.thommil.animalsgo.gl.libgl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class GlDrawableBufferBatch<T> extends GlDrawableBuffer<T>{

    private static final String TAG = "A_GO/GlDrawableBufferBatch";

    private final List<GlDrawableBufferBatch<T>> mBuffers;

    public GlDrawableBufferBatch(final GlDrawableBufferBatch<T> ...buffers){
        super();
        mBuffers = new ArrayList<>();
        mManagedBuffer = true;
    }

    public synchronized GlDrawableBufferBatch addElement(final GlDrawableBufferBatch<T> buffer){
        //Log.d(TAG, "addElement("+buffer+")");
        if(this.handle != UNBIND_HANDLE){
            throw new IllegalStateException("Cannot add element in batch after allocate()");
        }

        if(mBuffers.isEmpty()){
            this.datatype = buffer.chunks[0].datatype;
            this.datasize = buffer.chunks[0].datasize;
            this.stride = buffer.stride;
        }

        if(mBuffers.add(buffer)) {
            this.size += buffer.size;
            this.count += buffer.count;
        }

        if(this.buffer != null){
            switch(this.datatype){
                case TYPE_BYTE :
                    ByteBufferPool.getInstance().returnDirectBuffer((ByteBuffer)this.buffer);
                    break;
                case TYPE_SHORT :
                    ByteBufferPool.getInstance().returnDirectBuffer((ShortBuffer)this.buffer);
                    break;
                case TYPE_INT :
                    ByteBufferPool.getInstance().returnDirectBuffer((IntBuffer)this.buffer);
                    break;
                default :
                    ByteBufferPool.getInstance().returnDirectBuffer((FloatBuffer)this.buffer);
            }
            this.buffer = null;
        }

        return this;
    }

    public synchronized GlDrawableBufferBatch removeElement(final GlDrawableBufferBatch<T> buffer){
        //Log.d(TAG, "removeElement("+buffer+")");
        if(this.handle != UNBIND_HANDLE){
            throw new IllegalStateException("Cannot remove element in batch after allocate()");
        }
        if(mBuffers.remove(buffer)){
            this.size -= buffer.size;
            this.count -= buffer.count;
        }

        if(this.buffer != null){
            switch(this.datatype){
                case TYPE_BYTE :
                    ByteBufferPool.getInstance().returnDirectBuffer((ByteBuffer)this.buffer);
                    break;
                case TYPE_SHORT :
                    ByteBufferPool.getInstance().returnDirectBuffer((ShortBuffer)this.buffer);
                    break;
                case TYPE_INT :
                    ByteBufferPool.getInstance().returnDirectBuffer((IntBuffer)this.buffer);
                    break;
                default :
                    ByteBufferPool.getInstance().returnDirectBuffer((FloatBuffer)this.buffer);
            }
            this.buffer = null;
        }

        return this;
    }


    @Override
    public GlBuffer commit(boolean push) {
        if(this.buffer == null) {
            switch (this.datatype) {
                case TYPE_FLOAT:
                    this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(this.size >> 2);
                    for (final GlBuffer<T> element : mBuffers) {
                        element.buffer = this.buffer;
                    }
                    break;
                case TYPE_INT:
                    this.buffer = ByteBufferPool.getInstance().getDirectIntBuffer(this.size >> 2);
                    for (final GlBuffer<T> element : mBuffers) {
                        element.buffer = this.buffer;
                    }
                    break;
                case TYPE_SHORT:
                    this.buffer = ByteBufferPool.getInstance().getDirectShortBuffer(this.size >> 1);
                    for (final GlBuffer<T> element : mBuffers) {
                        element.buffer = this.buffer;
                    }
                    break;
                default:
                    this.buffer = ByteBufferPool.getInstance().getDirectByteBuffer(this.size);
                    for (final GlBuffer<T> element : mBuffers) {
                        element.buffer = this.buffer;
                    }
                    break;
            }
        }

        this.buffer.position(0);
        for(final GlBuffer<T> element : mBuffers){
            element.commit(push);
        }

        //Update server if needed
        if(push){
            push();
        }

        return this;
    }

    @Override
    public GlBuffer commit(Chunk<T>[] chunks) {
        return commit();
    }

    @Override
    public GlBuffer commit(Chunk<T>[] chunks, boolean push) {
        return commit(push);
    }

    @Override
    public GlBuffer commit(Chunk<T> chunk) {
        return commit();
    }

    @Override
    public GlBuffer commit(Chunk<T> chunk, boolean push) {
        return commit(push);
    }

    @Override
    public void draw(GlProgram program) {
        /*switch (this.mode){
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
                                this.chunks[index].datatype, this.chsunks[index].normalized, this.stride, this.chunks[index].offset);
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
        }*/
    }


    @Override
    public GlBuffer free() {
        super.free();
        mBuffers.clear();
        return this;
    }
}
