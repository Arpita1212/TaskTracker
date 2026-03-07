package com.todo.todoApp.service;

import com.todo.todoApp.entity.Todo;
import com.todo.todoApp.repository.TodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;

    public TodoServiceImpl(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public Todo saveTask(Todo todo) {
        return todoRepository.save(todo);
    }

    @Override
    public List<Todo> getAllTasks() {
        return todoRepository.findAll();
    }

    @Override
    public Todo getTaskById(Long id) {
        return todoRepository.findById(id).orElse(null);
    }

    @Override
    public Todo updateTask(Long id, Todo todo) {
        todo.setId(id);
        return todoRepository.save(todo);
    }

    @Override
    public void deleteTask(Long id) {
        todoRepository.deleteById(id);
    }

    @Override
    public List<Todo> filterTasks(Long id, String status) {
        return todoRepository.findAll();
    }

    @Override
    public Page<Todo> getTodos(int page) {

        Pageable pageable = PageRequest.of(page - 1, 5);

        return todoRepository.findAll(pageable);
    }
}