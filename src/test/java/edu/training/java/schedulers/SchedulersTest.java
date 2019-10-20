package edu.training.java.schedulers;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

public class SchedulersTest {

    @Test
    public void fluxIntervalWithoutScheduler() throws InterruptedException {
        CountDownLatch barier = new CountDownLatch(1);
        Flux.interval(Duration.ofMillis(300))
                .take(10)
                .subscribe(el -> this.identityWithThreadLogging(el,""),Throwable::printStackTrace, barier::countDown);
        barier.await();
    }

    @Test
    public void fluxIntervalWithScheduler() throws InterruptedException {
        CountDownLatch barier = new CountDownLatch(1);
        Flux.interval(Duration.ofMillis(300),Schedulers.newSingle("test"))
                .take(10)
                .subscribe(el -> this.identityWithThreadLogging(el,""),Throwable::printStackTrace,barier::countDown);
        barier.await();
    }

    @Test
    public void fluxSubscribeOnScheduler() throws InterruptedException {
        CountDownLatch barier = new CountDownLatch(1);
        Flux.just("1","2","3","4")
                .subscribeOn(Schedulers.elastic())
                .map(el -> this.identityWithThreadLogging(el,"map"))
                .subscribeOn(Schedulers.single())
                .subscribe(el -> this.identityWithThreadLogging(el,"subscribe"),Throwable::printStackTrace,barier::countDown);
        barier.await();
    }

    @Test
    public void fluxPublishOnScheduler() throws InterruptedException {
        CountDownLatch barier = new CountDownLatch(1);
        Flux.just("1","2","3","4","5")
                .subscribeOn(Schedulers.single())
                .map(el -> this.identityWithThreadLogging(el,"map1"))
                .publishOn(Schedulers.parallel())
                .map(el -> this.identityWithThreadLogging(el,"map2"))
                .subscribe(el -> this.identityWithThreadLogging(el,"subscribe"),Throwable::printStackTrace,barier::countDown);
        barier.await();
    }

    @Test
    public void flatMapWithoutChangingScheduler() throws InterruptedException {
        CountDownLatch barier = new CountDownLatch(1);
        Flux.range(1,3)
                .map(el -> this.identityWithThreadLogging(el,"map1"))
                .flatMap(el -> Flux.range(el * 10,3).map(el1-> this.identityWithThreadLogging(el,"inner map")))
                .subscribeOn(Schedulers.parallel())
                .subscribe(el -> this.identityWithThreadLogging(el,"subscribe"),Throwable::printStackTrace,barier::countDown);
        barier.await();
    }

    @Test
    public void flatMapWithChangingScheduler() throws InterruptedException {
        CountDownLatch barier = new CountDownLatch(1);
        Flux.range(1,3)
                .map(el -> this.identityWithThreadLogging(el,"map1"))
                .flatMap(el -> Flux.range(el * 10,3).map(el1-> this.identityWithThreadLogging(el,"inner map")).subscribeOn(Schedulers.elastic()))
                .subscribeOn(Schedulers.parallel())
                .subscribe(el -> this.identityWithThreadLogging(el,"subscribe"),Throwable::printStackTrace,barier::countDown);
        barier.await();
    }

    @Test
    public void flatMapWithDifferentSchedulers() throws InterruptedException {
        CountDownLatch barier = new CountDownLatch(1);
        Flux.range(1,4)
                .subscribeOn(Schedulers.immediate())
                .map(el -> this.identityWithThreadLogging(el,"map1"))
                .flatMap(el -> {
                    if (el == 1) return createMonoOnScheduler(el,Schedulers.parallel());
                    if (el == 2) return createMonoOnScheduler(el,Schedulers.elastic());
                    if (el == 3) return createMonoOnScheduler(el,Schedulers.single());
                    return Mono.error(new Exception("error")).subscribeOn(Schedulers.newSingle("error-thread"));
                })
                .map(el -> this.identityWithThreadLogging(el,"map2"))
                .subscribe(
                success -> System.out.println(identityWithThreadLogging(success, "subscribe")),
                error -> System.err.println(identityWithThreadLogging(error, "subscribe, err").getMessage()),
                barier::countDown
        );
        barier.await();
    }

    private <T> T identityWithThreadLogging(T el,String operation){
        System.out.println(operation + " -- " + el + " -- " + Thread.currentThread().getName() + (Thread.currentThread().isDaemon()? " is diamon": " is not diamon"));
        return el;
    }
    private <T> Mono<T> createMonoOnScheduler(T el, Scheduler scheduler){
        return Mono.just(el)
                .map(e -> this.identityWithThreadLogging(e,"inner map"))
                .subscribeOn(scheduler);
    }
}
