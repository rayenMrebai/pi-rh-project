package org.example.interfaces;

import java.util.List;

public interface GlobalInterface<T> {

    // Create
    void create(T entity);

    // Read
    List<T> getAll();

    // Update
    void update(T entity);

    // Delete
    void delete(int id);
}

