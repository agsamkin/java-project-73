package hexlet.code.service.impl;

import hexlet.code.dto.TaskDto;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    private final TaskStatusService taskStatusService;

    private final UserService userService;
    private final UserRepository userRepository;
    private final LabelService labelService;

    private TaskDto convertToTaskDto(Task task) {
        return modelMapper.map(task, TaskDto.class);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task getTaskById(long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
    }

    @Override
    public Task createTask(TaskDto taskDto) {
        User executor = null;
        if (Objects.nonNull(taskDto.getExecutorId())) {
            executor = userRepository
                    .findById(taskDto.getExecutorId())
                    .orElseThrow(() -> new NoSuchElementException("Executor not found"));
        }

        List<Label> labels = taskDto.getLabelIds().stream()
                .map(labelService::getLabelById)
                .collect(Collectors.toList());

        Task newTask = Task.builder()
                .name(taskDto.getName())
                .description(taskDto.getDescription())
                .taskStatus(taskStatusService.getTaskStatusById(taskDto.getTaskStatusId()))
                .author(userService.getCurrentUser())
                .labels(labels)
                .executor(executor).build();

        return taskRepository.save(newTask);
    }

    @Override
    public Task updateTask(long id, TaskDto taskDto) {
        Task task = getTaskById(id);
        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());

        if (Objects.nonNull(taskDto.getExecutorId())) {
            User executor = userRepository
                    .findById(taskDto.getExecutorId())
                    .orElseThrow(() -> new NoSuchElementException("Executor not found"));
            task.setExecutor(executor);
        }

        TaskStatus taskStatus = taskStatusService.getTaskStatusById(
                taskDto.getTaskStatusId()
        );
        task.setTaskStatus(taskStatus);

        List<Label> labels = taskDto.getLabelIds().stream()
                .map(labelService::getLabelById)
                .collect(Collectors.toList());
        task.setLabels(labels);

        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

}
