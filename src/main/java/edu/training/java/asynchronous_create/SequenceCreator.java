package edu.training.java.asynchronous_create;

import edu.training.java.synchronous_generate.FluxSequenceGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SequenceCreator {
    public Consumer<List<Integer>> consumer;

    public Flux<Integer>  createNumberSequence() {
        return Flux.create(sink -> this.consumer = items -> items.forEach(i -> this.slowSink(sink,i)));
    }

    private void slowSink(FluxSink<Integer> sink,Integer i){
        try {
            Thread.sleep(1000);
            sink.next(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FluxSequenceGenerator sequenceGenerator = new FluxSequenceGenerator();
        List<Integer> sequence1 = sequenceGenerator.generateFibonacciSequence().take(3).collectList().block();
        List<Integer> sequence2 = sequenceGenerator.generateFibonacciSequence().take(4).collectList().block();

        SequenceCreator sequenceCreator = new SequenceCreator();
        Flux<Integer> sequence = sequenceCreator.createNumberSequence();
        Thread producingThread1 = new Thread(() -> sequenceCreator.consumer.accept(sequence1));
        Thread producingThread2 = new Thread(() -> sequenceCreator.consumer.accept(sequence2));
        sequence.subscribe(System.out::println);
        producingThread1.start();
        producingThread2.start();
    }
}