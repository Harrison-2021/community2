package com.nowcoder.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTest {
    /**
     * 主线程模拟调用生产者和消费者线程
     * @param args
     */
    public static void main(String[] args) {
        // 实例化一共阻塞队列-使用ArrayBlockingQueue实现
        BlockingQueue queue = new ArrayBlockingQueue(10);
        new Thread(new Producer(queue), "生产者-1线程:").start();
        new Thread(new Consumer(queue), "消费者-1线程:").start();
        new Thread(new Consumer(queue), "消费者-2线程:").start();
        new Thread(new Consumer(queue), "消费者-3线程:").start();
    }
}

/**
 * 生产者线程业务逻辑定义
 */
class Producer implements Runnable {
    // 每个线程都初始化一个队列来接收传过来的阻塞队列
    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override

    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产：" + queue.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 消费者线程业务逻辑定义
 */
class Consumer implements Runnable {
    // 每个线程都初始化一个队列来接收传过来的阻塞队列
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + "消费：" + queue.size());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}