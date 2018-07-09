package com.thommil.animalsgo.gl.libgl;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;


/**
 * ByteBuffer buffer pool :
 *	<ul>
 * 	<li>try to avoid memory leaks</li> 
 * 	<li>reuse buffers when possible</li>
 * 	<li>use WeakReference to ease deallocation</li>
 *	</ul> 
 * 
 * 	@author Thomas MILLET
 * 
 */
public class ByteBufferPool {

	private static final String TAG = "A_GO/ByteBufferPool";

	/**
	 * Multimap with weak values for ByteBuffers 
	 */
	private final static ConcurrentMap<Integer, Queue<WeakReference<ByteBuffer>>> poolByte =
		    new ConcurrentHashMap<Integer, Queue<WeakReference<ByteBuffer>>>();
	
	/**
	 * Multimap with weak values for ShortBuffers 
	 */
	private final static ConcurrentMap<Integer, Queue<WeakReference<ShortBuffer>>> poolShort =
		    new ConcurrentHashMap<Integer, Queue<WeakReference<ShortBuffer>>>();
	
	/**
	 * Multimap with weak values for IntBuffers 
	 */
	private final static ConcurrentMap<Integer, Queue<WeakReference<IntBuffer>>> poolInt =
		    new ConcurrentHashMap<Integer, Queue<WeakReference<IntBuffer>>>();
	
	/**
	 * Multimap with weak values for FloatBuffers 
	 */
	private final static ConcurrentMap<Integer, Queue<WeakReference<FloatBuffer>>> poolFloat =
		    new ConcurrentHashMap<Integer, Queue<WeakReference<FloatBuffer>>>();
  
	/**
	 * The Singleton
	 */
	private final static ByteBufferPool instance = new ByteBufferPool();
	
	/**
	 * The private singleton constructor
	 */
	private ByteBufferPool(){

	}
	
	/**
	 * The singleton accessor
	 * 
	 * @return The {@link ByteBufferPool} singleton
	 */
	public static ByteBufferPool getInstance(){
		return instance;
	}
	
	/**
   	 * Allocate a direct ByteBuffer of the specified size, in bytes.
   	 * If a pooled buffer is available, returns that. Otherwise
   	 * allocates a new one.
   	 * 
   	 * @param size The size in elements (bytes) of the ByteBuffer to get
   	 * 
   	 * @return A direct allocated ByteBuffer
   	 */
	public ByteBuffer getDirectByteBuffer(int size) {
		//android.util.//Log.d(TAG,"getDirectByteBuffer("+size+")");
		final Queue<WeakReference<ByteBuffer>> list = poolByte.get(size);
		if (list == null) {
			return (ByteBuffer)ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
		}
    
		WeakReference<ByteBuffer> ref;
		while ((ref = list.poll()) != null) {
			final ByteBuffer b = ref.get();
			if (b != null) {
				b.clear();
				return b;
			}
		}

		return (ByteBuffer)ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}
	
	/**
   	 * Allocate a direct ShortBuffer of the specified size, in bytes.
   	 * If a pooled buffer is available, returns that. Otherwise
   	 * allocates a new one.
   	 * 
   	 * @param size The size in elements (shorts) of the ShortBuffer to get
   	 * 
   	 * @return A direct allocated ShortBuffer
   	 */
	public ShortBuffer getDirectShortBuffer(final int size) {
		//android.util.//Log.d(TAG,"getDirectShortBuffer("+size+")");
		final int trueSize = size * Short.BYTES;
		final Queue<WeakReference<ShortBuffer>> list = poolShort.get(trueSize);
		if (list == null) {
			return (ShortBuffer)ByteBuffer.allocateDirect(trueSize).order(ByteOrder.nativeOrder()).asShortBuffer();
		}
    
		WeakReference<ShortBuffer> ref;
		while ((ref = list.poll()) != null) {
			final ShortBuffer b = ref.get();
			if (b != null) {
				b.clear();
				return b;
			}
		}

		return (ShortBuffer)ByteBuffer.allocateDirect(trueSize).order(ByteOrder.nativeOrder()).asShortBuffer();
	}
	
	/**
   	 * Allocate a direct IntBuffer of the specified size, in bytes.
   	 * If a pooled buffer is available, returns that. Otherwise
   	 * allocates a new one.
   	 * 
   	 * @param size The size in elements (ints) of the IntBuffer to get
   	 * 
   	 * @return A direct allocated IntBuffer
   	 */
	public IntBuffer getDirectIntBuffer(final int size) {
		//android.util.//Log.d(TAG,"getDirectIntBuffer("+size+")");
		final int trueSize = size * Integer.BYTES;
		final Queue<WeakReference<IntBuffer>> list = poolInt.get(trueSize);
		if (list == null) {
			return (IntBuffer)ByteBuffer.allocateDirect(trueSize).order(ByteOrder.nativeOrder()).asIntBuffer();
		}
    
		WeakReference<IntBuffer> ref;
		while ((ref = list.poll()) != null) {
			final IntBuffer b = ref.get();
			if (b != null) {
				b.clear();
				return b;
			}
		}

		return (IntBuffer)ByteBuffer.allocateDirect(trueSize).order(ByteOrder.nativeOrder()).asIntBuffer();
	}
	
	/**
   	 * Allocate a direct FloatBuffer of the specified size, in bytes.
   	 * If a pooled buffer is available, returns that. Otherwise
   	 * allocates a new one.
   	 * 
   	 * @param size The size in elements (floats) of the FloatBuffer to get
   	 * 
   	 * @return A direct allocated FloatBuffer
   	 */
	public FloatBuffer getDirectFloatBuffer(final int size) {
		//android.util.//Log.d(TAG,"getDirectFloatBuffer("+size+")");
		final int trueSize = size * Float.BYTES;
		final Queue<WeakReference<FloatBuffer>> list = poolFloat.get(trueSize);
		if (list == null) {
			return (FloatBuffer)ByteBuffer.allocateDirect(trueSize).order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
    
		WeakReference<FloatBuffer> ref;
		while ((ref = list.poll()) != null) {
			final FloatBuffer b = ref.get();
			if (b != null) {
				b.clear();
				return b;
			}
		}

		return (FloatBuffer)ByteBuffer.allocateDirect(trueSize).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}
  
	/**
	 * Return a ByteBuffer into the pool. After being returned,
	 * the buffer may be recycled, so the user must not
	 * continue to use it in any way.
	 * 
	 * @param buf the ByteBuffer to return
	 */
	public void returnDirectBuffer(final ByteBuffer buf) {
		//android.util.//Log.d(TAG,"returnDirectBuffer(ByteBuffer)");
		final int size = buf.capacity();
		Queue<WeakReference<ByteBuffer>> list = poolByte.get(size);
		if (list == null) {
			list = new ConcurrentLinkedQueue<WeakReference<ByteBuffer>>();
			Queue<WeakReference<ByteBuffer>> prev = poolByte.putIfAbsent(size, list);
			if (prev != null) {
				list = prev;
			}
		}
		list.add(new WeakReference<ByteBuffer>(buf));
	}
	
	/**
	 * Return a ShortBuffer into the pool. After being returned,
	 * the buffer may be recycled, so the user must not
	 * continue to use it in any way.
	 * 
	 * @param buf the ShortBuffer to return
	 */
	public void returnDirectBuffer(final ShortBuffer buf) {
		//android.util.//Log.d(TAG,"returnDirectBuffer(ShortBuffer)");
		final int size = buf.capacity();
		Queue<WeakReference<ShortBuffer>> list = poolShort.get(size);
		if (list == null) {
			list = new ConcurrentLinkedQueue<WeakReference<ShortBuffer>>();
			Queue<WeakReference<ShortBuffer>> prev = poolShort.putIfAbsent(size, list);
			if (prev != null) {
				list = prev;
			}
		}
		list.add(new WeakReference<ShortBuffer>(buf));
	}
	
	/**
	 * Return a IntBuffer into the pool. After being returned,
	 * the buffer may be recycled, so the user must not
	 * continue to use it in any way.
	 * 
	 * @param buf the IntBuffer to return
	 */
	public void returnDirectBuffer(final IntBuffer buf) {
		//android.util.//Log.d(TAG,"returnDirectBuffer(IntBuffer)");
		final int size = buf.capacity();
		Queue<WeakReference<IntBuffer>> list = poolInt.get(size);
		if (list == null) {
			list = new ConcurrentLinkedQueue<WeakReference<IntBuffer>>();
			Queue<WeakReference<IntBuffer>> prev = poolInt.putIfAbsent(size, list);
			if (prev != null) {
				list = prev;
			}
		}
		list.add(new WeakReference<IntBuffer>(buf));
	}
	
	/**
	 * Return a FloatBuffer into the pool. After being returned,
	 * the buffer may be recycled, so the user must not
	 * continue to use it in any way.
	 * 
	 * @param buf the FloatBuffer to return
	 */
	public void returnDirectBuffer(final FloatBuffer buf) {
		//android.util.//Log.d(TAG,"returnDirectBuffer(FloatBuffer)");
		final int size = buf.capacity();
		Queue<WeakReference<FloatBuffer>> list = poolFloat.get(size);
		if (list == null) {
			list = new ConcurrentLinkedQueue<WeakReference<FloatBuffer>>();
			Queue<WeakReference<FloatBuffer>> prev = poolFloat.putIfAbsent(size, list);
			if (prev != null) {
				list = prev;
			}
		}
		list.add(new WeakReference<FloatBuffer>(buf));
	}
}