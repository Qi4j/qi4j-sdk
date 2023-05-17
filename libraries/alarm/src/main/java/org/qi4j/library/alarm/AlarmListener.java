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

package org.qi4j.library.alarm;

/**
 * Listener for AlarmEvents.
 * <code>AlarmPoint</code> classes will fire an <code>AlarmEvent</code> whenever
 * its <code>AlarmStatus</code> is altered.
 * <p><strong>Note:</strong>Since <code>AlarmStatus</code> of an <code>AlarmPoint</code>
 * may change <i>implicitly</i>, e.g. without proper execution of tha Java
 * Virtual Machine, there are situations when the sequence of <i>old state</i>
 * and <i>new state</i> in <code>AlarmEvents</code> will not match.
 *
 */
public interface AlarmListener
{

    /**
     * Method signature of the AlarmListener.
     *
     * @param event the event being fired.
     */
    void alarmFired( AlarmEvent event );
}

