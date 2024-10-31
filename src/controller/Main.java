package controller;

import model.Reader;
import model.Writer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
    private static List<String> words = new ArrayList<>();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final Object lock = new Object();

    public static void main(String[] args) {
        try {
            loadWordsFromFile("src/resources/bd.txt");
        } catch (IOException e) {
            System.out.println("Erro ao carregar o arquivo: " + e.getMessage());
            return;
        }

        long startTotalTime = System.currentTimeMillis(); // Início do tempo total

        // StringBuilder para acumular as saídas
        StringBuilder output = new StringBuilder();

        output.append("==== Iniciando Implementação com Prioridade ====\n");
        long startPriorityTime = System.currentTimeMillis();
        output.append(runImplementation(true));  // Executa com prioridade e acumula a saída
        long endPriorityTime = System.currentTimeMillis();
        long priorityTimeMinutes = (endPriorityTime - startPriorityTime) / 60000;
        output.append("Tempo total para Implementação com Prioridade: ").append(priorityTimeMinutes).append(" min\n\n");

        output.append("==== Iniciando Implementação sem Prioridade ====\n");
        long startNoPriorityTime = System.currentTimeMillis();
        output.append(runImplementation(false)); // Executa sem prioridade e acumula a saída
        long endNoPriorityTime = System.currentTimeMillis();
        long noPriorityTimeMinutes = (endNoPriorityTime - startNoPriorityTime) / 60000;
        output.append("Tempo total para Implementação sem Prioridade: ").append(noPriorityTimeMinutes).append(" min\n\n");

        long endTotalTime = System.currentTimeMillis(); // Fim do tempo total
        long totalTimeMinutes = (endTotalTime - startTotalTime) / 60000;
        output.append("==== Tempo total do processo: ").append(totalTimeMinutes).append(" min ====\n");

        // Exibe toda a saída acumulada de uma vez
        System.out.println(output.toString());
    }

    private static StringBuilder runImplementation(boolean usePriority) {
        StringBuilder output = new StringBuilder();

        for (int readersCount = 0; readersCount <= 100; readersCount++) {
            int writersCount = 100 - readersCount;
            long totalTime = 0;

            for (int run = 0; run < 10; run++) { // Ajuste para o número de rodadas
                List<Thread> threads = createThreads(readersCount, writersCount, usePriority);

                long startTime = System.currentTimeMillis();

                for (Thread thread : threads) {
                    thread.start();
                }

                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime);
            }

            long averageTime = totalTime / 10; // Calcula o tempo médio
            output.append(String.format("Proporção: %d Readers, %d Writers - Tempo médio: %d ms%n", readersCount, writersCount, averageTime));
        }

        return output;
    }

    private static List<Thread> createThreads(int readersCount, int writersCount, boolean usePriority) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < readersCount; i++) {
            threads.add(new Reader(words, usePriority ? rwLock : lock));
        }
        for (int i = 0; i < writersCount; i++) {
            threads.add(new Writer(words, usePriority ? rwLock : lock));
        }
        Collections.shuffle(threads);
        return threads;
    }

    private static void loadWordsFromFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line.trim());
            }
            System.out.println("Arquivo carregado com sucesso. Total de palavras: " + words.size());
        }
    }
}
