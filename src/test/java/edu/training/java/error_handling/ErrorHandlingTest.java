package edu.training.java.error_handling;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ErrorHandlingTest {

    @Test
    public void whenExceptionThrownDuringGeneration_thenErrorHandlerCalbackShouldBeCalled() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];
        Flux<Integer> flux = Flux.generate(() -> Tuples.of(0, 1), (tuple, sink) -> {
            sink.next(tuple.getT1());
            if (tuple.getT1() > 200) {
                throw new RuntimeException("Sequence too big");
            }
            return Tuples.of(tuple.getT2(), tuple.getT1() + tuple.getT2());
        });

        flux.subscribe(el -> {},
                ex -> {
                    error[0] = ex;
                    latch.countDown();
                });
        Assertions.assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        Assertions.assertThat(error[0]).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(error[0].getMessage()).isEqualTo("Sequence too big");

    }

    @Test
    public void whenExceptionThrownDuringOperation_thenErrorHandlerCalbackShouldBeCalled() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Throwable[] error = new Throwable[1];
        Flux<Integer> flux = Flux.range(0,100);

        flux.map(this::doSomethingDangerous)
                .subscribe(el -> {},
                ex -> {
                    error[0] = ex;
                    latch.countDown();
                });
        Assertions.assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        Assertions.assertThat(error[0]).isInstanceOf(RuntimeException.class);
        Assertions.assertThat(error[0].getMessage()).isEqualTo("Dangerous transformation");
    }

    @Test
    public void whenExceptionThrown_thenStaticFallbackValueShouldBeReturned(){
        Flux<Integer> flux = Flux.range(0,10);
        Flux<Integer> fallbackValue = flux.map(this::doSomethingDangerous)
                .onErrorReturn(-1);
        StepVerifier.create(fallbackValue).expectNext(-1).expectComplete().verify();
    }

    @Test
    public void whenExceptionThrownAndConditionSatisfied_thenFallbackValueShouldBeReturned(){
        Flux<Integer> flux = Flux.range(0,10);
        Flux<Integer> fallbackValue = flux.map(this::doSomethingDangerous)
                .onErrorReturn(t -> t.getMessage().equals("Dangerous transformation"), -1);
        StepVerifier.create(fallbackValue).expectNext(-1).expectComplete().verify();
    }

    @Test
    public void whenExceptionThrownAndConditionIsNotSatisfied_thenErrorHandlingCallbackShouldBeCalled(){
        Flux<Integer> flux = Flux.range(0,10);
        Flux<Integer> fallbackValue = flux.map(this::doSomethingDangerous)
                .onErrorReturn(t -> t.getMessage().equals("Dangerous"), -1);
        StepVerifier.create(fallbackValue).expectError().verify();
    }

    @Test
    public void whenExceptionThrown_thenShouldResumeToAnotherStream(){
        Flux<String> flux = Flux.just("A","B","C");
        Flux<String> resultFlux = flux
                .flatMap(this::callExternalService)
                .onErrorResume(t -> getFromCache());

        StepVerifier.create(resultFlux)
                .expectNext("1","2")
                .expectComplete()
                .verify();

    }

    private Flux<String> getFromCache(){
        return Flux.just("1","2");
    }
    private  <T> Flux<T> callExternalService(T el){
        throw new RuntimeException("Serviceis not available");
    }
    private <T> T doSomethingDangerous(T el){
        throw new RuntimeException("Dangerous transformation");
    }
}
