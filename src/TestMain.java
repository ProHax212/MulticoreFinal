import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main program for testing
 */
public class TestMain {

    public static void main(String[] args) {
        int numInserters = 10; int numDeleters = 10;
        int numInsert = 1000; int numDelete = 1000;

        fineGrainedTest(numInserters, numInsert, numDeleters, numDelete);
        //lockFreeTest(numInserters, numInsert, numDeleters, numDelete);
    }

    private static void fineGrainedTest(int numInserters, int numInsert, int numDeleters, int numDelete){
        FineGrainedPriorityQueue fineGrainedPriorityQueue = new FineGrainedPriorityQueue(1000);
        Random r = new Random();
        ExecutorService inserters = Executors.newFixedThreadPool(numInserters);
        ExecutorService deleters = Executors.newFixedThreadPool(numDeleters);

        for(int i = 0; i < numInserters; i++){
            inserters.submit(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < numInsert; i++){
                        int num = r.nextInt(10000);
                        fineGrainedPriorityQueue.insert(num, num);
                    }
                }
            });
        }

        for(int i = 0; i < numDeleters; i++){
            deleters.submit(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < numDelete; i++){
                        Integer num = fineGrainedPriorityQueue.deleteMin();
                        if(num != null) System.out.println(num);
                    }
                }
            });
        }

        try {
            inserters.shutdown();
            deleters.shutdown();
            inserters.awaitTermination(10, TimeUnit.SECONDS);
            deleters.awaitTermination(10, TimeUnit.SECONDS);
        }catch (InterruptedException e){

        }

        System.out.println(fineGrainedPriorityQueue.verify());
        System.out.println(fineGrainedPriorityQueue);
    }

    private static void lockFreeTest(int numInserters, int numInsert, int numDeleters, int numDelete){
        LockFreePriorityQueue lockFreePriorityQueue = new LockFreePriorityQueue();
        Random r = new Random();
        ExecutorService inserters = Executors.newFixedThreadPool(numInserters);
        ExecutorService deleters = Executors.newFixedThreadPool(numDeleters);

        for (int i = 0; i < numInserters; i++) {
            inserters.execute(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < numInsert; i++){
                        int num = r.nextInt(10000);
                        lockFreePriorityQueue.insert(num, num);
                    }
                }
            });
        }

        for (int i = 0; i < numDeleters; i++) {
            deleters.execute(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < numDelete; i++){
                        Integer num = lockFreePriorityQueue.deleteMin();
                        if(num != null) System.out.println(num);
                    }
                }
            });
        }

        try{
            inserters.shutdown();
            deleters.shutdown();
            inserters.awaitTermination(10, TimeUnit.SECONDS);
            deleters.awaitTermination(10, TimeUnit.SECONDS);
        }catch (InterruptedException e){}

        System.out.println(lockFreePriorityQueue);
        System.out.println(lockFreePriorityQueue.verify());
    }

}
