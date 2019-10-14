package edu.training.java.subscriber;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class SbscriberDemo {
    public static void main(String[] args) {
        SampleSubscriber<Integer> subscriber = new SampleSubscriber<>();
        Flux<Integer> ints = Flux.range(1, 5);
        ints.subscribe(subscriber);

        Flux<String> numbers = Flux.just("1", "2", "3", "a");
        numbers.map(Integer::parseInt)
                .subscribe(System.out::println,
                        System.err::println,
                        () -> System.out.println("Done!"),
                        s -> s.request(10));
    }
}
