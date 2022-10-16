import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyThreadLocal<T> {
    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();
    private static final ConcurrentHashMap<MapKey, Map<MyThreadLocal<?>, Object>> THREAD_TL = new ConcurrentHashMap<>() {

        @Override
        public Map<MyThreadLocal<?>, Object> get(Object key) {
            cleanUp();
            return super.get(key);
        }

        @Override
        public Map<MyThreadLocal<?>, Object> put(MapKey key, Map<MyThreadLocal<?>, Object> value) {
            cleanUp();
            return super.put(key, value);
        }

        private void cleanUp() {
            Object ref;
            while ((ref = QUEUE.poll()) != null) {
                MapKey mk = (MapKey) ref;
                THREAD_TL.remove(mk);
            }
        }
    };

    static int size() { //for tests
        return THREAD_TL.size();
    }

    private static final class MapKey extends WeakReference<Thread> {
        private final int hash; //use both hash and id - to avoid hash collision newly created thread less likely will have same id and hash (but still possible)
        private final long id;

        public MapKey(Thread referent, ReferenceQueue<Object> queue) {
            super(referent, queue);
            hash = System.identityHashCode(referent);
            id = referent.getId();
        }

        public MapKey(Thread referent) {
            this(referent, null);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MapKey) {
                MapKey mk = (MapKey) obj;
                return mk.hash == this.hash && mk.id == this.id;
            }
            return false;
        }
    }

    public void set(T t) {
        MapKey mk = new MapKey(Thread.currentThread());
        Map<MyThreadLocal<?>, Object> myThreadLocalObjectMap = THREAD_TL.get(mk);
        if (myThreadLocalObjectMap == null) {
            myThreadLocalObjectMap = THREAD_TL.computeIfAbsent(new MapKey(Thread.currentThread(), QUEUE), mapKey -> new HashMap<>());
        }
        myThreadLocalObjectMap.put(this, t);
    }

    public T get() {
        MapKey mk = new MapKey(Thread.currentThread());
        Map<MyThreadLocal<?>, Object> myThreadLocalObjectMap = THREAD_TL.get(mk);
        if (myThreadLocalObjectMap != null) {
            return (T) myThreadLocalObjectMap.get(this);
        } else {
            return null;
        }
    }

    public void remove() {
        MapKey mk = new MapKey(Thread.currentThread());
        Map<MyThreadLocal<?>, Object> myThreadLocalObjectMap = THREAD_TL.get(mk);
        if (myThreadLocalObjectMap != null) {
            myThreadLocalObjectMap.remove(this);
        }
    }
}
