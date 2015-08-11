/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/

#include <jni.h>
#include <stdio.h>
#include <errno.h>
#include <hwloc.h>
#include <sched.h>
#include <unistd.h>
#include <stdint.h> /* for uint64_t */
#include <time.h>
#include "SubMicroCore_jni.h"

 

/* assembly code to read the TSC */

static inline uint64_t RDTSC() {
    unsigned int hi, lo;
    __asm__ volatile("rdtsc" : "=a" (lo), "=d" (hi));
    return ((uint64_t)hi << 32) | lo;
}

static uint64_t NANO_SECONDS_IN_SEC = 1000000000LL;
static double g_TicksPerNanoSec;
static uint64_t i;
static double j = 123.45678;

static struct timespec *TimeSpecDiff(struct timespec *ts1, struct timespec *ts2) {
    static struct timespec ts;
    ts.tv_sec = ts1->tv_sec - ts2->tv_sec;
    ts.tv_nsec = ts1->tv_nsec - ts2->tv_nsec;
    if (ts.tv_nsec < 0) {
        ts.tv_sec--;
        ts.tv_nsec += NANO_SECONDS_IN_SEC;
    }
    return &ts;
}

static void CalibrateTicks() {
    struct timespec begints, endts;
    uint64_t begin = 0, end = 0;
    clock_gettime(CLOCK_MONOTONIC, &begints);
    begin = RDTSC();
    
    for( ; i < 300000000 ; i++) {
      j = (j * 1.12345) / 1.054321;
    }
    
    end = RDTSC();
    
    clock_gettime(CLOCK_MONOTONIC, &endts);
    struct timespec *tmpts = TimeSpecDiff(&endts, &begints);
    uint64_t nsecElapsed = tmpts->tv_sec * 1000000000LL + tmpts->tv_nsec;
    
    g_TicksPerNanoSec = (double)(end - begin)/(double)nsecElapsed;
    
    printf( "calibrateTicks timeMS=%d, ticksPerNanoSec=%f\n", (nsecElapsed/1000000), g_TicksPerNanoSec );
}

 
/* Call once before using RDTSC, has side effect of binding process to CPU1 */

JNIEXPORT void JNICALL Java_com_rr_core_os_NativeHooksImpl_jniCalibrateTicks( JNIEnv *env, jclass clazz ) {
    // dont need to use sched affinity we assume all cpus are same
    CalibrateTicks();
}

 
 /*
 Passing 0 as the pid, and it'll apply to the current thread only, or have other thread report their kernel pid 
 with the linux specific call pid_t gettid(void); and pass that in as the pid.

The affinity mask is actually a per-thread attribute that can be adjusted independently for each of the threads in a thread group. 
The value returned from a call to gettid(2) can be passed in the argument pid. 
Specifying pid as 0 will set the attribute for the calling thread, and passing the value returned from a call to getpid(2) 
will set the attribute for the main thread of the thread group. 
(If you are using the POSIX threads API, then use pthread_setaffinity_np (3) instead of sched_setaffinity().)
  */

JNIEXPORT void JNICALL Java_com_rr_core_os_NativeHooksImpl_jniSetPriority( JNIEnv *env, jclass clazz, jint cpumask, jint priority ) {

    int topodepth;
    hwloc_topology_t topology;
    hwloc_cpuset_t cpuset;

    hwloc_topology_init(&topology);
    hwloc_topology_load(topology);
    topodepth = hwloc_topology_get_depth(topology);

    cpuset = hwloc_bitmap_alloc();
    hwloc_bitmap_from_ulong( cpuset, (unsigned int)cpumask );

    char *str;
    hwloc_bitmap_asprintf(&str, cpuset);

    printf("cpumask [%d] => hwloc [%s]\n", cpumask, str);

    if (hwloc_set_cpubind(topology, cpuset, HWLOC_CPUBIND_THREAD)) {
        printf("Couldn't bind cpuset %s\n", str);
    } else {
        printf("BOUND cpuset %s\n", str);
    }

    free(str);

    /* Free our cpuset copy */
    hwloc_bitmap_free(cpuset);

    /* Destroy topology object. */
    hwloc_topology_destroy(topology);
}

JNIEXPORT void JNICALL Java_com_rr_core_os_NativeHooksImpl_jniSleep( JNIEnv *env, jclass clazz, jint sleepMS ) {

    struct timespec t,r;
    t.tv_sec = 0;
    t.tv_nsec = 1*1000*1000;
    int i = 0;
    for( ; i < sleepMS; i++) {
        nanosleep(&t, &r);
    }
}

JNIEXPORT jlong JNICALL Java_com_rr_core_os_NativeHooksImpl_jniNanoTimeRDTSC(JNIEnv *env, jclass clazz) {
    return( RDTSC() / g_TicksPerNanoSec );
}

JNIEXPORT jlong JNICALL Java_com_rr_core_os_NativeHooksImpl_jniNanoTimeMonotonicRaw(JNIEnv *env, jclass clazz) {
#ifndef CLOCK_MONOTONIC_RAW
    return( RDTSC() / g_TicksPerNanoSec );
#else
    struct timespec ts;
 
    clock_gettime(CLOCK_MONOTONIC_RAW,&ts);

    uint64_t nsecElapsed = ts.tv_sec * 1000000000LL + ts.tv_nsec;
    
    return nsecElapsed;
#endif    
}

JNIEXPORT void JNICALL Java_com_rr_core_os_NativeHooksImpl_jniSleepMicros( JNIEnv *env, jclass clazz, jint sleepMicros ) {

    struct timespec  timeout0;
    struct timespec  timeout1;
    struct timespec* tmp;
    struct timespec* t0 = &timeout0;
    struct timespec* t1 = &timeout1;
    
    t0->tv_sec = sleepMicros >> 20; // approx
    t0->tv_nsec = (sleepMicros & 0xFFFFF) << 10;
    
    while ( (nanosleep(t0, t1) == (-1)) && (errno == EINTR) ) {
        tmp = t0;
        t0 = t1;
        t1 = tmp;
    }
}

JNIEXPORT void JNICALL Java_com_rr_core_os_NativeHooksImpl_jniSetProcessMaxPriority( JNIEnv *env, jclass clazz ) {
    int max = sched_get_priority_max(SCHED_FIFO);
    struct sched_param p;
    printf( "Setting SCHED_FIFO priority %d\n", max );
    p.sched_priority = max;
    sched_setscheduler(0, SCHED_FIFO, &p);
    return;
}
 
#ifdef __i386__
#  define RDTSC_DIRTY "%eax", "%ebx", "%ecx", "%edx"
#elif __x86_64__
#  define RDTSC_DIRTY "%rax", "%rbx", "%rcx", "%rdx"
#else
# error unknown platform
#endif

#define RDTSC_START(cycles)                                \
    do {                                                   \
        register unsigned cyc_high, cyc_low;               \
        asm volatile("CPUID\n\t"                           \
                     "RDTSC\n\t"                           \
                     "mov %%edx, %0\n\t"                   \
                     "mov %%eax, %1\n\t"                   \
                     : "=r" (cyc_high), "=r" (cyc_low)     \
                     :: RDTSC_DIRTY);                      \
        (cycles) = ((uint64_t)cyc_high << 32) | cyc_low;   \
    } while (0)
    
#define RDTSC_STOP(cycles)                                 \
    do {                                                   \
        register unsigned cyc_high, cyc_low;               \
        asm volatile("RDTSCP\n\t"                          \
                     "mov %%edx, %0\n\t"                   \
                     "mov %%eax, %1\n\t"                   \
                     "CPUID\n\t"                           \
                     : "=r" (cyc_high), "=r" (cyc_low)     \
                     :: RDTSC_DIRTY);                      \
        (cycles) = ((uint64_t)cyc_high << 32) | cyc_low;   \
    } while(0)
    
JNIEXPORT jlong JNICALL Java_com_rr_core_os_NativeHooksImpl_jniNanoRDTSCStart(JNIEnv *env, jclass clazz) {
    long cycles;
    
    RDTSC_START( cycles );
    
    return( cycles );
}

JNIEXPORT jlong JNICALL Java_com_rr_core_os_NativeHooksImpl_jniNanoRDTSCStop(JNIEnv *env, jclass clazz) {
    long cycles;
    
    RDTSC_STOP( cycles );
    
    return( cycles );
}
        
