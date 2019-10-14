package edu.training.java.subscriber;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

public class SampleSubscriber<T> extends BaseSubscriber<T> {
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        System.out.println("Subscribed!");
        request(1);
    }

    @Override
    protected void hookOnNext(T value) {
        System.out.println(value);
        request(1);
    }

    @Override
    protected void hookOnComplete() {
        System.out.println("Done!");
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        System.out.println("Error " + throwable);
    }
}
