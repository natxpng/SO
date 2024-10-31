package model;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Reader extends Thread {
    private final List<String> words;
    private final Object lock;

    public Reader(List<String> words, Object lock) {
        this.words = words;
        this.lock = lock;
    }

    @Override
    public void run() {
        if (lock instanceof ReentrantReadWriteLock) {
            ReentrantReadWriteLock rwLock = (ReentrantReadWriteLock) lock;
            rwLock.readLock().lock(); // Região crítica para leitores
            try {
                for (int i = 0; i < 100; i++) {
                    int index = (int) (Math.random() * words.size());
                    String word = words.get(index);
                }
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                rwLock.readLock().unlock();
            }
        } else {
            synchronized (lock) { // Região crítica sem prioridade
                try {
                    for (int i = 0; i < 100; i++) {
                        int index = (int) (Math.random() * words.size());
                        String word = words.get(index);
                    }
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
