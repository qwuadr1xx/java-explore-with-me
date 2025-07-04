package ru.practicum.explorewithme.admin.users.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.explorewithme.jooq.tables.CompilationEvents;
import ru.explorewithme.jooq.tables.Events;
import ru.explorewithme.jooq.tables.Requests;
import ru.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.users.NewUserRequest;
import ru.practicum.explorewithme.users.UserDto;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UsersRepositoryImpl implements UsersRepository {
    private final DSLContext dsl;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        var query = dsl.selectFrom(Users.USERS);

        if (ids != null && !ids.isEmpty()) {
            return query.where(Users.USERS.ID.in(ids)).offset(from).limit(size).fetch().into(UserDto.class);
        }

        return query.offset(from).limit(size).fetch().into(UserDto.class);
    }

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        return dsl.insertInto(Users.USERS)
                .set(Users.USERS.EMAIL, newUserRequest.getEmail())
                .set(Users.USERS.NAME, newUserRequest.getName())
                .returning()
                .fetchOne()
                .into(UserDto.class);
    }

    @Override
    public void deleteUser(Long userId) {
        dsl.deleteFrom(Requests.REQUESTS)
                .where(Requests.REQUESTS.REQUESTER.eq(userId))
                .execute();

        dsl.deleteFrom(CompilationEvents.COMPILATION_EVENTS)
                .where(CompilationEvents.COMPILATION_EVENTS.EVENT_ID.in(
                        dsl.select(Events.EVENTS.ID)
                                .from(Events.EVENTS)
                                .where(Events.EVENTS.INITIATOR_ID.eq(userId))
                )).execute();

        dsl.deleteFrom(Events.EVENTS)
                .where(Events.EVENTS.INITIATOR_ID.eq(userId))
                .execute();

        dsl.deleteFrom(Users.USERS)
                .where(Users.USERS.ID.eq(userId))
                .execute();

    }
}
