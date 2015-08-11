/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/

/* BASED ON SUN JDK1.6.22 but with all thread safeness (locking) & temp object creation removed */

/* multicast code by RR */

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <hwloc.h>
#include <sched.h>
#include <unistd.h>
#include <string.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <onload/extensions.h>

#include "LinuxSocket_jni.h"

#include "jni_util.h"
#include "jvm.h"
#include "jlong.h"
#include "nio.h"

#include "net_util.h"
#include "nio_util.h"


#ifdef LINUX
#include <linux/unistd.h>
#include <linux/sysctl.h>
#include <sys/utsname.h>
#include <netinet/ip.h>

#define IPV6_MULTICAST_IF 17
#ifndef SO_BSDCOMPAT
#define SO_BSDCOMPAT  14
#endif
#endif

#ifndef IPTOS_TOS_MASK
#define IPTOS_TOS_MASK 0x1e
#endif
#ifndef IPTOS_PREC_MASK
#define IPTOS_PREC_MASK 0xe0
#endif


#include "java_net_SocketOptions.h"
#include "java_net_NetworkInterface.h"



jint convReturnVal(JNIEnv *env, jint n, jboolean reading);
void NET_ThrowByNameWithLastError(JNIEnv *env, const char *name, const char *defaultDetail);
void NET_ThrowCurrent(JNIEnv *env, char *msg);
void NET_ThrowNew(JNIEnv *env, int errorNumber, char *msg);


static jclass   ina_class;
static jfieldID ina_addressID;
static jfieldID ina_familyID;
static jfieldID ina_preferIPv6AddressID;

static jfieldID nei_addrsID;
static jfieldID nei_indexID;

static jfieldID ina6_ipaddressID;


static jclass isa_class = 0;        /* java.net.InetSocketAddress */
static jmethodID isa_ctorID;    /*   .InetSocketAddress(InetAddress, int) */

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_init(JNIEnv *env, jclass cl) {
    
    jclass c = (*env)->FindClass(env,"java/net/InetAddress");
    CHECK_NULL(c);
    ina_class = (*env)->NewGlobalRef(env, c);
    CHECK_NULL(ina_class);
    ina_addressID = (*env)->GetFieldID(env, ina_class, "address", "I");
    CHECK_NULL(ina_addressID);
    ina_familyID = (*env)->GetFieldID(env, ina_class, "family", "I");
    CHECK_NULL(ina_familyID);
    ina_preferIPv6AddressID = (*env)->GetStaticFieldID(env, ina_class, "preferIPv6Address", "Z");
    CHECK_NULL(ina_preferIPv6AddressID);
    
    c = (*env)->FindClass(env, "java/net/NetworkInterface");
    CHECK_NULL(c);
    nei_addrsID = (*env)->GetFieldID(env, c, "addrs", "[Ljava/net/InetAddress;");
    CHECK_NULL(nei_addrsID);
    nei_indexID = (*env)->GetFieldID(env, c, "index", "I");
    CHECK_NULL(nei_indexID);
    
    c = (*env)->FindClass(env, "java/net/Inet6Address");
    CHECK_NULL(c);
    jclass ia6_class = (*env)->NewGlobalRef(env, c);
    CHECK_NULL(ia6_class);
    ina6_ipaddressID = (*env)->GetFieldID(env, ia6_class, "ipaddress", "[B");
    CHECK_NULL(ina6_ipaddressID);
}

/* ./jdk/src/solaris/native/sun/nio/ch/Net.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_socket(JNIEnv *env, jclass cl, jboolean stream, jboolean reuse) {
    int fd;

#ifdef AF_INET6
    if (ipv6_available())
        fd = socket(AF_INET6, (stream ? SOCK_STREAM : SOCK_DGRAM), 0);
    else
#endif /* AF_INET6 */
        fd = socket(AF_INET, (stream ? SOCK_STREAM : SOCK_DGRAM), 0);

    if (fd < 0) {
        return handleSocketError(env, errno);
    }
    if (reuse) {
        int arg = 1;
        if (NET_SetSockOpt(fd, SOL_SOCKET, SO_REUSEADDR, (char*)&arg,
                           sizeof(arg)) < 0) {
            JNU_ThrowByNameWithLastError(env,
                                         JNU_JAVANETPKG "SocketException",
                                         "com.rr.core.socket.LinuxSocketImpl.socket");
        }
    }
    return fd;
}


/* ./jdk/src/solaris/native/sun/nio/ch/Net.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_connect(JNIEnv *env, jclass clazz, jint fd, jobject iao, jint port, jint trafficClass ) {
    SOCKADDR sa;
    int sa_len = SOCKADDR_LEN;
    int rv;

    if (NET_InetAddressToSockaddr(env, iao, port, (struct sockaddr *) &sa, &sa_len, JNI_TRUE) != 0) {
      return IOS_THROWN;
    }

#ifdef AF_INET6
#if 0
    if (trafficClass != 0 && ipv6_available()) { /* ## FIX */
        NET_SetTrafficClass((struct sockaddr *)&sa, trafficClass);
    }
#endif
#endif

    rv = connect(fd, (struct sockaddr *)&sa, sa_len);
    if (rv != 0) {
        if (errno == EINPROGRESS) {
            return IOS_UNAVAILABLE;
        } else if (errno == EINTR) {
            return IOS_INTERRUPTED;
        }
        return handleSocketError(env, errno);
    }
    return 1;
}

/* ./jdk/src/solaris/native/sun/nio/ch/SocketChannelImpl.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_checkConnect( JNIEnv *env, jclass c, jint fd, jboolean block, jboolean ready ) {
    int error = 0;
    int n = sizeof(int);
    int result = 0;
    struct pollfd poller;

    poller.revents = 1;
    if (!ready) {
        poller.fd = fd;
        poller.events = POLLOUT;
        poller.revents = 0;
        result = poll(&poller, 1, block ? -1 : 0);
        if (result < 0) {
            JNU_ThrowIOExceptionWithLastError(env, "Poll failed");
            return IOS_THROWN;
        }
        if (!block && (result == 0))
            return IOS_UNAVAILABLE;
    }

    if (poller.revents) {
        errno = 0;
        result = getsockopt(fd, SOL_SOCKET, SO_ERROR, &error, &n);
        if (result < 0) {
            handleSocketError(env, errno);
            return JNI_FALSE;
        } else if (error) {
            handleSocketError(env, error);
            return JNI_FALSE;
        }
        return 1;
    }
    return 0;
}


/* ./jdk/src/solaris/native/sun/nio/ch/IOUtil.c */

static int
configureBlocking(int fd, jboolean blocking) {
    int flags;
    
    if (-1 == (flags = fcntl(fd, F_GETFL, 0)))
        flags = 0;

    printf( "FD=%d, beforeFlags=%d, blocking=%d\n", fd, flags, blocking );

    if (blocking == JNI_TRUE)
        return fcntl(fd, F_SETFL, flags & ~O_NONBLOCK);

    return fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_configureBlocking(JNIEnv *env, jclass clazz, jint fd, jboolean blocking) {
    int res;
    
    if ( (res = configureBlocking(fd, blocking)) < 0)
        JNU_ThrowIOExceptionWithLastError(env, "Configure blocking failed");
        
    return res;
}

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_getFlags(JNIEnv *env, jclass clazz, jint fd) {
    int flags = fcntl(fd, F_GETFL, 0);

    return flags;
}

/* ./jdk/src/solaris/native/sun/nio/ch/FileDispatcher.c */

static void closeFileDescriptor(JNIEnv *env, int fd) {
    if (fd != -1) {
        int result = close(fd);
        if (result < 0)
            JNU_ThrowIOExceptionWithLastError(env, "Close failed");
    }
}

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_close(JNIEnv *env, jclass clazz, jint fd){
    closeFileDescriptor(env, fd);
}


/* ./jdk/src/solaris/native/sun/nio/ch/Net.c */

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_bind(JNIEnv *env, jclass clazz, jint fd, jobject ia, jint port ) {
    SOCKADDR sa;
    int sa_len = SOCKADDR_LEN;
    int rv = 0;

    if (NET_InetAddressToSockaddr(env, ia, port, (struct sockaddr *)&sa, &sa_len, JNI_TRUE) != 0) {
      return;
    }

    rv = NET_Bind(fd, (struct sockaddr *)&sa, sa_len);
    if (rv != 0) {
        handleSocketError(env, errno);
    }
}


/* ./jdk/src/windows/native/sun/nio/ch/ServerSocketChannelImpl.c */

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_listen(JNIEnv *env, jclass cl, jint fd, jint backlog ) {
    if (listen(fd, backlog) < 0)
        handleSocketError(env, errno);
}


/* ./jdk/src/windows/native/sun/nio/ch/ServerSocketChannelImpl.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_accept(JNIEnv *env, jclass cl, jint ssfd, jobjectArray isaa ) {
    jint newfd;
    struct sockaddr *sa;
    int sa_len;
    jobject remote_ia = 0;
    jobject isa;
    jint remote_port;

    NET_AllocSockaddr(&sa, &sa_len);

    /*
     * accept connection but ignore ECONNABORTED indicating that
     * a connection was eagerly accepted but was reset before
     * accept() was called.
     */
    for (;;) {
        newfd = accept(ssfd, sa, &sa_len);
        if (newfd >= 0) {
            break;
        }
        if (errno != ECONNABORTED) {
            break;
        }
        /* ECONNABORTED => restart accept */
    }

    if (newfd < 0) {
        free((void *)sa);
        if (errno == EAGAIN)
            return IOS_UNAVAILABLE;
        if (errno == EINTR)
            return IOS_INTERRUPTED;
        JNU_ThrowIOExceptionWithLastError(env, "Accept failed");
        return IOS_THROWN;
    }

    if ( isa_class == 0 ) {
        jclass cls;
        cls = (*env)->FindClass(env, "java/net/InetSocketAddress");
        isa_class = (*env)->NewGlobalRef(env, cls);
        isa_ctorID = (*env)->GetMethodID(env, cls, "<init>", "(Ljava/net/InetAddress;I)V");
    }

    remote_ia = NET_SockaddrToInetAddress(env, sa, (int *)&remote_port);
    free((void *)sa);
    isa = (*env)->NewObject(env, isa_class, isa_ctorID,
                            remote_ia, remote_port);
    (*env)->SetObjectArrayElement(env, isaa, 0, isa);
    return newfd;
}


/* ./jdk/src/solaris/native/sun/nio/ch/DatagramChannelImpl.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_receiveIgnoreSrc(JNIEnv *env, jclass clazz, jint fd, jlong address, jint len ) {
    void *buf = (void *)jlong_to_ptr(address);

    jboolean retry = JNI_FALSE;
    jint n = 0;

    if (len > MAX_PACKET_LEN) {
        len = MAX_PACKET_LEN;
    }

    do {
        retry = JNI_FALSE;
        n = recv(fd, buf, len, 0 );
        if (n < 0) {
            if (errno == EWOULDBLOCK) {
                return IOS_UNAVAILABLE;
            }
            if (errno == EINTR) {
                return IOS_INTERRUPTED;
            }
            if (errno == ECONNREFUSED) {
                JNU_ThrowByName(env, JNU_JAVANETPKG
                                "PortUnreachableException", 0);
                return IOS_THROWN;
            } else {
                return handleSocketError(env, errno);
            }
        }
    } while (retry == JNI_TRUE);
    
    // ignore source address of packet 
    
    return n;
}


/* ./jdk/src/solaris/native/sun/nio/ch/FileDispatcher.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_read(JNIEnv *env, jclass clazz, jint fd, jlong address, jint len ) {
    void *buf = (void *)jlong_to_ptr(address);

    return convReturnVal(env, read(fd, buf, len), JNI_TRUE);
}


static void mcast_join_leave(JNIEnv *env, jclass clazz, jint fd, jobject iaObj, jobject niObj, jboolean join) {
    jint ipv6_join_leave;

    if (IS_NULL(iaObj)) {
        JNU_ThrowNullPointerException(env, "iaObj");
        return;
    }

#ifdef AF_INET6
    ipv6_join_leave = ipv6_available();

#ifdef LINUX
    if ((*env)->GetIntField(env, iaObj, ina_familyID) == IPv4) {
        ipv6_join_leave = JNI_FALSE;
    }
#endif

#else
    ipv6_join_leave = JNI_FALSE;
#endif

    if (!ipv6_join_leave) {
#ifdef LINUX
        struct ip_mreqn mname;
#else
        struct ip_mreq mname;
#endif
        int mname_len;

        if (niObj != NULL) {
#if defined(LINUX) && defined(AF_INET6)
            if (ipv6_available()) {
                mname.imr_multiaddr.s_addr = htonl((*env)->GetIntField(env, iaObj, ina_addressID));
                mname.imr_address.s_addr = 0;
                mname.imr_ifindex =  (*env)->GetIntField(env, niObj, nei_indexID);
                mname_len = sizeof(struct ip_mreqn);
            } else
#endif
            {
                jobjectArray addrArray = (*env)->GetObjectField(env, niObj, nei_addrsID);
                jobject addr;

                if ((*env)->GetArrayLength(env, addrArray) < 1) {
                    JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "bad argument for IP_ADD_MEMBERSHIP: "
                        "No IP addresses bound to interface");
                    return;
                }
                addr = (*env)->GetObjectArrayElement(env, addrArray, 0);

                mname.imr_multiaddr.s_addr = htonl((*env)->GetIntField(env, iaObj, ina_addressID));
#ifdef LINUX
                mname.imr_address.s_addr = htonl((*env)->GetIntField(env, addr, ina_addressID));
#else
                mname.imr_interface.s_addr = htonl((*env)->GetIntField(env, addr, ina_addressID));
#endif
                mname_len = sizeof(struct ip_mreq);
            }
        }

        if (niObj == NULL) {

#if defined(LINUX) && defined(AF_INET6)
            if (ipv6_available()) {

                int index;
                int len = sizeof(index);

                if (JVM_GetSockOpt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                                   (char*)&index, &len) < 0) {
                    NET_ThrowCurrent(env, "getsockopt IPV6_MULTICAST_IF failed");
                    return;
                }

                mname.imr_multiaddr.s_addr = htonl((*env)->GetIntField(env, iaObj, ina_addressID));
                mname.imr_address.s_addr = 0 ;
                mname.imr_ifindex = index;
                mname_len = sizeof(struct ip_mreqn);
            } else
#endif
            {
                struct in_addr in;
                struct in_addr *inP = &in;
                int len = sizeof(struct in_addr);

#ifdef LINUX
                struct ip_mreqn mreqn;
#endif
                if (getsockopt(fd, IPPROTO_IP, IP_MULTICAST_IF, (char *)inP, &len) < 0) {
                    NET_ThrowCurrent(env, "getsockopt IP_MULTICAST_IF failed");
                    return;
                }

#ifdef LINUX
                mname.imr_address.s_addr = in.s_addr;

#else
                mname.imr_interface.s_addr = in.s_addr;
#endif
                mname.imr_multiaddr.s_addr = htonl((*env)->GetIntField(env, iaObj, ina_addressID));
                mname_len = sizeof(struct ip_mreq);
            }
        }


        if (JVM_SetSockOpt(fd, IPPROTO_IP, (join ? IP_ADD_MEMBERSHIP:IP_DROP_MEMBERSHIP),
                           (char *) &mname, mname_len) < 0) {

#if defined(LINUX) && defined(AF_INET6)
            if (errno == ENOPROTOOPT) {
                if (ipv6_available()) {
                    ipv6_join_leave = JNI_TRUE;
                    errno = 0;
                } else  {
                    errno = ENOPROTOOPT;    /* errno can be changed by ipv6_available */
                }
            }
#endif
            if (errno) {
                if (join) {
                    NET_ThrowCurrent(env, "setsockopt IP_ADD_MEMBERSHIP failed");
                } else {
                    if (errno == ENOENT)
                        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                            "Not a member of the multicast group");
                    else
                        NET_ThrowCurrent(env, "setsockopt IP_DROP_MEMBERSHIP failed");
                }
            }
        }

        if (!ipv6_join_leave) {
            return;
        }
    }


#ifdef AF_INET6
    {
        struct ipv6_mreq mname6;
        jbyteArray ipaddress;
        jbyte caddr[16];
        jint family;
        jint address;
        family = (*env)->GetIntField(env, iaObj, ina_familyID) == IPv4? AF_INET : AF_INET6;
        if (family == AF_INET) { /* will convert to IPv4-mapped address */
            memset((char *) caddr, 0, 16);
            address = (*env)->GetIntField(env, iaObj, ina_addressID);

            caddr[10] = 0xff;
            caddr[11] = 0xff;

            caddr[12] = ((address >> 24) & 0xff);
            caddr[13] = ((address >> 16) & 0xff);
            caddr[14] = ((address >> 8) & 0xff);
            caddr[15] = (address & 0xff);
        } else {
            ipaddress = (*env)->GetObjectField(env, iaObj, ina6_ipaddressID);
            (*env)->GetByteArrayRegion(env, ipaddress, 0, 16, caddr);
        }

        memcpy((void *)&(mname6.ipv6mr_multiaddr), caddr, sizeof(struct in6_addr));
        if (IS_NULL(niObj)) {
            int index;
            int len = sizeof(index);

            if (JVM_GetSockOpt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                             (char*)&index, &len) < 0) {
                NET_ThrowCurrent(env, "getsockopt IPV6_MULTICAST_IF failed");
                return;
            }

#ifdef LINUX
            if (index == 0) {
                int rt_index = getDefaultIPv6Interface(&(mname6.ipv6mr_multiaddr));
                if (rt_index > 0) {
                    index = rt_index;
                }
            }
#endif

            mname6.ipv6mr_interface = index;
        } else {
            jint idx = (*env)->GetIntField(env, niObj, nei_indexID);
            mname6.ipv6mr_interface = idx;
        }

        /* Join the multicast group */
        if (JVM_SetSockOpt(fd, IPPROTO_IPV6, (join ? IPV6_ADD_MEMBERSHIP : IPV6_DROP_MEMBERSHIP),
                           (char *) &mname6, sizeof (mname6)) < 0) {

            if (join) {
                NET_ThrowCurrent(env, "setsockopt IPV6_ADD_MEMBERSHIP failed");
            } else {
                if (errno == ENOENT) {
                   JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Not a member of the multicast group");
                } else {
                    NET_ThrowCurrent(env, "setsockopt IPV6_DROP_MEMBERSHIP failed");
                }
            }
        }
    }
#endif

}

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_join(JNIEnv *env, jclass clazz, jint fd, jobject iaObj, jobject niObj) {
    mcast_join_leave(env, clazz, fd, iaObj, niObj, JNI_TRUE);
}

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_leave(JNIEnv *env, jclass clazz, jint fd, jobject iaObj, jobject niObj) {
    mcast_join_leave(env, clazz, fd, iaObj, niObj, JNI_FALSE);
}


/* ./jdk/src/solaris/native/sun/nio/ch/FileDispatcher.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_write(JNIEnv *env, jclass clazz, jint fd, jlong address, jint len ){
    void *buf = (void *)jlong_to_ptr(address);
    return convReturnVal(env, write(fd, buf, len), JNI_FALSE);
}

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_onloadWrite(JNIEnv *env, jclass clazz, jint fd, jlong address, jint len, jboolean isDummyWriteToWarmCPU ){
    void *buf = (void *)jlong_to_ptr(address);
    int flags = O_NONBLOCK;

#ifdef ONLOAD_MSG_WARM
    /*  onload/extensions.h  */
    if ( isDummyWriteToWarmCPU ) {
        flags = flags | ONLOAD_MSG_WARM;
    }
#else
    if ( isDummyWriteToWarmCPU ) return len;
#endif
    return convReturnVal(env, send(fd, buf, len, flags), JNI_FALSE);
}


/* ./jdk/src/solaris/native/sun/nio/ch/Net.c */

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_getIntOption(JNIEnv *env, jclass clazz, jint fd, jint opt){
    int klevel, kopt;
    int result;
    struct linger linger;
    void *arg;
    int arglen;

    if (NET_MapSocketOption(opt, &klevel, &kopt) < 0) {
        JNU_ThrowByNameWithLastError(env,
                                     JNU_JAVANETPKG "SocketException",
                                     "Unsupported socket option");
        return -1;
    }

    if (opt == java_net_SocketOptions_SO_LINGER) {
        arg = (void *)&linger;
        arglen = sizeof(linger);
    } else {
        arg = (void *)&result;
        arglen = sizeof(result);
    }

    if (NET_GetSockOpt(fd, klevel, kopt, arg, &arglen) < 0) {
        JNU_ThrowByNameWithLastError(env,
                                     JNU_JAVANETPKG "SocketException",
                                     "com.rr.core.socket.LinuxSocketImpl.getIntOption");
        return -1;
    }

    if (opt == java_net_SocketOptions_SO_LINGER)
        return linger.l_onoff ? linger.l_linger : -1;
    else
        return result;
}


/* ./jdk/src/solaris/native/sun/nio/ch/Net.c */

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_setIntOption(JNIEnv *env, jclass clazz, jint fd, jint opt, jint arg ){
    int klevel, kopt;
    int result;
    struct linger linger;
    void *parg;
    int arglen;

    if (NET_MapSocketOption(opt, &klevel, &kopt) < 0) {
        JNU_ThrowByNameWithLastError(env,
                                     JNU_JAVANETPKG "SocketException",
                                     "Unsupported socket option");
        return;
    }

    if (opt == java_net_SocketOptions_SO_LINGER) {
        parg = (void *)&linger;
        arglen = sizeof(linger);
        if (arg >= 0) {
            linger.l_onoff = 1;
            linger.l_linger = arg;
        } else {
            linger.l_onoff = 0;
            linger.l_linger = 0;
        }
    } else {
        parg = (void *)&arg;
        arglen = sizeof(arg);
    }

    if (NET_SetSockOpt(fd, klevel, kopt, parg, arglen) < 0) {
        JNU_ThrowByNameWithLastError(env,
                                     JNU_JAVANETPKG "SocketException",
                                     "com.rr.core.socket.LinuxSocketImpl.setIntOption");
    }
}

const char* convString( JNIEnv *env, jstring jstr ) {
    jboolean iscopy;
    const char *ptr = (*env)->GetStringUTFChars(env, jstr, &iscopy);
    return ptr;
}

void releaseString( JNIEnv *env, jstring jstr, const char *ptr ){
    (*env)->ReleaseStringUTFChars(env, jstr, ptr);
}

/* dont worry about freeing the sockaddr_in ... it wont be called alot */

JNIEXPORT jlong JNICALL Java_com_rr_core_socket_LinuxSocketImpl_mcastIP4_1server_1makeSockAddrIn(JNIEnv *env, jclass clazz, jstring mcastAddrIP, jint port) {
    const char *mcastAddr = convString( env, mcastAddrIP );
    struct sockaddr_in *groupSock = (struct sockaddr_in *) malloc( sizeof(struct sockaddr_in) ); 
    memset((char *) groupSock, 0, sizeof(*groupSock));
    groupSock->sin_family = AF_INET;
    groupSock->sin_addr.s_addr = inet_addr(mcastAddr);
    groupSock->sin_port = htons(port);
    releaseString( env, mcastAddrIP, mcastAddr );
    return (long)groupSock;
}


JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_mcastIP4_1server_1setSendMulticastLocalIF(JNIEnv *env, jclass clazz, jint sd, jstring jlocalInterfaceIP) {
    const char *localInterfaceIP  = convString( env, jlocalInterfaceIP );
    struct in_addr localInterface;
    localInterface.s_addr = inet_addr( localInterfaceIP );

    if(setsockopt(sd, IPPROTO_IP, IP_MULTICAST_IF, (char *)&localInterface, sizeof(localInterface)) < 0) {
        NET_ThrowCurrent(env, "setSendMulticastLocalIF IP_MULTICAST_IF failed" );
        return;
    } 

    releaseString( env, jlocalInterfaceIP, localInterfaceIP );
}

JNIEXPORT jint JNICALL Java_com_rr_core_socket_LinuxSocketImpl_mcastIP4_1server_1sendTo(JNIEnv *env, jclass clazz, jint sd, jlong addr, jint len, jlong grpSockPtr) {
    void *buf = (void *)jlong_to_ptr(addr);
    struct sockaddr_in* groupSock = (struct sockaddr_in*) jlong_to_ptr(grpSockPtr);
    return convReturnVal(env, sendto(sd, buf, len, 0, (struct sockaddr*)groupSock, sizeof(*groupSock)), JNI_FALSE);
}

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_mcastIP4_1client_1bindAnyIF_1IP4(JNIEnv *env, jclass clazz, jint sd, jint port) {
    struct sockaddr_in localSock;
    memset((char *) &localSock, 0, sizeof(localSock));
    localSock.sin_family = AF_INET;
    localSock.sin_port = htons(port);
    localSock.sin_addr.s_addr = INADDR_ANY;

    int rv = NET_Bind(sd, (struct sockaddr *)&localSock, sizeof(localSock));
    if (rv != 0) {
        handleSocketError(env, errno);
    }
}

JNIEXPORT void JNICALL Java_com_rr_core_socket_LinuxSocketImpl_mcastIP4_1client_1join(JNIEnv *env, jclass clazz, jint sd, jstring jmcastAddrIP, jstring jlocalInterfaceIP) {
    const char *mcastGrpAddrIP    = convString( env, jmcastAddrIP );
    const char *localInterfaceIP  = convString( env, jlocalInterfaceIP );

    struct ip_mreq group;
    group.imr_multiaddr.s_addr = inet_addr( mcastGrpAddrIP );
    group.imr_interface.s_addr = inet_addr( localInterfaceIP );

    if(setsockopt(sd, IPPROTO_IP, IP_ADD_MEMBERSHIP, (char *)&group, sizeof(group)) < 0) {
        NET_ThrowCurrent(env, "mcastIP4_1client_1join IP_ADD_MEMBERSHIP failed" );
        return;
    } 

    releaseString( env, jmcastAddrIP, mcastGrpAddrIP );
    releaseString( env, jlocalInterfaceIP, localInterfaceIP );
}



/* Declared in nio_util.h */
 
jint
handleSocketError(JNIEnv *env, jint errorValue)
{
    char *xn;
    switch (errorValue) {
        case EINPROGRESS:       /* Non-blocking connect */
            return 0;
        case EPROTO:
            xn = JNU_JAVANETPKG "ProtocolException";
            break;
        case ECONNREFUSED:
            xn = JNU_JAVANETPKG "ConnectException";
            break;
        case ETIMEDOUT:
            xn = JNU_JAVANETPKG "ConnectException";
            break;
        case EHOSTUNREACH:
            xn = JNU_JAVANETPKG "NoRouteToHostException";
            break;
        case EADDRINUSE:  /* Fall through */
        case EADDRNOTAVAIL:
            xn = JNU_JAVANETPKG "BindException";
            break;
        default:
            xn = JNU_JAVANETPKG "SocketException";
            break;
    }
    errno = errorValue;
    JNU_ThrowByNameWithLastError(env, xn, "NioSocketError");
    return IOS_THROWN;
}

jint
convReturnVal(JNIEnv *env, jint n, jboolean reading)
{
    if (n > 0) /* Number of bytes written */
        return n;
    if (n < 0) {
        if (errno == EAGAIN)
            return IOS_UNAVAILABLE;
        if (errno == EINTR)
            return IOS_INTERRUPTED;
    }
    if (n == 0) {
        if (reading) {
            return IOS_EOF; /* EOF is -1 in javaland */
        } else {
            return 0;
        }
    }
    JNU_ThrowIOExceptionWithLastError(env, "Read/write/send failed");
    return IOS_THROWN;
}
 
void
NET_ThrowByNameWithLastError(JNIEnv *env, const char *name,
                   const char *defaultDetail) {
    char errmsg[255];
    sprintf(errmsg, "errno: %d, error: %s\n", errno, defaultDetail);
    JNU_ThrowByNameWithLastError(env, name, errmsg);
}

void
NET_ThrowCurrent(JNIEnv *env, char *msg) {
    NET_ThrowNew(env, errno, msg);
}

void
NET_ThrowNew(JNIEnv *env, int errorNumber, char *msg) {
    char fullMsg[512];
    if (!msg) {
        msg = "no further information";
    }
    switch(errorNumber) {
    case EBADF:
        jio_snprintf(fullMsg, sizeof(fullMsg), "socket closed: %s", msg);
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", fullMsg);
        break;
    case EINTR:
        JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException", msg);
        break;
    default:
        errno = errorNumber;
        JNU_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException", msg);
        break;
    }
}

 
