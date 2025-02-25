package org.example;

public class CounterImpl implements Counter {
    private int _counter;

    @Override
    public int getCounter() {
        return _counter;
    }

    @Override
    public void setCounter(int newCount) {
        _counter = newCount;
    }
}
