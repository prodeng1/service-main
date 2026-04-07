package ro.unibuc.prodeng.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.service.TodoService;
import ro.unibuc.prodeng.service.UserService;

@Component
@ConditionalOnProperty(name = "app.sample-data.enabled", havingValue = "true", matchIfMissing = true)
public class SampleDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TodoService todoService;

    public SampleDataLoader(UserRepository userRepository, UserService userService, TodoService todoService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.todoService = todoService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        List<CreateUserRequest> customers = List.of(
                new CreateUserRequest("Ana Popescu", "ana.popescu@example.com", "+40712345678"),
                new CreateUserRequest("Mihai Ionescu", "mihai.ionescu@example.com", "+40711122334"),
                new CreateUserRequest("Elena Marin", "elena.marin@example.com", "+40744455667"),
                new CreateUserRequest("Radu Georgescu", "radu.georgescu@example.com", "+40788899001"));

        customers.forEach(userService::createUser);

        List<CreateTodoRequest> todos = List.of(
                new CreateTodoRequest("Call Ana for order confirmation", "ana.popescu@example.com"),
                new CreateTodoRequest("Update Mihai loyalty status", "mihai.ionescu@example.com"),
                new CreateTodoRequest("Send invoice to Elena", "elena.marin@example.com"),
                new CreateTodoRequest("Validate Radu shipping details", "radu.georgescu@example.com"));

        todos.forEach(todoService::createTodo);
    }
}
