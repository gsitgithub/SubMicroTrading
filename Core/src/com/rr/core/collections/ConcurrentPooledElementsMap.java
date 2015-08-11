package com.rr.core.collections;

/*
 * @(#)ConcurrentPooledElementsMap.java   1.21 07/01/02
 *
 * Based on Sun ConcurrentHashMap but uses factories to avoid GC
 */

/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.TLC;
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;

@SuppressWarnings( { "hiding", "unchecked", "unused" } )
public class ConcurrentPooledElementsMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {

    private static final long      serialVersionUID          = 7249069246763182397L;

    /*
     * The basic strategy is to subdivide the table among Segments, each of which itself is a concurrently readable hash table.
     */

    /* ---------------- Constants -------------- */

    /**
     * The default initial capacity for this table, used when not otherwise specified in a constructor.
     */
    static final int               DEFAULT_INITIAL_CAPACITY  = 16;

    /**
     * The default load factor for this table, used when not otherwise specified in a constructor.
     */
    static final float             DEFAULT_LOAD_FACTOR       = 0.85f;

    /**
     * The default concurrency level for this table, used when not otherwise specified in a constructor.
     */
    static final int               DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with arguments. MUST be a power of two <= 1<<30 to
     * ensure that entries are indexable using ints.
     */
    static final int               MAXIMUM_CAPACITY          = 1 << 30;

    /**
     * The maximum number of segments to allow; used to bound constructor arguments.
     */
    static final int               MAX_SEGMENTS              = 1 << 16;             // slightly conservative

    /**
     * Number of unsynchronized retries in size and containsValue methods before resorting to locking. This is used to avoid unbounded retries if tables undergo
     * continuous modification which would make it impossible to obtain an accurate result.
     */
    static final int               RETRIES_BEFORE_LOCK       = 2;

    /* ---------------- Fields -------------- */

    /**
     * Mask value for indexing into segments. The upper bits of a key's hash code are used to choose the segment.
     */
    final int                      segmentMask;

    /**
     * Shift value for indexing within segments.
     */
    final int                      segmentShift;

    /**
     * The segments, each of which is a specialized hash table
     */
    final Segment<K, V>[]          segments;

    transient Set<K>               keySet;
    transient Set<Map.Entry<K, V>> entrySet;
    transient Collection<V>        values;

    /* ---------------- Small Utilities -------------- */

    /**
     * Applies a supplemental hash function to a given hashCode, which defends against poor quality hash functions. This is critical because ConcurrentPooledElementsMap uses
     * power-of-two length hash tables, that otherwise encounter collisions for hashCodes that do not differ in lower or upper bits.
     */
    private static int hash( int h ) {
        // Spread bits to regularize both Segment<K,V>and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        return h ^ (h >>> 16);
    }

    /**
     * Returns the Segment<K,V>that should be used for key with given hash
     * 
     * @param hash
     *            the hash code for the key
     * @return the segment
     */
    final Segment<K, V> segmentFor( int hash ) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }

    /* ---------------- Inner Classes -------------- */

    public static final class ConcHashEntryFactory<K, V> implements PoolFactory<ConcHashEntry<K, V>> {

        private SuperPool<ConcHashEntry<K, V>> _superPool;
        private ConcHashEntry<K, V>            _root;

        public ConcHashEntryFactory( SuperPool<ConcHashEntry<K, V>> superPool ) {
            _superPool = superPool;
            _root = _superPool.getChain();
        }

        @Override
        public ConcHashEntry<K, V> get() {
            if ( _root == null ) {
                _root = _superPool.getChain();
            }
            ConcHashEntry<K, V> obj = _root;
            _root = _root.getNext();
            obj.setNext( null );
            return obj;
        }

    }

    /**
     * ConcurrentPooledElementsMap list entry. Note that this is never exported out as a user-visible Map.Entry. ... public to allow external factory instantiation
     * 
     * Because the value field is volatile, not final, it is legal wrt the Java Memory Model for an unsynchronized reader to see null instead of initial value
     * when read via a data race. Although a reTing leading to this is not likely to ever actually occur, the Segment.readValueUnderLock method is used as a
     * backup in case a null (pre-initialized) value is ever seen in an unsynchronized access method.
     */
    public static final class ConcHashEntry<K, V> implements Reusable<ConcHashEntry<K, V>> {

        volatile V          value;
        K                   key;
        ConcHashEntry<K, V> next;
        int                 hash;

        void set( K key, int hash, ConcHashEntry<K, V> next, V value ) {
            this.key = key;
            this.hash = hash;
            this.next = next;
            this.value = value;
        }

        static final <K, V> ConcHashEntry<K, V>[] newArray( int i ) {
            return new ConcHashEntry[i];
        }

        @Override
        public void reset() {
            key = null;
            hash = 0;
            value = null;
            next = null;
        }

        @Override
        public ConcHashEntry<K, V> getNext() {
            return next;
        }

        @Override
        public void setNext( ConcHashEntry<K, V> nxt ) {
            next = nxt;
        }

        @Override
        public ReusableType getReusableType() {
            return CollectionTypes.ConcurrentMapEntry;
        }
    }

    /**
     * Segments are specialized versions of hash tables. This subclasses from ReentrantLock opportunistically, just to simplify some locking and avoid separate
     * construction.
     */
    static final class Segment<K, V> extends ReentrantLock implements Serializable {

        /*
         * Segments maintain a table of entry lists that are ALWAYS kept in a consistent state, so can be read without locking. Next fields of nodes are
         * immutable (final). All list additions are performed at the front of each bin. This makes it easy to check changes, and also fast to traverse. When
         * nodes would otherwise be changed, new nodes are created to replace them. This works well for hash tables since the bin lists tend to be short. (The
         * average length is less than two for the default load factor threshold.)
         * 
         * Read operations can thus proceed without locking, but rely on selected uses of volatiles to ensure that completed write operations performed by other
         * threads are noticed. For most purposes, the "count" field, tracking the number of elements, serves as that volatile variable ensuring visibility.
         * This is convenient because this field needs to be read in many read operations anyway:
         * 
         * - All (unsynchronized) read operations must first read the "count" field, and should not look at table entries if it is 0.
         * 
         * - All (synchronized) write operations should write to the "count" field after structurally changing any bin. The operations must not take any action
         * that could even momentarily cause a concurrent read operation to see inconsistent data. This is made easier by the nature of the read operations in
         * Map. For example, no operation can reveal that the table has grown but the threshold has not yet been updated, so there are no atomicity requirements
         * for this with respect to reads.
         * 
         * As a guide, all critical volatile reads and writes to the count field are marked in code comments.
         */

        private static final long                serialVersionUID = 2249069246763182397L;

        /**
         * The number of elements in this segment's region.
         */
        transient volatile int                   count;

        /**
         * Number of updates that alter the size of the table. This is used during bulk-read methods to make sure they see a consistent snapshot: If modCounts
         * change during a traversal of segments computing size or checking containsValue, then we might have an inconsistent view of state so (usually) must
         * retry.
         */
        transient int                            modCount;

        /**
         * The table is rehashed when its size exceeds this threshold. (The value of this field is always <tt>(int)(capacity *
         * loadFactor)</tt>.)
         */
        transient int                            threshold;

        /**
         * The per-Segment<K,V>table.
         */
        transient volatile ConcHashEntry<K, V>[] table;

        /**
         * The load factor for the hash table. Even though this value is same for all segments, it is replicated to avoid needing links to outer object.
         * 
         * @serial
         */
        final float                              loadFactor;

        private ConcHashEntryFactory<K, V>       _entryFactory    = SuperpoolManager.instance().getFactory( ConcHashEntryFactory.class, ConcHashEntry.class );
        private Recycler<ConcHashEntry<K, V>>    _entryRecycler   = SuperpoolManager.instance().getRecycler( ConcHashEntry.class );

        Segment( int initialCapacity, float lf ) {
            loadFactor = lf;
            ConcHashEntry<K, V>[] newTable = ConcHashEntry.newArray( initialCapacity );
            setTable( newTable );
        }

        static final <K, V> Segment<K, V>[] newArray( int i ) {
            return new Segment[i];
        }

        /**
         * Sets table to new HashEntry array. Call only while holding lock or in constructor.
         */
        void setTable( ConcHashEntry<K, V>[] newTable ) {
            threshold = (int) (newTable.length * loadFactor);
            table = newTable;
        }

        /**
         * Returns properly casted first entry of bin for given hash.
         */
        ConcHashEntry<K, V> getFirst( int hash ) {
            ConcHashEntry<K, V>[] tab = table;
            return tab[hash & (tab.length - 1)];
        }

        /**
         * Reads value field of an entry under lock. Called if value field ever appears to be null. This is possible only if a compiler happens to reT a
         * HashEntry initialization with its table assignment, which is legal under memory model but is not known to ever occur.
         */
        V readValueUnderLock( ConcHashEntry<K, V> e ) {
            lock();
            try {
                return e.value;
            } finally {
                unlock();
            }
        }

        /* Specialized implementations of map methods */

        V get( Object key, int hash ) {
            if ( count != 0 ) { // read-volatile
                ConcHashEntry<K, V> e = getFirst( hash );
                while( e != null ) {
                    if ( e.hash == hash && key.equals( e.key ) ) {
                        V v = e.value;
                        if ( v != null )
                            return v;
                        return readValueUnderLock( e ); // recheck
                    }
                    e = e.next;
                }
            }
            return null;
        }

        boolean containsKey( Object key, int hash ) {
            if ( count != 0 ) { // read-volatile
                ConcHashEntry<K, V> e = getFirst( hash );
                while( e != null ) {
                    if ( e.hash == hash && key.equals( e.key ) )
                        return true;
                    e = e.next;
                }
            }
            return false;
        }

        boolean containsValue( Object value ) {
            if ( count != 0 ) { // read-volatile
                ConcHashEntry<K, V>[] tab = table;
                int len = tab.length;
                for ( int i = 0 ; i < len ; i++ ) {
                    for ( ConcHashEntry<K, V> e = tab[i] ; e != null ; e = e.next ) {
                        V v = e.value;
                        if ( v == null ) // recheck
                            v = readValueUnderLock( e );
                        if ( value.equals( v ) )
                            return true;
                    }
                }
            }
            return false;
        }

        boolean replace( K key, int hash, V oldValue, V newValue ) {
            lock();
            try {
                ConcHashEntry<K, V> e = getFirst( hash );
                while( e != null && (e.hash != hash || !key.equals( e.key )) )
                    e = e.next;

                boolean replaced = false;
                if ( e != null && oldValue.equals( e.value ) ) {
                    replaced = true;
                    e.value = newValue;
                }
                return replaced;
            } finally {
                unlock();
            }
        }

        V replace( K key, int hash, V newValue ) {
            lock();
            try {
                ConcHashEntry<K, V> e = getFirst( hash );
                while( e != null && (e.hash != hash || !key.equals( e.key )) )
                    e = e.next;

                V oldValue = null;
                if ( e != null ) {
                    oldValue = e.value;
                    e.value = newValue;
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        V put( K key, int hash, V value, boolean onlyIfAbsent ) {
            lock();
            try {
                int c = count;
                if ( c++ > threshold ) // ensure capacity
                    rehash();
                ConcHashEntry<K, V>[] tab = table;
                int index = hash & (tab.length - 1);
                ConcHashEntry<K, V> first = tab[index];
                ConcHashEntry<K, V> e = first;
                while( e != null && (e.hash != hash || !key.equals( e.key )) )
                    e = e.next;

                V oldValue;
                if ( e != null ) {
                    oldValue = e.value;
                    if ( !onlyIfAbsent )
                        e.value = value;
                } else {
                    oldValue = null;
                    ++modCount;
                    tab[index] = newEntry( key, hash, first, value );
                    count = c; // write-volatile
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        void rehash() {
            ConcHashEntry<K, V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            if ( oldCapacity >= MAXIMUM_CAPACITY )
                return;

            /*
             * Reclassify nodes in each list to new Map. Because we are using power-of-two expansion, the elements from each bin must either stay at same index,
             * or move with a power of two offset. We eliminate unnecessary node creation by catching cases where old nodes can be reused because their next
             * fields won't change. Statistically, at the default threshold, only about one-sixth of them need cloning when a table doubles. The nodes they
             * replace will be garbage collectable as soon as they are no longer referenced by any reader thread that may be in the midst of traversing table
             * right now.
             */

            ConcHashEntry<K, V>[] newTable = ConcHashEntry.newArray( oldCapacity << 1 );
            threshold = (int) (newTable.length * loadFactor);
            int sizeMask = newTable.length - 1;
            for ( int i = 0 ; i < oldCapacity ; i++ ) {
                // We need to guarantee that any existing reads of old Map can
                // proceed. So we cannot yet null out each bin.
                ConcHashEntry<K, V> e = oldTable[i];

                if ( e != null ) {
                    ConcHashEntry<K, V> next = e.next;
                    int idx = e.hash & sizeMask;

                    // Single node on list
                    if ( next == null )
                        newTable[idx] = e;

                    else {
                        // Reuse trailing consecutive sequence at same slot
                        ConcHashEntry<K, V> lastRun = e;
                        int lastIdx = idx;
                        for ( ConcHashEntry<K, V> last = next ; last != null ; last = last.next ) {
                            int k = last.hash & sizeMask;
                            if ( k != lastIdx ) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;

                        // Clone all remaining nodes
                        for ( ConcHashEntry<K, V> p = e ; p != lastRun ; p = p.next ) {
                            int k = p.hash & sizeMask;
                            ConcHashEntry<K, V> n = newTable[k];
                            newTable[k] = newEntry( p.key, p.hash, n, p.value );
                        }
                    }
                }
            }
            table = newTable;
        }

        /**
         * Remove; match on key only if value null, else match both.
         */
        V remove( Object key, int hash, Object value ) {
            lock();
            try {
                int c = count - 1;
                ConcHashEntry<K, V>[] tab = table;
                int index = hash & (tab.length - 1);
                ConcHashEntry<K, V> first = tab[index];
                ConcHashEntry<K, V> e = first;
                while( e != null && (e.hash != hash || !key.equals( e.key )) )
                    e = e.next;

                V oldValue = null;
                if ( e != null ) {
                    V v = e.value;
                    if ( value == null || value.equals( v ) ) {
                        oldValue = v;
                        // All entries following removed node can stay
                        // in list, but all preceding ones need to be
                        // cloned.
                        ++modCount;
                        ConcHashEntry<K, V> newFirst = e.next;
                        for ( ConcHashEntry<K, V> p = first ; p != e ; p = p.next )
                            newFirst = newEntry( p.key, p.hash, newFirst, p.value );
                        tab[index] = newFirst;
                        count = c; // write-volatile
                    }
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        void clear() {
            if ( count != 0 ) {
                lock();
                try {
                    ConcHashEntry<K, V>[] tab = table;
                    for ( int i = 0 ; i < tab.length ; i++ ) {
                        recycleEntry( tab[i] );
                        tab[i] = null;
                    }
                    ++modCount;
                    count = 0; // write-volatile
                } finally {
                    unlock();
                }
            }
        }

        ConcHashEntry<K, V> newEntry( K key, int hash, ConcHashEntry<K, V> next, V value ) {
            ConcHashEntry<K, V> entry;
            entry = _entryFactory.get();
            entry.set( key, hash, next, value );
            return entry;
        }

        void recycleEntry( ConcHashEntry<K, V> entry ) {
            if ( entry != null ) {

                ConcHashEntry<K, V> tmp;

                while( entry != null ) {

                    // DONT RECYCLE the KEY !!

                    tmp = entry;
                    entry = entry.getNext();

                    _entryRecycler.recycle( tmp );
                }
            }
        }
    }

    /* ---------------- Public operations -------------- */

    /**
     * Creates a new, empty map with the specified initial capacity, load factor and concurrency level.
     * 
     * @param initialCapacity
     *            the initial capacity. The implementation performs internal sizing to accommodate this many elements.
     * @param loadFactor
     *            the load factor threshold, used to control resizing. Resizing may be performed when the average number of elements per bin exceeds this
     *            threshold.
     * @param concurrencyLevel
     *            the estimated number of concurrently updating threads. The implementation performs internal sizing to try to accommodate this many threads.
     * @throws IllegalArgumentException
     *             if the initial capacity is negative or the load factor or concurrencyLevel are nonpositive.
     */
    public ConcurrentPooledElementsMap( int initialCapacity, float loadFactor, int concurrencyLevel ) {
        if ( !(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0 )
            throw new IllegalArgumentException();

        if ( concurrencyLevel > MAX_SEGMENTS )
            concurrencyLevel = MAX_SEGMENTS;

        // Find power-of-two sizes best matching arguments
        int sshift = 0;
        int ssize = 1;
        while( ssize < concurrencyLevel ) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;
        this.segments = Segment.newArray( ssize );

        if ( initialCapacity > MAXIMUM_CAPACITY )
            initialCapacity = MAXIMUM_CAPACITY;
        int c = initialCapacity / ssize;
        if ( c * ssize < initialCapacity )
            ++c;
        int cap = 1;
        while( cap < c )
            cap <<= 1;

        for ( int i = 0 ; i < this.segments.length ; ++i )
            this.segments[i] = new Segment<K,V>( cap, loadFactor );
    }

    /**
     * Creates a new, empty map with the specified initial capacity and load factor and with the default concurrencyLevel (16).
     * 
     * @param initialCapacity
     *            The implementation performs internal sizing to accommodate this many elements.
     * @param loadFactor
     *            the load factor threshold, used to control resizing. Resizing may be performed when the average number of elements per bin exceeds this
     *            threshold.
     * @throws IllegalArgumentException
     *             if the initial capacity of elements is negative or the load factor is nonpositive
     * 
     * @since 1.6
     */
    public ConcurrentPooledElementsMap( int initialCapacity, float loadFactor ) {
        this( initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL );
    }

    /**
     * Creates a new, empty map with the specified initial capacity, and with default load factor (0.75) and concurrencyLevel (16).
     * 
     * @param initialCapacity
     *            the initial capacity. The implementation performs internal sizing to accommodate this many elements.
     * @throws IllegalArgumentException
     *             if the initial capacity of elements is negative.
     */
    public ConcurrentPooledElementsMap( int initialCapacity ) {
        this( initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL );
    }

    /**
     * Creates a new, empty map with a default initial capacity (16), load factor (0.75) and concurrencyLevel (16).
     */
    public ConcurrentPooledElementsMap() {
        this( DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL );
    }

    /**
     * Creates a new map with the same mappings as the given map. The map is created with a capacity of 1.5 times the number of mappings in the given map or 16
     * (whichever is greater), and a default load factor (0.75) and concurrencyLevel (16).
     * 
     * @param m
     *            the map
     */
    public ConcurrentPooledElementsMap( Map<? extends K, ? extends V> m ) {
        this( Math.max( (int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY ), DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL );
        putAll( m );
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        final Segment<K,V>[] segments = this.segments;
        /*
         * We keep track of per-Segment<K,V>modCounts to avoid ABA problems in which an element in one Segment<K,V>was added and in another removed during
         * traversal, in which case the table was never actually empty at any point. Note the similar use of modCounts in the size() and containsValue()
         * methods, which are the only other methods also susceptible to ABA problems.
         */
        int[] mc = new int[segments.length];
        int mcsum = 0;
        for ( int i = 0 ; i < segments.length ; ++i ) {
            if ( segments[i].count != 0 )
                return false;
            mcsum += mc[i] = segments[i].modCount;
        }
        // If mcsum happens to be zero, then we know we got a snapshot
        // before any modifications at all were made. This is
        // probably common enough to bother tracking.
        if ( mcsum != 0 ) {
            for ( int i = 0 ; i < segments.length ; ++i ) {
                if ( segments[i].count != 0 || mc[i] != segments[i].modCount )
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     * 
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        final Segment<K,V>[] segments = this.segments;
        long sum = 0;
        long check = 0;
        int[] mc = new int[segments.length];
        // Try a few times to get accurate count. On failure due to
        // continuous async changes in table, resort to locking.
        for ( int k = 0 ; k < RETRIES_BEFORE_LOCK ; ++k ) {
            check = 0;
            sum = 0;
            int mcsum = 0;
            for ( int i = 0 ; i < segments.length ; ++i ) {
                sum += segments[i].count;
                mcsum += mc[i] = segments[i].modCount;
            }
            if ( mcsum != 0 ) {
                for ( int i = 0 ; i < segments.length ; ++i ) {
                    check += segments[i].count;
                    if ( mc[i] != segments[i].modCount ) {
                        check = -1; // force retry
                        break;
                    }
                }
            }
            if ( check == sum )
                break;
        }
        if ( check != sum ) { // Resort to locking all segments
            sum = 0;
            for ( int i = 0 ; i < segments.length ; ++i )
                segments[i].lock();
            for ( int i = 0 ; i < segments.length ; ++i )
                sum += segments[i].count;
            for ( int i = 0 ; i < segments.length ; ++i )
                segments[i].unlock();
        }
        if ( sum > Integer.MAX_VALUE )
            return Integer.MAX_VALUE;
        return (int) sum;
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
     * 
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that {@code key.equals(k)}, then this method returns
     * {@code v}; otherwise it returns {@code null}. (There can be at most one such mapping.)
     * 
     * @throws NullPointerException
     *             if the specified key is null
     */
    @Override
    public V get( Object key ) {
        int hash = hash( key.hashCode() );
        return segmentFor( hash ).get( key, hash );
    }

    /**
     * Tests if the specified object is a key in this table.
     * 
     * @param key
     *            possible key
     * @return <tt>true</tt> if and only if the specified object is a key in this table, as determined by the <tt>equals</tt> method; <tt>false</tt> otherwise.
     * @throws NullPointerException
     *             if the specified key is null
     */
    @Override
    public boolean containsKey( Object key ) {
        int hash = hash( key.hashCode() );
        return segmentFor( hash ).containsKey( key, hash );
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the specified value. Note: This method requires a full internal traversal of the hash table,
     * and so is much slower than method <tt>containsKey</tt>.
     * 
     * @param value
     *            value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the specified value
     * @throws NullPointerException
     *             if the specified value is null
     */
    @Override
    public boolean containsValue( Object value ) {
        if ( value == null )
            throw new NullPointerException();

        // See explanation of modCount use above

        final Segment<K,V>[] segments = this.segments;
        int[] mc = new int[segments.length];

        // Try a few times without locking
        for ( int k = 0 ; k < RETRIES_BEFORE_LOCK ; ++k ) {
            int sum = 0;
            int mcsum = 0;
            for ( int i = 0 ; i < segments.length ; ++i ) {
                int c = segments[i].count;
                mcsum += mc[i] = segments[i].modCount;
                if ( segments[i].containsValue( value ) )
                    return true;
            }
            boolean cleanSweep = true;
            if ( mcsum != 0 ) {
                for ( int i = 0 ; i < segments.length ; ++i ) {
                    int c = segments[i].count;
                    if ( mc[i] != segments[i].modCount ) {
                        cleanSweep = false;
                        break;
                    }
                }
            }
            if ( cleanSweep )
                return false;
        }
        // Resort to locking all segments
        for ( int i = 0 ; i < segments.length ; ++i )
            segments[i].lock();
        boolean found = false;
        try {
            for ( int i = 0 ; i < segments.length ; ++i ) {
                if ( segments[i].containsValue( value ) ) {
                    found = true;
                    break;
                }
            }
        } finally {
            for ( int i = 0 ; i < segments.length ; ++i )
                segments[i].unlock();
        }
        return found;
    }

    /**
     * Legacy method testing if some key maps into the specified value in this table. This method is identical in functionality to {@link #containsValue}, and
     * exists solely to ensure full compatibility with class {@link java.util.Hashtable}, which supported this method prior to introduction of the Java
     * Collections framework.
     * 
     * @param value
     *            a value to search for
     * @return <tt>true</tt> if and only if some key maps to the <tt>value</tt> argument in this table as determined by the <tt>equals</tt> method;
     *         <tt>false</tt> otherwise
     * @throws NullPointerException
     *             if the specified value is null
     */
    public boolean contains( Object value ) {
        return containsValue( value );
    }

    /**
     * Maps the specified key to the specified value in this table. The key cannot be null, the value can be null.
     * 
     * <p>
     * The value can be retrieved by calling the <tt>get</tt> method with a key that is equal to the original key.
     * 
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    @Override
    public V put( K key, V value ) {
        int hash = hash( key.hashCode() );
        return segmentFor( hash ).put( key, hash, value, false );
    }

    /**
     * {@inheritDoc}
     * 
     * @return the previous value associated with the specified key, or <tt>null</tt> if there was no mapping for the key
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    @Override
    public V putIfAbsent( K key, V value ) {
        int hash = hash( key.hashCode() );
        return segmentFor( hash ).put( key, hash, value, true );
    }

    /**
     * Copies all of the mappings from the specified map to this one. These mappings replace any mappings that this map had for any of the keys currently in the
     * specified map.
     * 
     * @param m
     *            mappings to be stored in this map
     */
    @Override
    public void putAll( Map<? extends K, ? extends V> m ) {
        for ( Map.Entry<? extends K, ? extends V> e : m.entrySet() )
            put( e.getKey(), e.getValue() );
    }

    /**
     * Removes the key (and its corresponding value) from this map. This method does nothing if the key is not in the map.
     * 
     * @param key
     *            the key that needs to be removed
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException
     *             if the specified key is null
     */
    @Override
    public V remove( Object key ) {
        int hash = hash( key.hashCode() );
        return segmentFor( hash ).remove( key, hash, null );
    }

    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException
     *             if the specified key is null
     */
    @Override
    public boolean remove( Object key, Object value ) {
        int hash = hash( key.hashCode() );
        if ( value == null )
            return false;
        return segmentFor( hash ).remove( key, hash, value ) != null;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException
     *             if any of the arguments are null
     */
    @Override
    public boolean replace( K key, V oldValue, V newValue ) {
        if ( oldValue == null || newValue == null )
            throw new NullPointerException();
        int hash = hash( key.hashCode() );
        return segmentFor( hash ).replace( key, hash, oldValue, newValue );
    }

    /**
     * {@inheritDoc}
     * 
     * @return the previous value associated with the specified key, or <tt>null</tt> if there was no mapping for the key
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    @Override
    public V replace( K key, V value ) {
        if ( value == null )
            throw new NullPointerException();
        int hash = hash( key.hashCode() );
        return segmentFor( hash ).replace( key, hash, value );
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        for ( int i = 0 ; i < segments.length ; ++i )
            segments[i].clear();
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. The set supports element removal, which removes the corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     * 
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException}, and guarantees to traverse
     * elements as they existed upon construction of the iterator, and may (but is not guaranteed to) reflect any modifications subsequent to construction.
     */
    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map. The collection is backed by the map, so changes to the map are reflected in the
     * collection, and vice-versa. The collection supports element removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     * 
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException}, and guarantees to traverse
     * elements as they existed upon construction of the iterator, and may (but is not guaranteed to) reflect any modifications subsequent to construction.
     */
    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map. The set is backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. The set supports element removal, which removes the corresponding mapping from the map, via the <tt>Iterator.remove</tt>, <tt>Set.remove</tt>
     * , <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     * 
     * <p>
     * The view's <tt>iterator</tt> is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException}, and guarantees to traverse
     * elements as they existed upon construction of the iterator, and may (but is not guaranteed to) reflect any modifications subsequent to construction.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    /**
     * Returns an enumeration of the keys in this table.
     * 
     * @return an enumeration of the keys in this table
     * @see #keySet()
     */
    public Enumeration<K> keys() {
        return new KeyIterator();
    }

    /**
     * Returns an enumeration of the values in this table.
     * 
     * @return an enumeration of the values in this table
     * @see #values()
     */
    public Enumeration<V> elements() {
        return new ValueIterator();
    }

    /* ---------------- Iterator Support -------------- */

    abstract class HashIterator {

        int                   nextSegmentIndex;
        int                   nextTableIndex;
        ConcHashEntry<K, V>[] currentTable;
        ConcHashEntry<K, V>   nextEntry;
        ConcHashEntry<K, V>   lastReturned;

        HashIterator() {
            nextSegmentIndex = segments.length - 1;
            nextTableIndex = -1;
            advance();
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        final void advance() {
            if ( nextEntry != null && (nextEntry = nextEntry.next) != null )
                return;

            while( nextTableIndex >= 0 ) {
                if ( (nextEntry = currentTable[nextTableIndex--]) != null )
                    return;
            }

            while( nextSegmentIndex >= 0 ) {
                Segment<K, V> seg = segments[nextSegmentIndex--];
                if ( seg.count != 0 ) {
                    currentTable = seg.table;
                    for ( int j = currentTable.length - 1 ; j >= 0 ; --j ) {
                        if ( (nextEntry = currentTable[j]) != null ) {
                            nextTableIndex = j - 1;
                            return;
                        }
                    }
                }
            }
        }

        public boolean hasNext() {
            return nextEntry != null;
        }

        ConcHashEntry<K, V> nextEntry() {
            if ( nextEntry == null )
                throw new NoSuchElementException();
            lastReturned = nextEntry;
            advance();
            return lastReturned;
        }

        public void remove() {
            if ( lastReturned == null )
                throw new IllegalStateException();
            ConcurrentPooledElementsMap.this.remove( lastReturned.key );
            lastReturned = null;
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<K>, Enumeration<K> {

        @Override
        public K next() {
            return super.nextEntry().key;
        }

        @Override
        public K nextElement() {
            return super.nextEntry().key;
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V>, Enumeration<V> {

        @Override
        public V next() {
            return super.nextEntry().value;
        }

        @Override
        public V nextElement() {
            return super.nextEntry().value;
        }
    }

    /**
     * Custom Entry class used by EntryIterator.next(), that relays setValue changes to the underlying map.
     */
    @SuppressWarnings( { "serial" } )
    final class WriteThroughEntry extends AbstractMap.SimpleEntry<K,V> {

        WriteThroughEntry( K k, V v ) {
            super( k, v );
        }

        /**
         * Set our entry's value and write through to the map. The value to return is somewhat arbitrary here. Since a WriteThroughEntry does not necessarily
         * track asynchronous changes, the most recent "previous" value could be different from what we return (or could even have been removed in which case
         * the put will re-establish). We do not and cannot guarantee more.
         */
        @Override
        public V setValue( V value ) {
            if ( value == null )
                throw new NullPointerException();
            
            V v = super.setValue( value );
            ConcurrentPooledElementsMap.this.put( getKey(), value );
            return v;
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Entry<K, V>> {

        @Override
        public Map.Entry<K, V> next() {
            ConcHashEntry<K, V> e = super.nextEntry();
            return new WriteThroughEntry( e.key, e.value );
        }
    }

    final class KeySet extends AbstractSet<K> {

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return ConcurrentPooledElementsMap.this.size();
        }

        @Override
        public boolean contains( Object o ) {
            return ConcurrentPooledElementsMap.this.containsKey( o );
        }

        @Override
        public boolean remove( Object o ) {
            return ConcurrentPooledElementsMap.this.remove( o ) != null;
        }

        @Override
        public void clear() {
            ConcurrentPooledElementsMap.this.clear();
        }
    }

    final class Values extends AbstractCollection<V> {

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return ConcurrentPooledElementsMap.this.size();
        }

        @Override
        public boolean contains( Object o ) {
            return ConcurrentPooledElementsMap.this.containsValue( o );
        }

        @Override
        public void clear() {
            ConcurrentPooledElementsMap.this.clear();
        }
    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean contains( Object o ) {
            if ( !(o instanceof Map.Entry) )
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            V v = ConcurrentPooledElementsMap.this.get( e.getKey() );
            return v != null && v.equals( e.getValue() );
        }

        @Override
        public boolean remove( Object o ) {
            if ( !(o instanceof Map.Entry) )
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return ConcurrentPooledElementsMap.this.remove( e.getKey(), e.getValue() );
        }

        @Override
        public int size() {
            return ConcurrentPooledElementsMap.this.size();
        }

        @Override
        public void clear() {
            ConcurrentPooledElementsMap.this.clear();
        }
    }

    /* ---------------- Serialization Support -------------- */

    /**
     * Save the state of the <tt>ConcurrentPooledElementsMap</tt> instance to a stream (i.e., serialize it).
     * 
     * @param s
     *            the stream
     * @serialData the key (Object) and value (Object) for each key-value mapping, followed by a null pair. The key-value mappings are emitted in no particular
     *             T.
     */
    private void writeObject( java.io.ObjectOutputStream s ) throws IOException {
        s.defaultWriteObject();

        for ( int k = 0 ; k < segments.length ; ++k ) {
            Segment<K, V> seg = segments[k];
            seg.lock();
            try {
                ConcHashEntry<K, V>[] tab = seg.table;
                for ( int i = 0 ; i < tab.length ; ++i ) {
                    for ( ConcHashEntry<K, V> e = tab[i] ; e != null ; e = e.next ) {
                        s.writeObject( e.key );
                        s.writeObject( e.value );
                    }
                }
            } finally {
                seg.unlock();
            }
        }
        s.writeObject( null );
        s.writeObject( null );
    }

    /**
     * Reconstitute the <tt>ConcurrentPooledElementsMap</tt> instance from a stream (i.e., deserialize it).
     * 
     * @param s
     *            the stream
     */
    private void readObject( java.io.ObjectInputStream s ) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Initialize each Segment<K,V>to be minimally sized, and let grow.
        for ( int i = 0 ; i < segments.length ; ++i ) {
            segments[i].setTable( new ConcHashEntry[1] );
        }

        // Read the keys and values, and put the mappings in the table
        for ( ; ; ) {
            K key = (K) s.readObject();
            V value = (V) s.readObject();
            if ( key == null )
                break;
            put( key, value );
        }
    }
}
