package org.example;

@AssignOpAnnotation
public interface Counter {
    @ToBeMigrated
    int getCounter();
    void setCounter(int newCount);
}
