package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.thommil.animalsgo.utils.ByteBufferPool;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class GlDrawableBufferBatch<T> extends GlDrawableBuffer<T>{

    private static final String TAG = "A_GO/GlDraw...fferBatch";

    private final List<GlDrawableBuffer<T>> mBuffers;

    private GlBufferIndex mIndicesBuffer = null;

    private int mPosition[];
    private int mComponents[];
    private int mDatatype[];
    private boolean mNormalized[];
    private int mOffset[];

    private int mCountPerBuffer;

    public GlDrawableBufferBatch(final GlDrawableBuffer<T> ...buffers){
        super();
        mBuffers = new ArrayList<>();
        mManagedBuffer = true;
        for(GlDrawableBuffer buffer : buffers){
            addElement(buffer);
        }
    }

    public synchronized GlDrawableBufferBatch addElement(final GlDrawableBuffer<T> buffer){
        //Log.d(TAG, "addElement("+buffer+")");
        if(this.handle != UNBIND_HANDLE){
            throw new IllegalStateException("Cannot remove element in batch after allocate(), keep a fix amount of data using VBO/VAO");
        }

        if(mBuffers.isEmpty()){
            this.datatype = buffer.chunks[0].datatype;
            this.datasize = buffer.chunks[0].datasize;
            this.stride = buffer.stride;
            mPosition = new int[buffer.chunks.length];
            mComponents = new int[buffer.chunks.length];
            mDatatype = new int[buffer.chunks.length];
            mNormalized = new boolean[buffer.chunks.length];
            mOffset = new int[buffer.chunks.length];

            for(int index=0; index < buffer.chunks.length; index++){
                mPosition[index] = buffer.chunks[index].position;
                mComponents[index] = buffer.chunks[index].components;
                mDatatype[index] = buffer.chunks[index].datatype;
                mNormalized[index] = buffer.chunks[index].normalized;
                mOffset[index] = buffer.chunks[index].offset;
            }
        }

        if(mBuffers.add(buffer)) {
            this.size += buffer.size;
            this.count += buffer.count;
            mCountPerBuffer = this.count / mBuffers.size();
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

    public synchronized GlDrawableBufferBatch removeElement(final GlDrawableBuffer<T> buffer){
        //Log.d(TAG, "removeElement("+buffer+")");
        if(this.handle != UNBIND_HANDLE){
            throw new IllegalStateException("Cannot remove element in batch after allocate(), keep a fix amount of data using VBO/VAO");
        }
        if(mBuffers.remove(buffer)){
            this.size -= buffer.size;
            this.count -= buffer.count;
        }

        if(mBuffers.isEmpty()){
            mPosition = null;
            mComponents = null;
            mDatatype = null;
            mNormalized = null;
            mOffset = null;
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
    public GlBuffer allocate(final int usage, final int target, final boolean freeLocal){
        //android.util.//Log.d(TAG,"createVBO("+usage+","+target+","+freeLocal+")");
        if(this.handle == UNBIND_HANDLE){
            final int[] handles = new int[1];

            //Create buffer on server
            GLES20.glGenBuffers(1, handles, 0);
            this.handle = handles[0];
            this.target = target;

            GlOperation.checkGlError(TAG, "glGenBuffers");

            //Bind it
            GLES20.glBindBuffer(target, this.handle);
            if(this.buffer == null){
                this.commit(false);
            }
            //Push data into it
            this.buffer.position(0);
            GLES20.glBufferData(target, this.size, this.buffer, usage);
            //Unbind it
            GLES20.glBindBuffer(target, UNBIND_HANDLE);

            //Check error on bind only
            GlOperation.checkGlError(TAG, "glBufferData");

            //Free local buffer is queried
            if(mManagedBuffer && freeLocal){
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

            mode = MODE_VBO;

            if(GlOperation.getVersion()[0] >= 3 && this.vertexAttribHandles != null
                    && this.vertexAttribHandles.length > 0){
                GLES30.glGenVertexArrays(1, handles, 0);
                mVaoHandle = handles[0];
                GlOperation.checkGlError(TAG, "glGenVertexArrays");

                GLES30.glBindVertexArray(mVaoHandle);
                GLES20.glBindBuffer(target, this.handle);

                for(int index=0; index < this.vertexAttribHandles.length; index++){
                    GLES20.glEnableVertexAttribArray(this.vertexAttribHandles[index]);
                    GLES20.glVertexAttribPointer(this.vertexAttribHandles[index], mComponents[index],
                            mDatatype[index], mNormalized[index], stride, mOffset[index]);
                }
                GlOperation.checkGlError(TAG, "glVertexAttribPointer");

                GLES20.glBindBuffer(target, UNBIND_HANDLE);
                GLES30.glBindVertexArray(UNBIND_HANDLE);

                mode = MODE_VAO;
            }
        }
        else{
            Log.w(TAG, "multiple allocation detected !");
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

        final int elementsCount = mBuffers.size();
        if(elementsCount > 0) {
            if (mIndicesBuffer == null) {
                mIndicesBuffer = new GlBufferIndex(elementsCount, mBuffers.get(0).count);
                mIndicesBuffer.allocate(this.usage);
            } else {
                if (elementsCount != mIndicesBuffer.getElementsCount()) {
                    mIndicesBuffer.free();
                    mIndicesBuffer = new GlBufferIndex(elementsCount, mBuffers.get(0).count);
                    mIndicesBuffer.allocate(this.usage);
                }
            }
        }

        int bufferIndex = 0;
        for(final GlBuffer<T> element : mBuffers){
            this.buffer.position(bufferIndex * element.stride);
            element.commit(false);
            bufferIndex++;
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
        switch (this.mode){
            case GlBuffer.MODE_VAO: {
                bind();

                mIndicesBuffer.bind();
                GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndicesBuffer.count, GLES20.GL_UNSIGNED_SHORT, 0);
                mIndicesBuffer.unbind();

                unbind();
                break;
            }
            case GlBuffer.MODE_VBO: {
                program.enableAttributes();

                this.bind();

                if(this.vertexAttribHandles != null) {
                    for (int index = 0; index < this.vertexAttribHandles.length; index++) {
                        GLES20.glVertexAttribPointer(this.vertexAttribHandles[index], mComponents[index],
                                mDatatype[index], mNormalized[index], this.stride, mOffset[index]);
                    }
                }

                mIndicesBuffer.bind();
                GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndicesBuffer.count, GLES20.GL_UNSIGNED_SHORT, 0);
                mIndicesBuffer.unbind();

                unbind();
                program.disableAttributes();
                break;
            }
            default: {
                program.enableAttributes();

                if(this.vertexAttribHandles != null) {
                    for (int index = 0; index < this.vertexAttribHandles.length; index++) {
                        this.buffer.position(mPosition[index]);
                        GLES20.glVertexAttribPointer(this.vertexAttribHandles[index], mComponents[index],
                                mDatatype[index], mNormalized[index], this.stride, this.buffer);
                    }
                }

                mIndicesBuffer.bind();
                this.buffer.position(0);
                GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndicesBuffer.count, GLES20.GL_UNSIGNED_SHORT, 0);
                mIndicesBuffer.unbind();

                program.disableAttributes();
            }
        }
    }


    @Override
    public GlBuffer free() {
        super.free();
        if(mIndicesBuffer != null){
            mIndicesBuffer.free();
            mIndicesBuffer = null;
        }
        mBuffers.clear();
        return this;
    }
}
