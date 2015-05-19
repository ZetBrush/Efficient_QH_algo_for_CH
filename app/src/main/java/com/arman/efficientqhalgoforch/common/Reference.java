package com.arman.efficientqhalgoforch.common;

/**
 * Created by Arman.
 */
public class Reference<T> {

    private volatile T t;

    public Reference(T t) {
        this.t = t;
    }

    public void update(T t){
        this.t = t;
    }

    public T get(){
        return t;
    }
}