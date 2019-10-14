package edu.training.java.fluxmono;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public class FluxMonoDemo {
    public static void main(String[] args) {
        Flux<String> flux = Flux.just("A","B","C");
        Iterable<String> iterable = List.of("A","B","C");
        Flux<String> flux1 = Flux.fromIterable(iterable);
        Flux<Integer> fiveToNine = Flux.range(5,4);

        Mono<String> empty = Mono.empty();
        Mono<String> data = Mono.just("A");

        flux.subscribe();
        flux1.subscribe(System.out::println);
        fiveToNine.map(FluxMonoDemo::mapperWithException)
                .subscribe(System.out::println,System.err::println, () -> System.out.println("Done"));
    }

    private static Integer mapperWithException(int el){
        if (el == 7){
         //   throw new RuntimeException("Reached to 7");
        }
        return el;
    }
}
