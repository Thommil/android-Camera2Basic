package com.thommil.animalsgo.gl.libgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;



import android.opengl.GLES20;
import android.util.Log;

import com.thommil.animalsgo.utils.ByteBufferPool;

/**
 * Buffer abstraction class for VBO
 * 
 * @author Thomas MILLET
 *
 * @param <T> Should be of type byte[], short[], int[], float[]
 *
 */
public class GlBuffer<T>{
	
	/**
	 * TAG log
	 */
	@SuppressWarnings("unused")
	private static final String TAG = "A_GO/GlBuffer";
	
	/**
	 * Alias for BYTE in OpenGL for inner data type
	 */
	public static final int TYPE_BYTE = GLES20.GL_UNSIGNED_BYTE;
	
	/**
	 * Alias for SHORT in OpenGL for inner data type
	 */
	public static final int TYPE_SHORT = GLES20.GL_UNSIGNED_SHORT;
	
	/**
	 * Alias for INT in OpenGL for inner data type
	 */
	public static final int TYPE_INT = GLES20.GL_UNSIGNED_INT;
	
	/**
	 * Alias for FLOAT in OpenGL for inner data type
	 */
	public static final int TYPE_FLOAT = GLES20.GL_FLOAT;
	
	/**
	 * Handle to use to unbind current buffer
	 */
	public static final int UNBIND_HANDLE = GLES20.GL_ZERO;
	
	/**
	 * Buffer type for client drawing
	 */
	public static final int TYPE_CLIENT_DRAW = GLES20.GL_ZERO;
	
	/**
	 * Buffer usage for server static drawing (no data update)
	 */
	public static final int USAGE_STATIC_DRAW = GLES20.GL_STATIC_DRAW;
	
	/**
	 * Buffer usage for server dynamic drawing (many data updates)
	 */
	public static final int USAGE_DYNAMIC_DRAW = GLES20.GL_DYNAMIC_DRAW;
	
	/**
	 * Buffer usage for server stream drawing (few data updates)
	 */
	public static final int USAGE_STREAM_DRAW = GLES20.GL_STREAM_DRAW;
	
	/**
	 * Buffer target for vertices data
	 */
	public static final int TARGET_ARRAY_BUFFER = GLES20.GL_ARRAY_BUFFER;
	
	/**
	 * Buffer target for index data
	 */
	public static final int TARGET_ELEMENT_ARRAY_BUFFER = GLES20.GL_ELEMENT_ARRAY_BUFFER;

	/**
	 * The type of data in buffer (should be Chunk.TYPE_BYTE, Chunk.TYPE_SHORT, Chunk.TYPE_INT, Chunk.TYPE_FLOAT)
	 */
	public int datatype = TYPE_BYTE;
	
	/**
	 * The size of data
	 */
	public int datasize = Byte.BYTES;
	
	/**
	 * The number of elements in this buffer
	 */
	public int count = 0;
	
	/**
	 * The buffer size
	 */
	public int size = 0;

	/**
	 * The current GL usage (USAGE_STATIC_DRAW, USAGE_DYNAMIC_DRAW or USAGE_STREAM_DRAW)
	 */
	public int usage = USAGE_STATIC_DRAW;
	
	/**
	 * The current GL target (TARGET_ARRAY_BUFFER or TARGET_ELEMENT_ARRAY_BUFFER)
	 */
	public int target = TARGET_ARRAY_BUFFER;
	
	/**
	 * The bound buffer
	 */
	public Buffer buffer;
	
	/**
	 * Handle on server buffer if TYPE_SERVER_STATIC_DRAW or TYPE_SERVER_DYNAMIC_DRAW
	 */
	public int handle = GLES20.GL_NONE;
	

	/**
	 * Constructor
	 */
	public GlBuffer(){
		final int[] handles = new int[1];
		GLES20.glGenBuffers(1, handles, 0);
		this.handle = handles[0];
	}
	
	/**
	 * Bind current buffer to active buffer if GPU 
	 */
	public GlBuffer bind(){
		//Log.d(TAG,"bind()");
		GLES20.glBindBuffer(this.target, this.handle);
		return this;
	}
	
	/**
	 * Unbind current buffer from active buffer if GPU 
	 */
	public GlBuffer unbind(){
		//Log.d(TAG,"unbind()");
		GLES20.glBindBuffer(this.target, UNBIND_HANDLE);
		return this;
	}
	
	/**
	 * Update buffer and associated VBO
	 */
	public void setData(final T data){
        //Log.d(TAG,"setData("+data+")");
		if(buffer == null){
			if(data instanceof byte[]) {
				datatype = TYPE_BYTE;
				datasize = Byte.BYTES;
				count = ((byte[]) data).length;
				size = count * datasize;
				buffer = ByteBufferPool.getInstance().getDirectByteBuffer(count);
				((ByteBuffer) buffer).put((byte[])data);
				buffer.rewind();
			}
			else if(data instanceof short[]) {
				datatype = TYPE_SHORT;
				datasize = Short.BYTES;
				count = ((short[]) data).length;
				size = count * datasize;
				buffer = ByteBufferPool.getInstance().getDirectShortBuffer(count);
				((ShortBuffer) buffer).put((short[])data);
				buffer.rewind();
			}
			else if(data instanceof int[]) {
				datatype = TYPE_INT;
				datasize = Integer.BYTES;
				count = ((int[]) data).length;
				size = count * datasize;
				buffer = ByteBufferPool.getInstance().getDirectIntBuffer(count);
				((IntBuffer) buffer).put((int[])data);
				buffer.rewind();
			}
			else if(data instanceof float[]){
				datatype = TYPE_FLOAT;
				datasize = Float.BYTES;
				count = ((float[]) data).length;
				size = count * datasize;
				buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(count);
				((FloatBuffer) buffer).put((float[])data);
				buffer.rewind();
			}
			else{
				throw new RuntimeException("Invalid type : " + data.getClass());
			}
		}
		else{
			final int dataCount;
			switch(datatype) {
				case TYPE_BYTE:
					dataCount = ((byte[]) data).length;
					break;
				case TYPE_SHORT:
					dataCount = ((short[]) data).length;
					break;
				case TYPE_INT:
					dataCount = ((int[]) data).length;
					break;
				default:
					dataCount = ((float[]) data).length;
					break;
			}
			if(dataCount > count){
				throw new RuntimeException("Data length must be equal or less than initial buffer capacity");
			}
			this.buffer.limit(dataCount * datasize);
		}

		if(this.handle != UNBIND_HANDLE){
			this.buffer.rewind();
			GLES20.glBufferSubData(this.target, 0, this.size, this.buffer);
		}	
	}

	protected void freeBuffer(){
        //Log.d(TAG,"freeBuffer()");
		if(this.buffer != null){
			switch (datatype){
				case TYPE_BYTE:
					ByteBufferPool.getInstance().returnDirectBuffer((ByteBuffer) buffer);
					break;
				case TYPE_SHORT:
					ByteBufferPool.getInstance().returnDirectBuffer((ShortBuffer) buffer);
					break;
				case TYPE_INT:
					ByteBufferPool.getInstance().returnDirectBuffer((IntBuffer) buffer);
					break;
				case TYPE_FLOAT:
					ByteBufferPool.getInstance().returnDirectBuffer((FloatBuffer) buffer);
					break;
			}
			this.buffer = null;
		}
		this.datasize = 0;
		this.size = 0;
	}

	/**
	 * Create a VBO on GPU and bind buffer data to it
	 * 
	 * @param usage Should be USAGE_STATIC_DRAW, USAGE_DYNAMIC_DRAW or USAGE_STREAM_DRAW
	 * @param target Should be GLES20.GL_ARRAY_BUFFER or GLES20.GL_ELEMENT_ARRAY_BUFFER
	 * @param freeLocal If true, the local buffer is released at binding
	 * 
	 * @return The buffer handle on server (available in handle attribute too)
	 */
	public GlBuffer allocate(final int usage, final int target, final boolean freeLocal){
        Log.d(TAG,"allocate("+usage+", "+target+", "+freeLocal+")");

        //Create buffer on server
        this.target = target;

        //Push data into it
        this.buffer.rewind();
        GLES20.glBufferData(target, this.size, this.buffer, usage);
        //Unbind it
        GLES20.glBindBuffer(target, UNBIND_HANDLE);

        //Check error on bind only
        GlOperation.checkGlError("create VBO");

        //Free local buffer is queried
        if(freeLocal){
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



	/**
	 * Free local and server buffers
	 */
	public void free(){
		//Log.d(TAG,"free()");
		if(this.handle != UNBIND_HANDLE){
			final int[] handles = new int[]{this.handle};
			this.handle = UNBIND_HANDLE;
			GLES20.glDeleteBuffers(1, handles, 0);
		}
		freeBuffer();
	}

}
