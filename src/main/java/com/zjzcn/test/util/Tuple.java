package com.zjzcn.test.util;

public abstract class Tuple {

    public static  <E, T> Tuple of (E e, T t) {
        return new Tuple2(e, t);
    }

    public static  <E, T, K> Tuple of (E e, T t, K k) {
        return new Tuple3(e, t, k);
    }

    public abstract <E> E _1 ();

    public abstract <E> E _2 ();

    public abstract <E> E _3 ();

}

class Tuple2<E, T> extends Tuple {
    private E e;
    private T t;

    Tuple2 (E e, T t) {
        this.e = e;
        this.t = t;
    }

    @Override
    public E _1 () {
        return e;
    }

    @Override
    public T _2 () {
        return t;
    }

    @Override
    public <E> E _3() {
        throw new IllegalStateException("Can not access.");
    }

    @Override
    public String toString() {
        return _1() + " | " + _2();
    }
}

class Tuple3<E, T, K> extends Tuple {
    private E e;
    private T t;
    private K k;

    Tuple3 (E e, T t, K k) {
        this.e = e;
        this.t = t;
        this.k = k;
    }

    @Override
    public E _1 () {
        return e;
    }

    @Override
    public T _2 () {
        return t;
    }

    @Override
    public K _3() {
        return k;
    }

    @Override
    public String toString() {
        return _1() + " | " + _2() + " | " + _3();
    }
}
