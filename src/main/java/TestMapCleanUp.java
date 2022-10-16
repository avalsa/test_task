import java.util.concurrent.CountDownLatch;

//create several threads with threadLocal
//and await while finished threads will be cleaned up from ThreadLocal
public class TestMapCleanUp {
    MyThreadLocal<Integer> tl = new MyThreadLocal<>();
    public static void main(String[] args) throws InterruptedException {
        TestMapCleanUp testMapCleanUp = new TestMapCleanUp();
        execTLTasks(testMapCleanUp);
        System.out.println("await clean up");
        while (MyThreadLocal.size() > 1) {
            testMapCleanUp.tl.set(0); //clean up only during incoming requests
            System.gc();
        }
        System.out.println("fine");
    }

    private static void execTLTasks(TestMapCleanUp testMapCleanUp) throws InterruptedException {
        CountDownLatch ck = new CountDownLatch(3);
        new Thread(() -> testMapCleanUp.m(ck)) {
            @Override
            protected void finalize() throws Throwable {
                System.out.println("gced");
                super.finalize();
            }
        }.start();
        new Thread(() -> testMapCleanUp.m(ck)) {
            @Override
            protected void finalize() throws Throwable {
                System.out.println("gced");
                super.finalize();
            }
        }.start();
        new Thread(() -> testMapCleanUp.m(ck)) {
            @Override
            protected void finalize() throws Throwable {
                System.out.println("gced");
                super.finalize();
            }
        }.start();

        ck.await(); //all started
    }


    void m(CountDownLatch ck) {
        tl.set(0);
        ck.countDown();
        System.out.println("m done");
    }

}
