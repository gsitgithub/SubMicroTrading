/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.tasks;

public abstract class ZTimerTask {

    public enum TaskState {
        INITIAL, SCHEDULED, EXECUTING, EXECUTED, CANCELLED, 
    }

    private final String _name;

    private TaskState _taskState    = TaskState.INITIAL;

    private long      _nextFireTime;
    private long      _taskInterval = 0;

    public ZTimerTask( String name ) {
       _name = name;
    }

    /**
     * fire the task within the timers thread of control
     */
    public abstract void fire();

    public synchronized void cancel() {
        _taskState = TaskState.CANCELLED;
    }

    public void scheduleNext() {
        _nextFireTime = _nextFireTime + _taskInterval;
    }

    public final TaskState getTaskState() {
        return _taskState;
    }

    public final long getNextFireTime() {
        return _nextFireTime;
    }

    public final long getTaskInterval() {
        return _taskInterval;
    }

    public boolean isCancelled() {
        return _taskState == TaskState.CANCELLED;
    }

    final void setTaskState( TaskState taskState ) {
        _taskState = taskState;
    }

    final void setNextFireTime( long nextFireTime ) {
        _nextFireTime = nextFireTime;
    }

    final void setTaskInterval( long taskInterval ) {
        _taskInterval = taskInterval;
    }

    public String getName() {
        return _name;
    }

    // initialiser used by the ZTimer
    void init( long nextFireTime, long repeatInterval ) {
        setTaskState( TaskState.SCHEDULED );
        
        setNextFireTime( nextFireTime );
        setTaskInterval( repeatInterval );
    }
}