package ru.practicum.explorewithme.admin.users.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.admin.users.service.UsersService;
import ru.practicum.explorewithme.users.NewUserRequest;
import ru.practicum.explorewithme.users.UserDto;
import ru.practicum.explorewithme.users.UserShortDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController("adminUsersController")
@RequestMapping("/admin/users")
public class UsersController {
    UsersService usersService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(required = false, defaultValue = "0") Integer from,
                                  @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /admin/users - получение пользователей с параметрами ids: {}, from: {}, size: {}", ids, from, size);
        return usersService.getUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("POST /admin/users - добавление пользователя {}", newUserRequest);
        return usersService.addUser(newUserRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{} - удаление пользователя", userId);
        usersService.deleteUser(userId);
    }
}