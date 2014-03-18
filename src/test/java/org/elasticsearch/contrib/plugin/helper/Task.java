package org.elasticsearch.contrib.plugin.helper;

public interface Task<T> {
    T execute();
}
