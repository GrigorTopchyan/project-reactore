package edu.training.java.asynchronous_create;

import edu.training.java.synchronous_generate.FluxSequenceGenerator;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

public class SequenceCreator {
    public Consumer<List<Integer>> consumer;

    public Flux<Integer>  createNumberSequence() {
        return Flux.create(sink -> this.consumer = items -> items.forEach(sink::next));
    }

    public static void main(String[] args) {
        FluxSequenceGenerator sequenceGenerator = new FluxSequenceGenerator();
        List<Integer> sequence1 = sequenceGenerator.generateFibonacciSequence().take(3).collectList().block();
        List<Integer> sequence2 = sequenceGenerator.generateFibonacciSequence().take(4).collectList().block();

        SequenceCreator sequenceCreator = new SequenceCreator();
        Thread producingThread1 = new Thread(() -> sequenceCreator.consumer.accept(sequence1));
        Thread producingThread2 = new Thread(() -> sequenceCreator.consumer.accept(sequence2));
        sequenceCreator.createNumberSequence().subscribe(System.out::println);
    }
}