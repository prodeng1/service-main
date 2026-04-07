package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.TodoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.TodoRepository;
import ro.unibuc.prodeng.request.AssignTodoRequest;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.EditTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TodoService todoService;

    @Test
    void testGetTodosByUserEmail_whenTodosExist_returnsMappedTodos() throws EntityNotFoundException {
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        List<TodoEntity> todos = List.of(
                new TodoEntity("todo-1", "First task", false, "1"),
                new TodoEntity("todo-2", "Second task", true, "1"));
        when(userService.getUserEntityByEmail("alice@example.com")).thenReturn(user);
        when(todoRepository.findByAssignedUserId("1")).thenReturn(todos);

        List<TodoResponse> result = todoService.getTodosByUserEmail("alice@example.com");

        assertEquals(2, result.size());
        assertEquals("First task", result.get(0).description());
        assertEquals("alice@example.com", result.get(1).assigneeEmail());
    }

    @Test
    void testGetTodoById_whenTodoExists_returnsMappedTodo() throws EntityNotFoundException {
        TodoEntity todo = new TodoEntity("todo-1", "Complete project", false, "1");
        UserEntity assignee = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(todo));
        when(userService.getUserEntityById("1")).thenReturn(assignee);

        TodoResponse result = todoService.getTodoById("todo-1");

        assertEquals("todo-1", result.id());
        assertFalse(result.done());
        assertEquals("Alice", result.assigneeName());
    }

    @Test
    void testGetTodoById_whenTodoMissing_throwsEntityNotFoundException() {
        when(todoRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> todoService.getTodoById("missing"));
    }

    @Test
    void testCreateTodo_whenRequestIsValid_savesAndReturnsTodo() throws EntityNotFoundException {
        UserEntity assignee = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        CreateTodoRequest request = new CreateTodoRequest("Finish unit tests", "alice@example.com");
        when(userService.getUserEntityByEmail("alice@example.com")).thenReturn(assignee);
        when(todoRepository.save(any(TodoEntity.class)))
                .thenAnswer(invocation -> {
                    TodoEntity todo = invocation.getArgument(0);
                    return new TodoEntity("generated-id", todo.description(), todo.done(), todo.assignedUserId());
                });

        TodoResponse result = todoService.createTodo(request);

        assertNotNull(result.id());
        assertEquals("Finish unit tests", result.description());
        assertFalse(result.done());
        assertEquals("alice@example.com", result.assigneeEmail());
    }

    @Test
    void testSetDone_whenTodoExists_updatesDoneFlag() throws EntityNotFoundException {
        TodoEntity existing = new TodoEntity("todo-1", "Complete project", false, "1");
        UserEntity assignee = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(existing));
        when(todoRepository.save(any(TodoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserEntityById("1")).thenReturn(assignee);

        TodoResponse result = todoService.setDone("todo-1", true);

        assertEquals("todo-1", result.id());
        assertEquals(true, result.done());
    }

    @Test
    void testAssign_whenTodoExists_updatesAssignee() throws EntityNotFoundException {
        TodoEntity existing = new TodoEntity("todo-1", "Complete project", false, "1");
        UserEntity newAssignee = new UserEntity("2", "Bob", "bob@example.com", "+40987654321");
        AssignTodoRequest request = new AssignTodoRequest("bob@example.com");
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(existing));
        when(userService.getUserEntityByEmail("bob@example.com")).thenReturn(newAssignee);
        when(todoRepository.save(any(TodoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TodoResponse result = todoService.assign("todo-1", request);

        assertEquals("Bob", result.assigneeName());
        assertEquals("bob@example.com", result.assigneeEmail());
    }

    @Test
    void testEdit_whenTodoExists_updatesDescription() throws EntityNotFoundException {
        TodoEntity existing = new TodoEntity("todo-1", "Old description", false, "1");
        UserEntity assignee = new UserEntity("1", "Alice", "alice@example.com", "+40123456789");
        EditTodoRequest request = new EditTodoRequest("New description");
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(existing));
        when(todoRepository.save(any(TodoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserEntityById("1")).thenReturn(assignee);

        TodoResponse result = todoService.edit("todo-1", request);

        assertEquals("New description", result.description());
        assertEquals("Alice", result.assigneeName());
    }

    @Test
    void testDeleteTodo_whenTodoExists_deletesTodo() throws EntityNotFoundException {
        when(todoRepository.existsById("todo-1")).thenReturn(true);

        todoService.deleteTodo("todo-1");

        verify(todoRepository, times(1)).deleteById("todo-1");
    }

    @Test
    void testDeleteTodo_whenTodoMissing_throwsEntityNotFoundException() {
        when(todoRepository.existsById("missing")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> todoService.deleteTodo("missing"));
        verify(todoRepository, never()).deleteById(any());
    }
}
