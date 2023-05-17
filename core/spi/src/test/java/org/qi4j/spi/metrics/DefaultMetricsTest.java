/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.qi4j.spi.metrics;

import java.time.Instant;
import org.qi4j.api.metrics.MetricsCounter;
import org.qi4j.api.metrics.MetricsCounterFactory;
import org.qi4j.api.metrics.MetricsGauge;
import org.qi4j.api.metrics.MetricsGaugeFactory;
import org.qi4j.api.metrics.MetricsHealthCheck;
import org.qi4j.api.metrics.MetricsHealthCheckFactory;
import org.qi4j.api.metrics.MetricsHistogram;
import org.qi4j.api.metrics.MetricsHistogramFactory;
import org.qi4j.api.metrics.MetricsMeter;
import org.qi4j.api.metrics.MetricsMeterFactory;
import org.qi4j.api.metrics.MetricsProvider;
import org.qi4j.api.metrics.MetricsTimer;
import org.qi4j.api.metrics.MetricsTimerFactory;
import org.qi4j.api.time.SystemTime;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

public class DefaultMetricsTest
{
    @Test
    public void givenMetricsProviderWithoutSupportForCounterWhenRequestingCounterExpectDefaultNullImplementation()
    {
        MetricsProvider underTest = new MetricsProviderAdapter();
        MetricsCounterFactory factory = underTest.createFactory( MetricsCounterFactory.class );
        MetricsCounter test = factory.createCounter( "test" );
        test.increment();
        test.decrement();
    }

    @Test
    public void givenMetricsProviderWithoutSupportForGaugeWhenRequestingGaugeExpectDefaultNullImplementation()
    {
        MetricsProvider underTest = new MetricsProviderAdapter();
        MetricsGaugeFactory factory = underTest.createFactory( MetricsGaugeFactory.class );
        MetricsGauge<Instant> test = factory.registerGauge( "test", new MetricsGauge<Instant>()
        {
            @Override
            public Instant value()
            {
                return SystemTime.now();
            }
        } );
        assertThat( test.value(), nullValue() );
    }

    @Test
    public void givenMetricsProviderWithoutSupportForHealthCheckWhenRequestingHealthCheckExpectDefaultNullImplementation()
        throws Exception
    {
        MetricsProvider underTest = new MetricsProviderAdapter();
        MetricsHealthCheckFactory factory = underTest.createFactory( MetricsHealthCheckFactory.class );
        MetricsHealthCheck test = factory.registerHealthCheck( "test", new MetricsHealthCheck()
        {
            @Override
            public Result check()
                throws Exception
            {
                throw new RuntimeException( "Not healthy!!!" );
            }
        } );
        test.check(); // Should not throw an exception, as it should have been replaced by a null implementation.
    }

    @Test
    public void givenMetricsProviderWithoutSupportForHistogramWhenRequestingHistogramExpectDefaultNullImplementation()
    {
        MetricsProvider underTest = new MetricsProviderAdapter();
        MetricsHistogramFactory factory = underTest.createFactory( MetricsHistogramFactory.class );
        MetricsHistogram test = factory.createHistogram( "test" );
        test.update( 5L );
        test.update( 5L );
        test.update( 5L );
    }

    @Test
    public void givenMetricsProviderWithoutSupportForMeterWhenRequestingMeterExpectDefaultNullImplementation()
    {
        MetricsProvider underTest = new MetricsProviderAdapter();
        MetricsMeterFactory factory = underTest.createFactory( MetricsMeterFactory.class );
        MetricsMeter test = factory.createMeter( "test" );
        test.mark();
        test.mark();
        test.mark();
    }

    @Test
    public void givenMetricsProviderWithoutSupportForTimerWhenRequestingTimerExpectDefaultNullImplementation()
    {
        MetricsProvider underTest = new MetricsProviderAdapter();
        MetricsTimerFactory factory = underTest.createFactory( MetricsTimerFactory.class );
        MetricsTimer test = factory.createTimer( "test" );
        test.start().stop();
    }
}
