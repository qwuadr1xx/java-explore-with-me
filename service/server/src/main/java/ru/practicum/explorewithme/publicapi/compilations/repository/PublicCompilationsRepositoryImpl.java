package ru.practicum.explorewithme.publicapi.compilations.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.*;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.utils.RecordToShortEventMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jooq.impl.DSL.trueCondition;

@Repository
@RequiredArgsConstructor
public class PublicCompilationsRepositoryImpl implements CompilationsRepository {
    private final DSLContext dsl;
    private static final Set<Field<?>> SELECT_FIELDS = Set.of(
            Events.EVENTS.ID,
            Events.EVENTS.ANNOTATION,
            Categories.CATEGORIES.ID,
            Categories.CATEGORIES.NAME,
            Events.EVENTS.CONFIRMED_REQUESTS,
            Events.EVENTS.EVENT_DATE,
            Users.USERS.ID,
            Users.USERS.NAME,
            Events.EVENTS.PAID,
            Events.EVENTS.TITLE,
            Events.EVENTS.VIEWS
    );

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        var query = dsl.selectFrom(Compilations.COMPILATIONS)
                .where(trueCondition());

        if (pinned != null) {
            query = query.and(Compilations.COMPILATIONS.PINNED.eq(pinned));
        }

        List<CompilationDto> compilationDtoListWithoutEvent = query.offset(from)
                .limit(size)
                .fetchInto(CompilationDto.class);

        List<CompilationDto> compilationDtoList = new ArrayList<>();


        compilationDtoListWithoutEvent.forEach(compilationDto -> {
            compilationDto.setEvents(dsl.select(SELECT_FIELDS)
                    .from(Events.EVENTS)
                    .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                    .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                    .where(Events.EVENTS.ID.in(
                            dsl.select(CompilationEvents.COMPILATION_EVENTS.EVENT_ID)
                                    .from(CompilationEvents.COMPILATION_EVENTS)
                                    .where(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID
                                                    .in(compilationDtoList.stream().map(CompilationDto::getId).toList()))))
                    .fetch()
                    .stream().map(RecordToShortEventMapper::map).toList());

            compilationDtoList.add(compilationDto);
        });

        return compilationDtoList;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        CompilationDto compilationDto =  dsl.selectFrom(Compilations.COMPILATIONS)
                .where(Compilations.COMPILATIONS.ID.eq(compId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id %s does not exist in " +
                        "the database", compId)))
                .into(CompilationDto.class);

        compilationDto.setEvents(dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.ID.in(
                        dsl.select(CompilationEvents.COMPILATION_EVENTS.EVENT_ID)
                                .from(CompilationEvents.COMPILATION_EVENTS)
                                .where(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID.eq(compId))))
                .fetch()
                .stream().map(RecordToShortEventMapper::map).toList());

        return compilationDto;
    }
}
