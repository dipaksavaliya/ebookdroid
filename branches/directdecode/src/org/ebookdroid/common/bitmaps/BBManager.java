package org.ebookdroid.common.bitmaps;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.emdev.common.log.LogContext;
import org.emdev.common.log.LogManager;
import org.emdev.utils.LengthUtils;
import org.emdev.utils.collections.ArrayDeque;
import org.emdev.utils.collections.SparseArrayEx;
import org.emdev.utils.collections.TLIterator;

public class BBManager {

    static final LogContext LCTX = LogManager.root().lctx("BBManager", true);

    private final static long BITMAP_MEMORY_LIMIT = Runtime.getRuntime().maxMemory() / 2;

    private static final int GENERATION_THRESHOLD = 10;

    private static SparseArrayEx<ByteBufferBitmap> used = new SparseArrayEx<ByteBufferBitmap>();

    private static ArrayDeque<ByteBufferBitmap> pool = new ArrayDeque<ByteBufferBitmap>();

    private static Queue<Object> releasing = new ConcurrentLinkedQueue<Object>();

    private static final AtomicLong created = new AtomicLong();
    private static final AtomicLong reused = new AtomicLong();

    private static final AtomicLong memoryUsed = new AtomicLong();
    private static final AtomicLong memoryPooled = new AtomicLong();

    private static AtomicLong generation = new AtomicLong();

    private static ReentrantLock lock = new ReentrantLock();

    public static ByteBufferBitmap getBitmap(final int width, final int height) {
        lock.lock();
        try {
            if (LCTX.isDebugEnabled()) {
                if (memoryUsed.get() + memoryPooled.get() == 0) {
                    LCTX.d("!!! Bitmap pool size: " + (BITMAP_MEMORY_LIMIT / 1024) + "KB");
                }
            }

            final TLIterator<ByteBufferBitmap> it = pool.iterator();
            try {
                while (it.hasNext()) {
                    final ByteBufferBitmap ref = it.next();

                    if (ref.size >= 4 * width * height) {
                        if (ref.used.compareAndSet(false, true)) {
                            it.remove();

                            ref.gen = generation.get();
                            ref.width = width;
                            ref.height = height;
                            used.append(ref.id, ref);

                            reused.incrementAndGet();
                            memoryPooled.addAndGet(-ref.size);
                            memoryUsed.addAndGet(ref.size);

                            if (LCTX.isDebugEnabled()) {
                                LCTX.d("Reuse bitmap: [" + ref.id + ", " + width + ", " + height + "], created="
                                        + created + ", reused=" + reused + ", memoryUsed=" + used.size() + "/"
                                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                                        + (memoryPooled.get() / 1024) + "KB");
                            }
                            return ref;
                        } else {
                            if (LCTX.isDebugEnabled()) {
                                LCTX.d("Attempt to re-use used bitmap: " + ref);
                            }
                        }
                    }
                }
            } finally {
                it.release();
            }

            final ByteBufferBitmap ref = new ByteBufferBitmap(width, height);
            used.put(ref.id, ref);

            created.incrementAndGet();
            memoryUsed.addAndGet(ref.size);

            if (LCTX.isDebugEnabled()) {
                LCTX.d("Create bitmap: [" + ref.id + ", " + width + ", " + height + "], created="
                        + created + ", reused=" + reused + ", memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }

            shrinkPool(BITMAP_MEMORY_LIMIT);

            return ref;
        } finally {
            lock.unlock();
        }
    }

    public static void clear(final String msg) {
        lock.lock();
        try {
            generation.addAndGet(GENERATION_THRESHOLD * 2);
            removeOldRefs();
            release();
            shrinkPool(0);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public static void release() {
        lock.lock();
        try {
            generation.incrementAndGet();
            removeOldRefs();

            int count = 0;
            final int queueBefore = LCTX.isDebugEnabled() ? releasing.size() : 0;
            while (!releasing.isEmpty()) {
                final Object ref = releasing.poll();
                if (ref instanceof ByteBufferBitmap) {
                    releaseImpl((ByteBufferBitmap) ref);
                    count++;
                } else if (ref instanceof List) {
                    final List<BBBitmaps> list = (List<BBBitmaps>) ref;
                    for (final BBBitmaps bmp : list) {
                        final ByteBufferBitmap bitmap = bmp.clear();
                        if (bitmap != null) {
                            releaseImpl(bitmap);
                            count++;
                        }
                    }
                } else if (ref instanceof ByteBufferBitmap[]) {
                    final ByteBufferBitmap[] bitmaps = (ByteBufferBitmap[]) ref;
                    for (final ByteBufferBitmap bitmap : bitmaps) {
                        if (bitmap != null) {
                            releaseImpl(bitmap);
                            count++;
                        }
                    }
                } else {
                    LCTX.e("Unknown object in release queue: " + ref);
                }
            }

            shrinkPool(BITMAP_MEMORY_LIMIT);

            if (LCTX.isDebugEnabled()) {
                LCTX.d("Return " + count + " bitmap(s) to pool: " + "memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB" + ", releasing queue size " + queueBefore + " => 0");
            }
        } finally {
            lock.unlock();
        }
    }

    public static void release(final ByteBufferBitmap ref) {
        if (ref != null) {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Adding 1 ref to release queue");
            }
            releasing.add(ref);
        }
    }

    public static void release(final ByteBufferBitmap[] refs) {
        if (refs != null) {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Adding " + refs.length + " refs to release queue");
            }
            releasing.add(refs);
        }
    }

    public static void release(final List<BBBitmaps> bitmapsToRecycle) {
        if (LengthUtils.isNotEmpty(bitmapsToRecycle)) {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Adding  list of " + bitmapsToRecycle.size() + " bitmaps to release queue");
            }
            releasing.add(new ArrayList<BBBitmaps>(bitmapsToRecycle));
        }
    }

    static void releaseImpl(final ByteBufferBitmap ref) {
        assert ref != null;
        if (ref.used.compareAndSet(true, false)) {
            if (used.get(ref.id, null) == ref) {
                used.remove(ref.id);
                memoryUsed.addAndGet(-ref.size);
            } else {
                LCTX.e("The bitmap " + ref + " not found in used ones");
            }
        } else {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Attempt to release unused bitmap");
            }
        }

        pool.add(ref);
        memoryPooled.addAndGet(ref.size);
    }

    private static void removeOldRefs() {
        final long gen = generation.get();
        int recycled = 0;
        final Iterator<ByteBufferBitmap> it = pool.iterator();
        while (it.hasNext()) {
            final ByteBufferBitmap ref = it.next();
            if (gen - ref.gen > GENERATION_THRESHOLD) {
                it.remove();
                ref.recycle();
                recycled++;
                memoryPooled.addAndGet(-ref.size);
            }
        }
        if (recycled > 0) {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Recycled " + recycled + " pooled bitmap(s): " + "memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }
        }
    }

    private static void shrinkPool(final long limit) {
        int recycled = 0;
        while (memoryPooled.get() + memoryUsed.get() > limit && !pool.isEmpty()) {
            final ByteBufferBitmap ref = pool.poll();
            if (ref != null) {
                ref.recycle();
                memoryPooled.addAndGet(-ref.size);
                recycled++;
            }
        }

        if (recycled > 0) {
            if (LCTX.isDebugEnabled()) {
                LCTX.d("Recycled " + recycled + " pooled bitmap(s): " + "memoryUsed=" + used.size() + "/"
                        + (memoryUsed.get() / 1024) + "KB" + ", memoryInPool=" + pool.size() + "/"
                        + (memoryPooled.get() / 1024) + "KB");
            }
        }
    }

    public static int getBitmapBufferSize(final int width, final int height, final Bitmap.Config config) {
        return getPixelSizeInBytes(config) * width * height;
    }

    public static int getBitmapBufferSize(final Bitmap parentBitmap, final Rect childSize) {
        int bytes = 4;
        if (parentBitmap != null) {
            bytes = BBManager.getPixelSizeInBytes(parentBitmap.getConfig());
        }
        return bytes * childSize.width() * childSize.height();
    }

    public static int getPixelSizeInBytes(final Bitmap.Config config) {
        switch (config) {
            case ALPHA_8:
                return 1;
            case ARGB_4444:
            case RGB_565:
                return 2;
            case ARGB_8888:
            default:
                return 4;
        }
    }
}
