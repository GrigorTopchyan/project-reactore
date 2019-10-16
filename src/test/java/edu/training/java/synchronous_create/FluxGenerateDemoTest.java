package edu.training.java.synchronous_create;

import edu.training.java.synchronous_generate.FluxSequenceGenerator;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class FluxGenerateDemoTest {

    @Test
    public void generateFibonacciSequenceProduceFibonacciSequence(){
        FluxSequenceGenerator sequenceGenerator = new FluxSequenceGenerator();
        Flux<Integer> fibonacciFlux = sequenceGenerator.generateFibonacciSequence().take(5);
        StepVerifier.create(fibonacciFlux)
                .expectNext(0,1,1,2,3)
                .expectComplete()
                .verify();
    }
}
