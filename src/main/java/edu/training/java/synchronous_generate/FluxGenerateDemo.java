package edu.training.java.synchronous_generate;

import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicLong;

public class FluxGenerateDemo {
    public static void main(String[] args) {
        Flux<String> flux = Flux.generate(() -> 0,
                (state, sunk) -> {
                    sunk.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) sunk.complete();
                    return state + 1;
                }
        );

        //flux.subscribe(System.out::println);

        Flux<String> flux1 = Flux.generate(AtomicLong::new,
                (state, sink) -> {
                    long i = state.incrementAndGet();
                    sink.next("3 x " + state + " = " + 3 * i);
                    if (i == 10) sink.complete();
                    return state;
                }, state -> {
                    System.out.println("State clean up ");
                    state.set(0);
                }
        );
        flux1.subscribe(System.out::println);
    }
}
