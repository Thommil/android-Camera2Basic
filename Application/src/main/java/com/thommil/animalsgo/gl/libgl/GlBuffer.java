package com.thommil.animalsgo.gl.libgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;



import android.opengl.GLES20;

import com.thommil.animalsgo.utils.ByteBufferPool;

/**
 * Buffer abstraction class :
 * 		<ul>
 * 			<li>build underlying buffer based on array of Chunks</li>
 * 			<li>fill the buffer using interleaves and stride</li>
 *  		<li>allows to upload buffer content to VBOs</li>
 *  		<li>underlined pools for preformances</li>
 *  		<li>not thread safe !</li>
 *  	</ul>
 *
 * <b>
 * <br/>
 * !!! Important !!!<br/>
 * To use VBO on Android 2.2, you must include local libs/armeabi to your project
 * </b>
 * <br/>
 * <br/>
 * Typical calls :
 * <pre>{@code
 *  //Local No Index
 *  buffer.toVertexAttribute(handle, chunkIndex, false);
 *  GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, buffer.count);
 *
 *  //VBO No Index
 *  buffer.toVertexAttribute(handle, chunkIndex, true);
 *  GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, buffer.count);
 *
 *  //Local Indexed
 *  buffer.toVertexAttribute(handle, chunkIndex, false);
 *  indexBuffer.position(indexBuffer.chunks[chunkIndex]);
 *  GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.count, indexBuffer.datatype, indexBuffer.data);
 *
 *  //VBO Indexed
 *  buffer.toVertexAttribute(handle, chunkIndex, true);
 *  indexBuffer.bind()
 *  GLES20Ext.glDrawElements(GLES20.GL_TRIANGLES, indexBuffer.count, indexBuffer.datatype, indexBuffer.chunks[chunkIndex].position*indexBuffer.datasize);
 *  indexBuffer.unbind()
 *
 * }</pre>
 *
 *
 *
 *
 * 
 * @author Thomas MILLET
 *
 * @param <E> Should be of type byte[], short[], int[], float[]
 *
 */
public class GlBuffer<E>{
	
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
	public static final int UNBIND_HANDLE = GLES20.GL_NONE;

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
	 * Buffer target for mVertices data
	 */
	public static final int TARGET_ARRAY_BUFFER = GLES20.GL_ARRAY_BUFFER;
	
	/**
	 * Buffer target for index data
	 */
	public static final int TARGET_ELEMENT_ARRAY_BUFFER = GLES20.GL_ELEMENT_ARRAY_BUFFER;

	/**
	 * Contains all the Buffer chunks
	 */
	public final Chunk<E>[] chunks;

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
	 * The buffer stride
	 */
	public int stride = 0;

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
	public int handle = GLES20.GL_FALSE;
	
	/**
	 * Cache the list of chunk ids
	 */
	private int[] indexCache;

	/**
	 * Constructor
	 */
	public GlBuffer(final Chunk<E>[] chunks){
		//android.util.Log.d(TAG,"NEW");
		this.chunks = chunks;

		//Init
		if(this.chunks != null && this.chunks.length > 0) {
			//Count
			this.count = this.chunks[0].size / this.chunks[0].datasize / this.chunks[0].components;
			//Datatype
			this.datatype = this.chunks[0].datatype;
			//Datasize
			this.datasize = this.chunks[0].datasize;
		} else {
			throw new IllegalArgumentException("failed to build buffer : no data provided");
		}
		int currentPosition = 0;
		int currentOffset = 0;
		this.indexCache = new int[this.chunks.length];
		int index=0;
		//First pass -> size & position
		for(Chunk<E> chunk : this.chunks){
			//BufferSize
			this.size += chunk.size;
			//Position
            chunk.offset = currentPosition;
            chunk.position = currentPosition / chunk.datasize;
			currentPosition += chunk.datasize * chunk.components;
			//Index cache
			this.indexCache[index] = index++;
		}
		//Stride
		this.stride = currentPosition;

		this.update(false);
	}
	
	/**
	 * Bind current buffer to active buffer if GPU 
	 */
	public GlBuffer bind(){
		//android.util.Log.d(TAG,"bind()");
		GLES20.glBindBuffer(this.target, this.handle);
		return this;
	}
	
	/**
	 * Unbind current buffer from active buffer if GPU 
	 */
	public GlBuffer unbind(){
		//android.util.Log.d(TAG,"unbind()");
		GLES20.glBindBuffer(this.target, UNBIND_HANDLE);
		return this;
	}
	
	/**
	 * Update the whole buffer
	 *
	 * @param updateVBO If true, buffer will be updated on VBO
	 */
	public GlBuffer update(final boolean updateVBO){
		this.update(this.indexCache, updateVBO);
		return this;
	}

	/**
	 * Update the whole buffer without commit
	 *
	 */
	public GlBuffer update(){
		this.update(this.indexCache, false);
		return this;
	}

	/**
	 * Update buffer with the indicated chunk index
	 *
	 * Data can be commited into VBO if queried.
	 *
	 * @param chunkToUpdate The index/id of the chunk to update
	 */
	public GlBuffer update(final int chunkToUpdate, final boolean updateVBO){
		this.update(new int[]{chunkToUpdate}, updateVBO);
		return this;
	}

	/**
	 * Update buffer with the indicated chunk index without commit
	 *
	 * @param chunkToUpdate The index/id of the chunk to update
	 */
	public GlBuffer update(final int chunkToUpdate){
		this.update(new int[]{chunkToUpdate}, false);
		return this;
	}

	/**
	 * Update buffer with the list of chunks indicated.
	 *
	 * Data can be commited into VBO if queried.
	 *
	 * @param chunksToUpdate The list of chunks index to update
	 * @param updateVBO Update VBO too is set to true
	 */
	public GlBuffer update(int chunksToUpdate[], boolean updateVBO){
		//android.util.Log.d(TAG,"update("+chunksToUpdate+", "+commit+")");
		switch(this.datatype){
			case TYPE_FLOAT :
				if(this.buffer == null){
					this.buffer = ByteBufferPool.getInstance().getDirectFloatBuffer(this.size / Float.BYTES);
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.buffer.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((FloatBuffer)this.buffer).put(((float[])chunk.data),compIndex,chunk.components);
					}
				}
				break;
			case TYPE_BYTE :
				if(this.buffer == null){
					this.buffer = ByteBufferPool.getInstance().getDirectByteBuffer(this.size);
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.buffer.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((ByteBuffer)this.buffer).put(((byte[])chunk.data),compIndex,chunk.components);
					}
				}
				break;
			case TYPE_SHORT :
				if(this.buffer == null){
					this.buffer = ByteBufferPool.getInstance().getDirectShortBuffer(this.size / Short.BYTES);
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.buffer.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((ShortBuffer)this.buffer).put(((short[])chunk.data),compIndex,chunk.components);
					}
				}
				break;
			case TYPE_INT :
				if(this.buffer == null){
					this.buffer = ByteBufferPool.getInstance().getDirectIntBuffer(this.size / Integer.BYTES);
				}
				for(int id : chunksToUpdate){
					final Chunk<E> chunk = this.chunks[id];
					for(int elementIndex=0, compIndex=0; elementIndex < this.count ; elementIndex++, compIndex+=chunk.components){
						this.buffer.position((chunk.position+ ((elementIndex*this.stride))/chunk.datasize));
						((IntBuffer)this.buffer).put(((int[])chunk.data),compIndex,chunk.components);
					}
				}
				break;
		}

		//Update server if needed
		if(updateVBO && this.handle != UNBIND_HANDLE){
			GLES20.glBindBuffer(this.target, this.handle);
			this.buffer.rewind();
			GLES20.glBufferSubData(this.target, 0, this.size, this.buffer);
			GLES20.glBindBuffer(this.target, UNBIND_HANDLE);
		}

		return this;
	}

	/**
	 * Update buffer with the list of chunks indicated without commit
	 *
	 * @param chunksToUpdate The list of chunks index to update
	 */
	public GlBuffer update(int chunksToUpdate[]){
		this.update(chunksToUpdate, false);
		return this;
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
		//android.util.Log.d(TAG,"createVBO("+usage+","+target+","+freeLocal+")");
		if(this.handle == GLES20.GL_FALSE){
			final int[] handles = new int[1];

			//Create buffer on server
			GLES20.glGenBuffers(1, handles, 0);
			this.handle = handles[0];
			this.target = target;

			GlOperation.checkGlError(TAG, "glGenBuffers");

			//Bind it
			GLES20.glBindBuffer(target, this.handle);
			//Push data into it
			this.buffer.rewind();
			GLES20.glBufferData(target, this.size, this.buffer, usage);
			//Unbind it
			GLES20.glBindBuffer(target, UNBIND_HANDLE);

			//Check error on bind only
			GlOperation.checkGlError(TAG, "glBufferData");

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
		}
		return this;
	}

	/**
	 * Free local and server buffers
	 */
	public GlBuffer free(){
		//android.util.Log.d(TAG,"free()");
        if(this.handle != UNBIND_HANDLE){
            final int[] handles = new int[]{this.handle};
            this.handle = UNBIND_HANDLE;
            GLES20.glDeleteBuffers(1, handles, 0);
            GlOperation.checkGlError(TAG, "glDeleteBuffers");
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
		this.size = 0;
		return this;
	}


	/**
	 *
	 * Represents a buffer chunk. This class must be used
	 * to exchange data between buffer, application and OpenGL
	 *
	 * @author Thomas MILLET
	 *
	 * @param <T> Should be of type byte[], short[], int[], float[]
	 */
	public static class Chunk<T> {

		/**
		 * The data contained in this chunk (set by Application)
		 */
		public final T data;

		/**
		 * The number of components per data (set by Application)
		 */
		public final int components;

		/**
		 * The size of a chunk element (set by GlBuffer)
		 */
		public final int datasize;

		/**
		 * The type of data in GL constant (set by GlBuffer)
		 */
		public final int datatype;

		/**
		 * The overall size of the chunk (set by GlBuffer)
		 */
		public final int size;

		/**
		 * The start position in buffer, in elements (set by GlBuffer)
		 */
		public int position;

        /**
         * The start position in buffer, in bytes (set by GlBuffer)
         */
        public int offset;

		/**
		 * Default constructor
		 *
		 * @param data       The data elements in byte[], short[], int[], float[]
		 * @param components The number of components per data entry (1, 2, 3 or 4)
		 */
		public Chunk(final T data, final int components) {
			this.data = data;
			this.components = components;
			this.offset = 0;

			//Byte data
			if (data instanceof byte[]) {
				this.datatype = GlBuffer.TYPE_BYTE;
				this.datasize = Byte.BYTES;
				this.size = this.datasize * ((byte[]) this.data).length;
			}
			//Short data
			else if (data instanceof short[]) {
				this.datatype = GlBuffer.TYPE_SHORT;
				this.datasize = Short.BYTES;
				this.size = this.datasize * ((short[]) this.data).length;
			}
			//Int data
			else if (data instanceof int[]) {
				this.datatype = GlBuffer.TYPE_INT;
				this.datasize = Integer.BYTES;
				this.size = this.datasize * ((int[]) this.data).length;
			}
			//Foat data
			else {
				this.datatype = GlBuffer.TYPE_FLOAT;
				this.datasize = Float.BYTES;
				this.size = this.datasize * ((float[]) this.data).length;
			}
		}
	}
}
