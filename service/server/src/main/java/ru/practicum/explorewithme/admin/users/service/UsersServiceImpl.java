package ru.practicum.explorewithme.admin.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.admin.users.repository.UsersRepository;
import ru.practicum.explorewithme.users.NewUserRequest;
import ru.practicum.explorewithme.users.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {
    UsersRepository usersRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        return usersRepository.getUsers(ids, from, size);
    }

    @Override
    public UserDto addUser(NewUserRequest newUserRequest) {
        return usersRepository.createUser(newUserRequest);
    }

    @Override
    public void deleteUser(Long userId) {
        usersRepository.deleteUser(userId);
    }
}
