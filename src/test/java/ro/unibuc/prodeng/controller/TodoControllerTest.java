package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.AssignTodoRequest;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.EditTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;
import ro.unibuc.prodeng.service.TodoService;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TodoResponse todo1 = new TodoResponse("todo-1", "Complete project", false, "Alice", "alice@example.com");
    private final TodoResponse todo2 = new TodoResponse("todo-2", "Write documentation", true, "Alice", "alice@example.com");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(todoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetTodosByUserEmail_whenTodosExist_returnsList() throws Exception {
        when(todoService.getTodosByUserEmail("alice@example.com")).thenReturn(List.of(todo1, todo2));

        mockMvc.perform(get("/api/todos").param("assigneeEmail", "alice@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("todo-1")))
                .andExpect(jsonPath("$[1].done", is(true)));
    }

    @Test
    void testGetTodoById_whenTodoExists_returnsTodo() throws Exception {
        when(todoService.getTodoById("todo-1")).thenReturn(todo1);

        mockMvc.perform(get("/api/todos/todo-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Complete project")));
    }

    @Test
    void testCreateTodo_whenPayloadIsValid_returnsCreatedTodo() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest("Complete project", "alice@example.com");
        when(todoService.createTodo(any(CreateTodoRequest.class))).thenReturn(todo1);

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("todo-1")))
                .andExpect(jsonPath("$.assigneeEmail", is("alice@example.com")));
    }

    @Test
    void testCreateTodo_whenPayloadIsInvalid_returnsBadRequest() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest("", "invalid");

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", hasKey("description")))
                .andExpect(jsonPath("$", hasKey("assigneeEmail")));
    }

    @Test
    void testSetDone_whenPayloadIsValid_returnsUpdatedTodo() throws Exception {
        TodoResponse doneTodo = new TodoResponse("todo-1", "Complete project", true, "Alice", "alice@example.com");
        when(todoService.setDone("todo-1", true)).thenReturn(doneTodo);

        mockMvc.perform(patch("/api/todos/todo-1/done")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done", is(true)));
    }

    @Test
    void testAssign_whenPayloadIsValid_returnsUpdatedTodo() throws Exception {
        AssignTodoRequest request = new AssignTodoRequest("bob@example.com");
        TodoResponse reassigned = new TodoResponse("todo-1", "Complete project", false, "Bob", "bob@example.com");
        when(todoService.assign("todo-1", request)).thenReturn(reassigned);

        mockMvc.perform(patch("/api/todos/todo-1/assignee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeName", is("Bob")));
    }

    @Test
    void testEdit_whenPayloadIsValid_returnsUpdatedTodo() throws Exception {
        EditTodoRequest request = new EditTodoRequest("Updated description");
        TodoResponse updated = new TodoResponse("todo-1", "Updated description", false, "Alice", "alice@example.com");
        when(todoService.edit("todo-1", request)).thenReturn(updated);

        mockMvc.perform(patch("/api/todos/todo-1/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    void testDeleteTodo_whenTodoExists_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/todos/todo-1"))
                .andExpect(status().isNoContent());

        verify(todoService, times(1)).deleteTodo("todo-1");
    }

    @Test
    void testGetTodoById_whenTodoMissing_returnsNotFound() throws Exception {
        when(todoService.getTodoById("missing")).thenThrow(new EntityNotFoundException("missing"));

        mockMvc.perform(get("/api/todos/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Entity: missing was not found")));
    }
}
