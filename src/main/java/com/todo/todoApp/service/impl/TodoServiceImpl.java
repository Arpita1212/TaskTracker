package com.todo.todoApp.service.impl;

import com.todo.todoApp.entity.Todo;
import com.todo.todoApp.entity.User;
import com.todo.todoApp.repository.TodoRepository;
import com.todo.todoApp.repository.UserRepository;
import com.todo.todoApp.service.TodoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public TodoServiceImpl(TodoRepository todoRepository,
                           UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    // ===============================
    // EXISTING TODO FUNCTIONALITY
    // ===============================

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
        return todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public Todo updateTask(Long id, Todo todo) {

        Todo existing = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        existing.setTaskname(todo.getTaskname());
        existing.setDescription(todo.getDescription());
        existing.setStatus(todo.getStatus());
        existing.setDeadline(todo.getDeadline());
        existing.setPriority(todo.getPriority());

        return todoRepository.save(existing);
    }

    @Override
    public void deleteTask(Long id) {
        todoRepository.deleteById(id);
    }

    @Override
    public List<Todo> filterTasks(Long id, String status) {

        if (status == null || status.isEmpty()) {
            return todoRepository.findAll();
        }

        return todoRepository.findByStatus(status);
    }

    @Override
    public Page<Todo> getTodos(int page) {
        return todoRepository.findAll(PageRequest.of(page, 5));
    }

    // ===============================
    // JWT USER BASED FUNCTIONALITY
    // ===============================

    @Override
    public List<Todo> getTasksByUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return todoRepository.findByUser(user);
    }

    @Override
    public Todo saveTodo(Todo todo, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        todo.setUser(user);

        return todoRepository.save(todo);
    }

}