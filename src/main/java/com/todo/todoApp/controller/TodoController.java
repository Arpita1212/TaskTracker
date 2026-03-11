package com.todo.todoApp.controller;

import com.todo.todoApp.entity.Todo;
import com.todo.todoApp.service.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // ------------------ HOME / DASHBOARD ------------------
    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "1") int page, Model model) {

        List<Todo> todos = todoService.getAllTasks();
        todos = smartSort(todos);

        // Dashboard stats
        addDashboardStats(model, todos);

        model.addAttribute("todos", todos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1); // No real pagination yet

        return "home";
    }

    // ------------------ INSERT TASK ------------------
    @GetMapping("/insert")
    public String showInsertForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "insert";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute Todo todo) {
        todoService.saveTask(todo);
        return "redirect:/";
    }

    // ------------------ UPDATE TASK ------------------
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        Todo todo = todoService.getTaskById(id);
        model.addAttribute("todo", todo);
        return "update-form";
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute Todo todo) {
        todoService.updateTask(id, todo);
        return "redirect:/";
    }

    // ------------------ DELETE TASK ------------------
    @DeleteMapping("/todos/{id}")
    public String deleteTask(@PathVariable Long id) {
        todoService.deleteTask(id);
        return "redirect:/";
    }

    // ------------------ FILTER ------------------
    @GetMapping("/filter")
    public String filterTasks(@RequestParam(required = false) Long id,
                              @RequestParam(required = false) String status,
                              Model model) {

        List<Todo> todos = todoService.getAllTasks();

        if (id != null) {
            todos = todos.stream()
                    .filter(t -> t.getId().equals(id))
                    .toList();
        }

        if (status != null && !status.isEmpty()) {
            todos = todos.stream()
                    .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                    .toList();
        }

        todos = smartSort(todos);

        model.addAttribute("todos", todos);
        addDashboardStats(model, todos);

        return "home";
    }

    // ------------------ TODAY'S TASKS ------------------
    @GetMapping("/today")
    public String todaysTasks(Model model) {

        List<Todo> todos = todoService.getAllTasks();
        LocalDate today = LocalDate.now();

        List<Todo> todayTasks = todos.stream()
                .filter(t -> t.getDeadline() != null &&
                        t.getDeadline().toLocalDate().equals(today))
                .toList();

        todayTasks = smartSort(todayTasks);

        model.addAttribute("todos", todayTasks);
        addDashboardStats(model, todayTasks);

        return "home";
    }

    // ------------------ CALENDAR VIEW ------------------
    @GetMapping("/calendar")
    public String calendarView(Model model) {

        List<Todo> todos = todoService.getAllTasks();

        // Convert to JSON-friendly objects
        List<Map<String, String>> calendarTodos = todos.stream().map(t -> {
            Map<String, String> map = new HashMap<>();
            map.put("title", t.getTaskname());
            if(t.getDeadline() != null) {
                map.put("start", t.getDeadline().toString()); // LocalDateTime as ISO string
            }
            return map;
        }).toList();

        model.addAttribute("calendarTodos", calendarTodos);

        return "calendar";
    }

    // ------------------ HELPER METHODS ------------------
    private void addDashboardStats(Model model, List<Todo> todos) {

        long total = todos.size();

        long completed = todos.stream()
                .filter(t -> "Completed".equalsIgnoreCase(t.getStatus()))
                .count();

        long pending = todos.stream()
                .filter(t -> "Pending".equalsIgnoreCase(t.getStatus()))
                .count();

        long overdue = todos.stream()
                .filter(t -> t.getDeadline() != null &&
                        t.getDeadline().isBefore(LocalDateTime.now()) &&
                        !"Completed".equalsIgnoreCase(t.getStatus()))
                .count();

        model.addAttribute("totalTasks", total);
        model.addAttribute("completedTasks", completed);
        model.addAttribute("pendingTasks", pending);
        model.addAttribute("overdueTasks", overdue);
    }

    private List<Todo> smartSort(List<Todo> todos) {

        LocalDateTime now = LocalDateTime.now();

        return todos.stream()
                .sorted((a, b) -> {

                    boolean aOverdue = a.getDeadline() != null && a.getDeadline().isBefore(now);
                    boolean bOverdue = b.getDeadline() != null && b.getDeadline().isBefore(now);

                    if (aOverdue && !bOverdue) return -1;
                    if (!aOverdue && bOverdue) return 1;

                    if (a.getDeadline() == null) return 1;
                    if (b.getDeadline() == null) return -1;

                    return a.getDeadline().compareTo(b.getDeadline());
                })
                .toList();
    }
    @GetMapping("/search")
    public String searchTasks(@RequestParam String keyword, Model model){

        List<Todo> todos = todoService.getAllTasks();

        List<Todo> filtered = todos.stream()
                .filter(t -> t.getTaskname().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        filtered = smartSort(filtered);

        model.addAttribute("todos", filtered);

        addDashboardStats(model, filtered);

        return "home";
    }
    @GetMapping("/page")
    public String paginate(@RequestParam(defaultValue = "0") int page,
                           Model model){

        List<Todo> todos = todoService.getAllTasks();

        int pageSize = 5;

        int start = page * pageSize;

        int end = Math.min(start + pageSize, todos.size());

        List<Todo> pageContent = todos.subList(start, end);

        model.addAttribute("todos", pageContent);
        model.addAttribute("currentPage", page);

        addDashboardStats(model, pageContent);

        return "home";
    }

}
