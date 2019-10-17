package edu.training.java.asynchronous_create;

import edu.training.java.synchronous_generate.FluxSequenceGenerator;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class SequenceCreatorTest {

    @Test
    public void whenCreatingNumbersThenSequenceIsProducedAsynchronously() throws InterruptedException {
        FluxSequenceGenerator sequenceGenerator = new FluxSequenceGenerator();
        List<Integer> sequence1 = sequenceGenerator.generateFibonacciSequence().take(3).collectList().block();
        List<Integer> sequence2 = sequenceGenerator.generateFibonacciSequence().take(4).collectList().block();
        List<Integer> result = new ArrayList<>();

        SequenceCreator sequenceCreator = new SequenceCreator();
        Flux<Integer> sequence = sequenceCreator.createNumberSequence();
        Thread producingThread1 = new Thread(() -> sequenceCreator.consumer.accept(sequence1));
        Thread producingThread2 = new Thread(() -> sequenceCreator.consumer.accept(sequence2));
        sequence.subscribe(result::add);
        producingThread1.start();
        producingThread2.start();
        producingThread1.join();
        producingThread2.join();

        Assertions.assertThat(result).containsExactlyInAnyOrder(0, 1, 1, 0, 1, 1, 2);
    }
}
