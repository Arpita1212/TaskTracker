package com.todo.todoApp.controller;

import com.todo.todoApp.entity.Todo;
import com.todo.todoApp.service.TodoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tasks")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    // ================= JWT API =================
    @GetMapping("/api")
    @ResponseBody
    public List<Todo> getTasks(Authentication auth) {

        String email = auth.getName();
        return todoService.getTasksByUser(email);
    }

    // ================= ADD TASK API =================
    @PostMapping("/api")
    @ResponseBody
    public Todo addTask(@RequestBody Todo todo, Authentication auth){

        String email = auth.getName();
        return todoService.saveTodo(todo,email);
    }

    // ================= HOME =================
    @GetMapping({"", "/"})
    public String home(HttpSession session, Model model) {

        String email = (String) session.getAttribute("userEmail");

        if(email == null){
            return "redirect:/login";
        }

        List<Todo> todos = todoService.getTasksByUser(email);

        todos = smartSort(todos);

        addDashboardStats(model, todos);

        model.addAttribute("todos", todos);

        return "home";
    }

    // ================= INSERT =================
    @GetMapping("/insert")
    public String showInsertForm(Model model) {

        model.addAttribute("todo", new Todo());
        return "insert";
    }

    @PostMapping("/save")
    public String saveTask(@ModelAttribute Todo todo,
                           HttpSession session) {

        String email = (String) session.getAttribute("userEmail");

        if(email == null){
            return "redirect:/login";
        }

        todoService.saveTodo(todo,email);

        return "redirect:/tasks";
    }

    // ================= UPDATE =================
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id,
                                 Model model) {

        Todo todo = todoService.getTaskById(id);
        model.addAttribute("todo", todo);

        return "update-form";
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @ModelAttribute Todo todo) {

        todoService.updateTask(id, todo);

        return "redirect:/tasks";
    }

    // ================= DELETE =================
    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {

        todoService.deleteTask(id);
        return "redirect:/tasks";
    }

    // ================= SEARCH =================
    @GetMapping("/search")
    public String searchTasks(@RequestParam String keyword,
                              HttpSession session,
                              Model model) {

        String email = (String) session.getAttribute("userEmail");

        List<Todo> todos = todoService.getTasksByUser(email);

        List<Todo> filtered = todos.stream()
                .filter(t -> t.getTaskname()
                        .toLowerCase()
                        .contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        filtered = smartSort(filtered);

        model.addAttribute("todos", filtered);

        addDashboardStats(model, filtered);

        return "home";
    }

    // ================= TODAY TASKS =================
    @GetMapping("/today")
    public String todaysTasks(HttpSession session,
                              Model model) {

        String email = (String) session.getAttribute("userEmail");

        List<Todo> todos = todoService.getTasksByUser(email);

        LocalDate today = LocalDate.now();

        List<Todo> todayTasks = todos.stream()
                .filter(t -> t.getDeadline() != null &&
                        t.getDeadline().toLocalDate().equals(today))
                .collect(Collectors.toList());

        todayTasks = smartSort(todayTasks);

        model.addAttribute("todos", todayTasks);

        addDashboardStats(model, todayTasks);

        return "home";
    }

    // ================= CALENDAR =================
    @GetMapping("/calendar")
    public String calendarView(HttpSession session,
                               Model model) {

        String email = (String) session.getAttribute("userEmail");

        List<Todo> todos = todoService.getTasksByUser(email);

        List<Map<String,String>> calendarTodos = todos.stream().map(t -> {

            Map<String,String> map = new HashMap<>();

            map.put("title", t.getTaskname());

            if(t.getDeadline()!=null){
                map.put("start", t.getDeadline().toString());
            }

            return map;

        }).collect(Collectors.toList());

        model.addAttribute("calendarTodos", calendarTodos);

        return "calendar";
    }

    // ================= DASHBOARD STATS =================
    private void addDashboardStats(Model model,
                                   List<Todo> todos) {

        long total = todos.size();

        long completed = todos.stream()
                .filter(t -> "Completed".equalsIgnoreCase(t.getStatus()))
                .count();

        long pending = todos.stream()
                .filter(t -> "Pending".equalsIgnoreCase(t.getStatus()))
                .count();

        long overdue = todos.stream()
                .filter(t -> t.getDeadline()!=null &&
                        t.getDeadline().isBefore(LocalDateTime.now()) &&
                        !"Completed".equalsIgnoreCase(t.getStatus()))
                .count();

        model.addAttribute("totalTasks", total);
        model.addAttribute("completedTasks", completed);
        model.addAttribute("pendingTasks", pending);
        model.addAttribute("overdueTasks", overdue);
    }

    // ================= SMART SORT =================
    private List<Todo> smartSort(List<Todo> todos) {

        LocalDateTime now = LocalDateTime.now();

        return todos.stream()
                .sorted((a,b)->{

                    boolean aOverdue = a.getDeadline()!=null && a.getDeadline().isBefore(now);
                    boolean bOverdue = b.getDeadline()!=null && b.getDeadline().isBefore(now);

                    if(aOverdue && !bOverdue) return -1;
                    if(!aOverdue && bOverdue) return 1;

                    if(a.getDeadline()==null) return 1;
                    if(b.getDeadline()==null) return -1;

                    return a.getDeadline().compareTo(b.getDeadline());

                })
                .collect(Collectors.toList());
    }

    // ------------------ FILTER ------------------
    @GetMapping("/filter")
    public String filterTasks(@RequestParam(required = false) Long id,
                              @RequestParam(required = false) String status,
                              HttpSession session,
                              Model model) {
        String email = (String) session.getAttribute("userEmail");
        List<Todo> todos = todoService.getTasksByUser(email);

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

}