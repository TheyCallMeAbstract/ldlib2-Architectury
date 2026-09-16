package com.lowdragmc.lowdraglib2.event;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventBusTest {

    static class PingEvent extends Event {
    }

    static class OtherEvent extends Event {
    }

    static class BaseEvent extends Event {
    }

    static class DerivedEvent extends BaseEvent {
    }

    static class StaticListener {
        static final AtomicInteger CALLS = new AtomicInteger();

        @SubscribeEvent
        public static void onPing(PingEvent event) {
            CALLS.incrementAndGet();
        }
    }

    static class InstanceListener {
        final AtomicInteger calls = new AtomicInteger();

        @SubscribeEvent
        public void onPing(PingEvent event) {
            calls.incrementAndGet();
        }
    }

    static class SupertypeListener {
        final AtomicInteger calls = new AtomicInteger();

        @SubscribeEvent
        public void onBase(BaseEvent event) {
            calls.incrementAndGet();
        }
    }

    static class WrongEventListener {
        final AtomicInteger calls = new AtomicInteger();

        @SubscribeEvent
        public void onOther(OtherEvent event) {
            calls.incrementAndGet();
        }
    }

    static class ThrowingListener {
        @SubscribeEvent
        public void onPing(PingEvent event) {
            throw new IllegalStateException("deliberate listener failure");
        }
    }

    @Test
    void staticListenerReceivesMatchingEvent() {
        StaticListener.CALLS.set(0);
        var bus = new EventBus();

        bus.register(StaticListener.class);
        bus.post(new PingEvent());

        assertEquals(1, StaticListener.CALLS.get());
    }

    @Test
    void instanceListenerReceivesMatchingEvent() {
        var bus = new EventBus();
        var listener = new InstanceListener();

        bus.register(listener);
        bus.post(new PingEvent());

        assertEquals(1, listener.calls.get());
    }

    @Test
    void supertypeListenerReceivesSubtypeEvent() {
        var bus = new EventBus();
        var listener = new SupertypeListener();

        bus.register(listener);
        bus.post(new DerivedEvent());

        assertEquals(1, listener.calls.get());
    }

    @Test
    void listenerForAnotherEventTypeIsNotInvoked() {
        var bus = new EventBus();
        var listener = new WrongEventListener();

        bus.register(listener);
        bus.post(new PingEvent());

        assertEquals(0, listener.calls.get());
    }

    @Test
    void postingWithNoListenersIsANoOp() {
        var bus = new EventBus();

        assertTrue(bus.isEmpty());
        assertDoesNotThrow(() -> bus.post(new PingEvent()));
        assertTrue(bus.isEmpty());
    }

    @Test
    void throwingListenerDoesNotPreventLaterListenerOrPropagate() {
        var bus = new EventBus();
        var counting = new InstanceListener();

        bus.register(new ThrowingListener());
        bus.register(counting);

        assertDoesNotThrow(() -> bus.post(new PingEvent()));
        assertFalse(bus.isEmpty());
        assertEquals(1, counting.calls.get());
    }
}
