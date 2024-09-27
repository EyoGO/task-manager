package com.eyogo.taskmanager.mapper;

public interface Mapper<F, T> {
    T map(F from);
}
