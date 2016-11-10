import java.util.Random;

/**
 * Main program for testing
 */
public class TestMain {

    public static void main(String[] args){
        // LOCK FREE PRIORITY QUEUE TESTING
        int numInsert = 100;
        LockFreePriorityQueue lockFreePriorityQueue = new LockFreePriorityQueue();

        for(int i = 0; i < numInsert; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int numToAdd = 100;
                    for(int i = 0; i < numToAdd; i++){
                        int num = Integer.parseInt(Thread.currentThread().getName()) * numToAdd + i;
                        lockFreePriorityQueue.insert(num, num);
                    }
                }
            });
            thread.setName(Integer.toString(i));
            thread.start();
        }

        try{Thread.sleep(1000);}
        catch (InterruptedException e){}

        System.out.println("Done");
        System.out.println(lockFreePriorityQueue);
        System.out.print(lockFreePriorityQueue.verify());





        // LOCK FREE SKIP LIST TESTING
        /*LockFreeSkipList skipList = new LockFreeSkipList();
        int numAdd=5, numRemove=4;

        for(int i = 0; i < numAdd; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 5; i++){
                        skipList.add(i + (Integer.parseInt(Thread.currentThread().getName()) * 5));
                    }
                }
            });
            thread.setName(Integer.toString(i));
            thread.start();
        }

        for(int i = 0; i < numRemove; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 5; i++){
                        skipList.remove(i + (Integer.parseInt(Thread.currentThread().getName()) * 5));
                    }
                }
            });
            thread.setName(Integer.toString(i));
            thread.start();
        }

        try{Thread.sleep(1000);}
        catch (InterruptedException e){}

        skipList.print();*/



        // LOCK FREE PRIORITY QUEUE TESTING
        /*LockFreePriorityQueue queue = new LockFreePriorityQueue();

        SequentialSkipList skipList = new SequentialSkipList();
        Random r = new Random();

        int count = 0;
        for(int i = 0; i < 100; i++){
            int n = r.nextInt(1000);
            if(skipList.insert(n, n)) count += 1;
        }
        for(int i = 0; i < count; i++) System.out.println(skipList.deleteMin());*/



        FineGrainedPriorityQueue queue = new FineGrainedPriorityQueue();
        Random r = new Random();

        for(int i = 0; i < 5; i++){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < 5; i++){
                        int num = r.nextInt(50);
                        queue.insert(num, num);
                    }
                }
            });
            thread.start();
        }

//        try{Thread.sleep(500);}
//        catch (InterruptedException e){}
//
//        System.out.println(queue);
    }

}
