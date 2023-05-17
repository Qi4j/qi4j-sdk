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

package org.qi4j.api.metrics;

/**
 * Metrics Health Check.
 */
public interface MetricsHealthCheck extends Metric
{
    Result check()
        throws Exception;

    final class Result
    {
        private final boolean healthy;
        private final String message;
        private final Throwable exception;

        private Result( boolean isHealthy, String message, Throwable exception )
        {
            healthy = isHealthy;
            this.message = message;
            this.exception = exception;
        }

        /** Factory method for reporting an Ok health.
         *
         * @return A healthy result.
         */
        public static Result healthOk() {
            return new Result( true, "", null );
        }

        /** Factory method for reporting an unhealthy state.
         *
         * @param message The message to relay in the result.
         * @return An unhealthy result.
         */
        public static Result unhealthy(String message) {
            return new Result( false, message, null );
        }

        /** Factory method for reporting a state where an exception has occurred.
         *
         * @param message The message to relay in the result.
         * @param exception the exception that has occurred.
         * @return A failing health state.
         */
        public static Result exception(String message, Throwable exception) {
            return new Result( false, message, exception );
        }

        public boolean isHealthy()
        {
            return healthy;
        }

        public String getMessage()
        {
            return message;
        }

        public Throwable getException()
        {
            return exception;
        }
    }
}
