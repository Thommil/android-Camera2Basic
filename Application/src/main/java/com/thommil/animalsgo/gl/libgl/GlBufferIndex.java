package com.thommil.animalsgo.gl.libgl;

import android.opengl.GLES20;
import android.util.Log;

import com.thommil.animalsgo.utils.ByteBufferPool;

import java.nio.ShortBuffer;


public class GlBufferIndex extends GlBuffer<short[]> {

    private static final String TAG = "A_GO/GlBufferIndex";

    protected final int mElementsCount;
    protected final int mVerticesPerElement;

    public GlBufferIndex(Chunk<short[]>... chunks) {
        super(chunks);
        mElementsCount = mVerticesPerElement = 0;
    }

    public GlBufferIndex(final int elementsCount, final int verticesPerElement){
        super(new GlBuffer.Chunk<>(new short[(verticesPerElement + 2) * elementsCount - 2], 1));
        mElementsCount = elementsCount;
        mVerticesPerElement = verticesPerElement;
        mManagedBuffer = true;

        int dataIndex = 0;
        while(dataIndex < verticesPerElement){
            chunks[0].data[dataIndex] = (short)dataIndex++;
        }

        if(elementsCount >1 ){
            short j = (short)dataIndex;
            chunks[0].data[dataIndex++] = (short)(j-1);
            for (int element = 1; element < (elementsCount - 1) ; j += verticesPerElement, element++) {
                chunks[0].data[dataIndex++] = (short)(j + 0);
                for(int vertice = 0; vertice < verticesPerElement; vertice++){
                    chunks[0].data[dataIndex++] = (short)(j + vertice);
                }
                chunks[0].data[dataIndex++] = (short)(j + (verticesPerElement-1));
            }
            chunks[0].data[dataIndex++] = (short)(j + 0);
            for(int vertice = 0; vertice < verticesPerElement; vertice++){
                chunks[0].data[dataIndex++] = (short)(j + vertice);
            }
        }

    }

    public GlBuffer allocate(int usage) {
        return allocate(usage, TARGET_ELEMENT_ARRAY_BUFFER, true);
    }

    @Override
    public GlBuffer allocate(int usage, int target, boolean freeLocal) {
        //android.util.//Log.d(TAG,"createVBO("+usage+","+target+","+freeLocal+")");
        if(this.handle == UNBIND_HANDLE){
            final int[] handles = new int[1];

            //Create buffer on server
            GLES20.glGenBuffers(1, handles, 0);
            this.handle = handles[0];
            this.target = TARGET_ELEMENT_ARRAY_BUFFER;

            GlOperation.checkGlError(TAG, "glGenBuffers");

            //Bind it
            GLES20.glBindBuffer(TARGET_ELEMENT_ARRAY_BUFFER, this.handle);
            if(this.buffer == null){
                this.commit(false);
            }
            //Push data into it
            this.buffer.position(0);
            GLES20.glBufferData(this.target, this.size, this.buffer, usage);
            //Unbind it
            GLES20.glBindBuffer(this.target, UNBIND_HANDLE);

            //Check error on bind only
            GlOperation.checkGlError(TAG, "glBufferData");

            //Free local buffer is queried
            ByteBufferPool.getInstance().returnDirectBuffer((ShortBuffer)this.buffer);
            this.buffer = null;

            mode = MODE_VBO;
        }
        else{
            Log.w(TAG, "multiple allocation detected !");
        }

        return this;
    }

    public int getElementsCount() {
        return mElementsCount;
    }

    public int getVerticesPerElement() {
        return mVerticesPerElement;
    }
}
