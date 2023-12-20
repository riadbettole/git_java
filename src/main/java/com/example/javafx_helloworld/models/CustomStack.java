package com.example.javafx_helloworld.models;

import java.util.HashSet;
import java.util.Stack;

public class CustomStack<T> extends Stack<T>{
    private final int maxSize;
    private final HashSet<T> set;

    public CustomStack(){
        super();
        this.maxSize = 3;
        this.set = new HashSet<>();
    }

    @Override
    public synchronized T push(T object){
        if(set.contains(object)){
            super.remove(object);
        }
        else{
            while (this.size() >= maxSize){
                this.remove(0);
            }
            this.set.add(object);
        }
        super.push(object);
        return object;
    }

    @Override
    public synchronized T pop(){
        if (!isEmpty()) {
            T item = super.pop();
            this.set.remove(item);
            return item;
        }
        return null;
    }

    public CustomStack(CustomStack<T> other){
        super();
        this.maxSize = 3;
        this.set = new HashSet<>();
        for(T item : other){
            super.push(item);
        }
    }

}
