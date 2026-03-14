package com.todo.todoApp.service;

import com.todo.todoApp.entity.Todo;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TodoService {

    // ----- Existing Todo functionality -----

    Todo saveTask(Todo todo);

    List<Todo> getAllTasks();

    Todo getTaskById(Long id);

    Todo updateTask(Long id, Todo todo);

    void deleteTask(Long id);

    List<Todo> filterTasks(Long id, String status);

    Page<Todo> getTodos(int page);


    // ----- User based functionality (JWT) -----

    List<Todo> getTasksByUser(String email);

    Todo saveTodo(Todo todo, String email);

}