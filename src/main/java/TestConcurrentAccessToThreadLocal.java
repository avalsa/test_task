import java.util.ConcurrentModificationException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


//start several threads with same ThreadLocal
// check no interleaving between threads
public class TestConcurrentAccessToThreadLocal {
    MyThreadLocal<Integer> tl = new MyThreadLocal<>();
    public static void main(String[] args) throws InterruptedException {
        final TestConcurrentAccessToThreadLocal testConcurrentAccessToThreadLocal = new TestConcurrentAccessToThreadLocal();
        ThreadPoolExecutor threads = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
        for (int i = 0; i < 5; i++) {
            threads.submit(testConcurrentAccessToThreadLocal::m);
        }
        threads.shutdown();
        threads.awaitTermination(1, TimeUnit.MINUTES);
    }


    private void m() {
        tl.set(0);
        int prev = 0;
        while (tl.get() < 10000000) {
            tl.set(prev + 1);
            if (prev + 1 != tl.get()) {
                throw new ConcurrentModificationException();
            } else {
                prev = tl.get();
            }
        }
        System.out.println("fine");
    }
}
