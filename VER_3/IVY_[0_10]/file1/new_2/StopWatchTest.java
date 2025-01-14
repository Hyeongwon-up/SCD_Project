/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;

/**
 * TestCase for StopWatch.
 */
public class StopWatchTest {

    /**
     * <p>
     * Creates a suspended StopWatch object which appears to have elapsed
     * for the requested amount of time in nanoseconds.
     * <p>
     * <p>
     * <pre>
     * // Create a mock StopWatch with a time of 2:59:01.999
     * final long nanos = TimeUnit.HOURS.toNanos(2)
     *         + TimeUnit.MINUTES.toNanos(59)
     *         + TimeUnit.SECONDS.toNanos(1)
     *         + TimeUnit.MILLISECONDS.toNanos(999);
     * final StopWatch watch = createMockStopWatch(nanos);
     * </pre>
     *
     * @param nanos Time in nanoseconds to have elapsed on the stop watch
     * @return StopWatch in a suspended state with the elapsed time
     */
    private StopWatch createMockStopWatch(final long nanos) {
        final StopWatch watch = StopWatch.createStarted();
        watch.suspend();
        try {
            final long currentNanos = System.nanoTime();
            FieldUtils.writeField(watch, "startTime", currentNanos - nanos, true);
            FieldUtils.writeField(watch, "stopTime", currentNanos, true);
        } catch (final IllegalAccessException e) {
            return null;
        }
        return watch;
    }

    // test bad states
    @Test
    public void testBadStates() {
        final StopWatch watch = new StopWatch();
        assertThrows(
                IllegalStateException.class,
                watch::stop,
                "Calling stop on an unstarted StopWatch should throw an exception. ");

        assertThrows(
                IllegalStateException.class,
                watch::suspend,
                "Calling suspend on an unstarted StopWatch should throw an exception. ");

        assertThrows(
                IllegalStateException.class,
                watch::split,
                "Calling split on a non-running StopWatch should throw an exception. ");

        assertThrows(
                IllegalStateException.class,
                watch::unsplit,
                "Calling unsplit on an unsplit StopWatch should throw an exception. ");

        assertThrows(
                IllegalStateException.class,
                watch::resume,
                "Calling resume on an unsuspended StopWatch should throw an exception. ");

        watch.start();

        assertThrows(
                IllegalStateException.class,
                watch::start,
                "Calling start on a started StopWatch should throw an exception. ");

        assertThrows(
                IllegalStateException.class,
                watch::unsplit,
                "Calling unsplit on an unsplit StopWatch should throw an exception. ");

        assertThrows(
                IllegalStateException.class,
                watch::getSplitTime,
                "Calling getSplitTime on an unsplit StopWatch should throw an exception. ");

        assertThrows(
                IllegalStateException.class,
                watch::resume,
                "Calling resume on an unsuspended StopWatch should throw an exception. ");

        watch.stop();

        assertThrows(
                IllegalStateException.class,
                watch::start,
                "Calling start on a stopped StopWatch should throw an exception as it needs to be reset. ");
    }

    @Test
    public void testBooleanStates() {
        final StopWatch watch = new StopWatch();
        assertFalse(watch.isStarted());
        assertFalse(watch.isSuspended());
        assertTrue(watch.isStopped());

        watch.start();
        assertTrue(watch.isStarted());
        assertFalse(watch.isSuspended());
        assertFalse(watch.isStopped());

        watch.suspend();
        assertTrue(watch.isStarted());
        assertTrue(watch.isSuspended());
        assertFalse(watch.isStopped());

        watch.stop();
        assertFalse(watch.isStarted());
        assertFalse(watch.isSuspended());
        assertTrue(watch.isStopped());
    }

    @Test
    public void testFormatSplitTime() throws InterruptedException {
        final StopWatch watch = StopWatch.createStarted();
        Thread.sleep(20);
        watch.split();
        assertNotEquals("00:00:00.000", watch.formatSplitTime());
    }

    @Test
    public void testFormatTime() {
        final StopWatch watch = StopWatch.create();
        assertEquals("00:00:00.000", watch.formatTime());
    }

    @Test
    public void testGetStartTime() {
        final long beforeStopWatch = System.currentTimeMillis();
        final StopWatch watch = new StopWatch();
        assertThrows(
                IllegalStateException.class,
                watch::getStartTime,
                "Calling getStartTime on an unstarted StopWatch should throw an exception");
        watch.start();

        watch.getStartTime();
        assertTrue(watch.getStartTime() >= beforeStopWatch);

        watch.reset();
        assertThrows(
                IllegalStateException.class,
                watch::getStartTime,
                "Calling getStartTime on a reset, but unstarted StopWatch should throw an exception");
    }

    @Test
    public void testLang315() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            Thread.sleep(200);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.suspend();
        final long suspendTime = watch.getTime();
        try {
            Thread.sleep(200);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.stop();
        final long totalTime = watch.getTime();
        assertEquals(suspendTime, totalTime);
    }

    @Test
    public void testStopWatchGetWithTimeUnit() {
        // Create a mock StopWatch with a time of 2:59:01.999
        final StopWatch watch = createMockStopWatch(
                TimeUnit.HOURS.toNanos(2)
                        + TimeUnit.MINUTES.toNanos(59)
                        + TimeUnit.SECONDS.toNanos(1)
                        + TimeUnit.MILLISECONDS.toNanos(999));

        assertEquals(2L, watch.getTime(TimeUnit.HOURS));
        assertEquals(179L, watch.getTime(TimeUnit.MINUTES));
        assertEquals(10741L, watch.getTime(TimeUnit.SECONDS));
        assertEquals(10741999L, watch.getTime(TimeUnit.MILLISECONDS));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testStopWatchSimple() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.stop();
        final long time = watch.getTime();
        assertEquals(time, watch.getTime());

        assertTrue(time >= 500);
        assertTrue(time < 700);

        watch.reset();
        assertEquals(0, watch.getTime());
    }

    @Test
    public void testStopWatchSimpleGet() {
        final StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
        assertEquals("00:00:00.000", watch.toString());

        watch.start();
        try {
            Thread.sleep(500);
        } catch (final InterruptedException ex) {
            // ignore
        }
        assertTrue(watch.getTime() < 2000);
    }

    @Test
    public void testStopWatchSplit() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.split();
        final long splitTime = watch.getSplitTime();
        final String splitStr = watch.toSplitString();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.unsplit();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.stop();
        final long totalTime = watch.getTime();

        assertEquals(splitStr.length(), 12, "Formatted split string not the correct length");
        assertTrue(splitTime >= 500);
        assertTrue(splitTime < 700);
        assertTrue(totalTime >= 1500);
        assertTrue(totalTime < 1900);
    }

    @Test
    public void testStopWatchStatic() {
        final StopWatch watch = StopWatch.createStarted();
        assertTrue(watch.isStarted());
    }

    @Test
    public void testStopWatchSuspend() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.suspend();
        final long suspendTime = watch.getTime();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.resume();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.stop();
        final long totalTime = watch.getTime();

        assertTrue(suspendTime >= 500);
        assertTrue(suspendTime < 700);
        assertTrue(totalTime >= 1000);
        assertTrue(totalTime < 1300);
    }

    @Test
    public void testToSplitString() {
        final StopWatch watch = StopWatch.createStarted();
        try {
            Thread.sleep(550);
        } catch (final InterruptedException ex) {
            // ignore
        }
        watch.split();
        final String splitStr = watch.toSplitString();
        assertEquals(splitStr.length(), 12, "Formatted split string not the correct length");
    }
}
