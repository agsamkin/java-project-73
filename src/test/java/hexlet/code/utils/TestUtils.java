package hexlet.code.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.component.JWTHelper;
import hexlet.code.controller.LabelController;
import hexlet.code.controller.TaskController;
import hexlet.code.controller.TaskStatusController;
import hexlet.code.controller.UserController;
import hexlet.code.dto.LabelDto;
import hexlet.code.dto.TaskDto;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class TestUtils {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTHelper jwtHelper;

    public static final String BASE_URL = "/api";
    public static final String TEST_USERNAME = "test@test.com";

    private final UserDto testRegistrationUser = UserDto.builder()
            .firstName("First name")
            .lastName("Last name")
            .email(TEST_USERNAME)
            .password("Password").build();

    public static final UserDto TEST_USER_1 = UserDto.builder()
            .firstName("New first name")
            .lastName("New last name")
            .email("new_test@test.com")
            .password("New password").build();

    public static final TaskStatusDto TEST_TASK_STATUS_1 = TaskStatusDto.builder()
            .name("Task status").build();

    public static final TaskStatusDto TEST_TASK_STATUS_2 = TaskStatusDto.builder()
            .name("New task status").build();

    public static final LabelDto TEST_LABEL_1 = LabelDto.builder()
            .name("Label").build();

    public static final LabelDto TEST_LABEL_2 = LabelDto.builder()
            .name("New label").build();

    public UserDto getTestRegistrationUser() {
        return testRegistrationUser;
    }

    public void clear() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();
    }

    public ResultActions regDefaultTask(final String byUser) throws Exception {
        regDefaultUser();
        regDefaultLabel(TEST_USERNAME);
        regDefaultTaskStatus(TEST_USERNAME);
        final User user = userRepository.findAll().get(0);
        final TaskStatus taskStatus = taskStatusRepository.findAll().get(0);
        final Label label = labelRepository.findAll().get(0);
        final TaskDto testRegTaskDto = new TaskDto(
                "Task name",
                "Task description",
                taskStatus.getId(),
                user.getId(),
                List.of(label.getId())
        );
        return regTask(testRegTaskDto, byUser);
    }

    public ResultActions regTask(final TaskDto dto, final String byUser) throws Exception {
        final var request =
                MockMvcRequestBuilders.post(BASE_URL + TaskController.TASK_CONTROLLER_PATH)
                .content(asJson(dto))
                .contentType(APPLICATION_JSON);

        return perform(request, byUser);
    }

    public ResultActions regDefaultLabel(final String byUser) throws Exception {
        return regLabel(TEST_LABEL_1, byUser);
    }

    public ResultActions regLabel(final LabelDto labelDto, final String byUser) throws Exception {
        final var request =
                MockMvcRequestBuilders.post(BASE_URL + LabelController.LABEL_CONTROLLER_PATH)
                        .content(asJson(labelDto))
                        .contentType(APPLICATION_JSON);

        return perform(request, byUser);
    }

    public ResultActions regDefaultTaskStatus(final String byUser) throws Exception {
        return regTaskStatus(TEST_TASK_STATUS_1, byUser);
    }

    public ResultActions regTaskStatus(final TaskStatusDto taskStatusDto, final String byUser) throws Exception {
        final var request =
                MockMvcRequestBuilders.post(BASE_URL + TaskStatusController.TASK_STATUS_CONTROLLER_PATH)
                        .content(asJson(taskStatusDto))
                        .contentType(APPLICATION_JSON);

        return perform(request, byUser);
    }

    public ResultActions regDefaultUser() throws Exception {
        return regUser(testRegistrationUser);
    }

    public ResultActions regUser(final UserDto userDto) throws Exception {
        final var request =
                MockMvcRequestBuilders.post(BASE_URL + UserController.USER_CONTROLLER_PATH)
                .content(asJson(userDto))
                .contentType(APPLICATION_JSON);

        return perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request);
    }

    public ResultActions perform(final MockHttpServletRequestBuilder request, final String byUser) throws Exception {
        final String token = jwtHelper.expiring(Map.of("username", byUser));
        request.header(AUTHORIZATION, token);

        return perform(request);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public static String asJson(final Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(final String json, final TypeReference<T> to) throws JsonProcessingException {
        return MAPPER.readValue(json, to);
    }

}
