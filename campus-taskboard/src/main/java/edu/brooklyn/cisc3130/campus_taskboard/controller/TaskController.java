package edu.brooklyn.cisc3130.campus_taskboard.controller;

import edu.brooklyn.cisc3130.campus_taskboard.dto.TaskRequest;
import edu.brooklyn.cisc3130.campus_taskboard.dto.TaskResponse;
import edu.brooklyn.cisc3130.campus_taskboard.model.Task;
import edu.brooklyn.cisc3130.campus_taskboard.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET ALL TASKS
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // GET TASK BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Integer id) {
        Optional<Task> task = taskService.getTaskById(id);

        return task.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE TASK
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest taskRequest) {

        Task task = new Task();

        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());

        task.setCompleted(
                taskRequest.getCompleted() != null
                        ? taskRequest.getCompleted()
                        : false
        );

        // Handle priority safely
        Task.Priority priority;

        try {
            priority = Task.Priority.valueOf(
                    taskRequest.getPriority() != null
                            ? taskRequest.getPriority().toUpperCase()
                            : "MEDIUM"
            );
        } catch (IllegalArgumentException e) {
            priority = Task.Priority.MEDIUM;
        }

        task.setPriority(priority);

        Task createdTask = taskService.createTask(task);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TaskResponse.fromEntity(createdTask));
    }

    // UPDATE TASK
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Integer id,
            @RequestBody Task updatedTask) {

        Optional<Task> optionalTask = taskService.getTaskById(id);

        if (optionalTask.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = optionalTask.get();

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setCompleted(updatedTask.getCompleted());
        task.setPriority(updatedTask.getPriority());

        Task savedTask = taskService.createTask(task);

        return ResponseEntity.ok(savedTask);
    }

    // DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {

        boolean deleted = taskService.deleteTask(id);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // VALIDATION ERROR HANDLER
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            errors.put(fieldName, errorMessage);
        });

        return errors;
    }

    // GET COMPLETED TASKS
    @GetMapping("/completed")
    public ResponseEntity<List<Task>> getCompletedTasks() {

        List<Task> tasks = taskService.getCompletedTasks();

        return ResponseEntity.ok(tasks);
    }

    // GET INCOMPLETE TASKS
    @GetMapping("/incomplete")
    public ResponseEntity<List<Task>> getIncompleteTasks() {

        List<Task> tasks = taskService.getIncompleteTasks();

        return ResponseEntity.ok(tasks);
    }

    // GET TASKS BY PRIORITY
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(
            @PathVariable String priority) {

        try {
            Task.Priority priorityEnum =
                    Task.Priority.valueOf(priority.toUpperCase());

            List<Task> tasks =
                    taskService.getTasksByPriority(priorityEnum);

            return ResponseEntity.ok(tasks);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().build();
        }
    }

    // SEARCH TASKS
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(
            @RequestParam String keyword) {

        List<Task> tasks = taskService.searchTasks(keyword);

        return ResponseEntity.ok(tasks);
    }

    // PAGINATED TASKS
    @GetMapping("/paginated")
    public ResponseEntity<Page<Task>> getTasksPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortBy));

        Page<Task> tasks =
                taskService.getAllTasks(pageable);

        return ResponseEntity.ok(tasks);
    }
}