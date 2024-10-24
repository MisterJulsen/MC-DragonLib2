package de.mrjulsen.mcdragonlib.util;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;

public class WorkerAsync {

    private final String name;
    private final Logger logger;
    private final Thread thread;
    private final Queue<Runnable> taskQueue = new LinkedList<>();
    private volatile boolean running = true;

    public WorkerAsync(String name, Logger logger) {
        this.name = name;
        this.logger = logger;
        this.thread = new Thread(() -> {
            logger.info(name + " worker thread has been started.");
            while (running) {
                Runnable task = null;
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty() && running) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            logger.error("Error while waiting for tasks.", e);
                        }
                    }

                    if (!taskQueue.isEmpty()) {
                        task = taskQueue.poll();
                    }
                }

                if (task != null) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        logger.error("Error while executing task.", e);
                    }
                }
            }
            logger.info(name + " worker thread has been stopped!");
        }, name);
    }

    public String getName() {
        return name;
    }

    public void start() {
        logger.info("Starting " + name + " worker thread...");
        taskQueue.clear();
        running = true;
        thread.start();
    }

    public void queueTask(Runnable task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notify();
        }
    }
    
    public void clearTasks() {
        synchronized (taskQueue) {
            taskQueue.clear();
            taskQueue.notify();
        }
    }

    public void stop() {
        this.logger.info("Stopping " + name + " worker thread...");
        synchronized (taskQueue) {
            running = false;
            clearTasks();
        }
    }
}
