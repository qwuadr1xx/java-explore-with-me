package ru.practicum.explorewithme.admin.compilations.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;
import ru.practicum.explorewithme.complitations.UpdateCompilationRequest;
import ru.practicum.explorewithme.events.EventShortDto;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.tables.*;
import ru.practicum.explorewithme.utils.RecordToShortEventMapper;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class CompilationsRepositoryImpl implements CompilationsRepository {
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
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Map<Field<?>, Object> fields = new HashMap<>();
        Optional.ofNullable(newCompilationDto.getTitle()).ifPresent(v ->
                fields.put(Compilations.COMPILATIONS.TITLE, v));
        Optional.ofNullable(newCompilationDto.getPinned()).ifPresent(v ->
                fields.put(Compilations.COMPILATIONS.PINNED, v));

        CompilationDto compilation = dsl.insertInto(Compilations.COMPILATIONS)
                .set(fields)
                .returning()
                .fetchOne()
                .into(CompilationDto.class);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            dsl.batch(
                    newCompilationDto.getEvents().stream()
                            .map(eventId -> dsl.insertInto(CompilationEvents.COMPILATION_EVENTS)
                                    .set(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID, compilation.getId())
                                    .set(CompilationEvents.COMPILATION_EVENTS.EVENT_ID, eventId))
                            .toList()
            ).execute();

            List<EventShortDto> eventShortDtoList = dsl.select(SELECT_FIELDS)
                    .from(Events.EVENTS)
                    .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                    .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                    .where(Events.EVENTS.ID.in(newCompilationDto.getEvents()))
                    .stream().map(RecordToShortEventMapper::map).toList();

            compilation.setEvents(eventShortDtoList);
        } else {
            compilation.setEvents(List.of());
        }

        return compilation;
    }

    @Override
    public void deleteCompilation(Long compId) {
        dsl.deleteFrom(CompilationEvents.COMPILATION_EVENTS)
                .where(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID.eq(compId))
                .execute();

        dsl.deleteFrom(Compilations.COMPILATIONS)
                .where(Compilations.COMPILATIONS.ID.eq(compId))
                .execute();
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        Map<Field<?>, Object> updates = new HashMap<>();
        Optional.ofNullable(updateCompilationRequest.getTitle()).ifPresent(v ->
                updates.put(Compilations.COMPILATIONS.TITLE, v));
        Optional.ofNullable(updateCompilationRequest.getPinned()).ifPresent(v ->
                updates.put(Compilations.COMPILATIONS.PINNED, v));

        CompilationDto compilation = dsl.update(Compilations.COMPILATIONS)
                .set(updates)
                .where(Compilations.COMPILATIONS.ID.eq(compId))
                .returning()
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %s does not exist in " +
                        "the database", compId)))
                .into(CompilationDto.class);

        dsl.deleteFrom(CompilationEvents.COMPILATION_EVENTS)
                .where(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID.eq(compId))
                .execute();

        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            dsl.batch(
                    updateCompilationRequest.getEvents().stream()
                            .map(eventId -> dsl.insertInto(CompilationEvents.COMPILATION_EVENTS)
                                    .set(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID, compId)
                                    .set(CompilationEvents.COMPILATION_EVENTS.EVENT_ID, eventId))
                            .toList()
            ).execute();

            List<EventShortDto> eventShortDtoList = dsl.select(SELECT_FIELDS)
                    .from(Events.EVENTS)
                    .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                    .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                    .where(Events.EVENTS.ID.in(updateCompilationRequest.getEvents()))
                    .stream().map(RecordToShortEventMapper::map).toList();

            compilation.setEvents(eventShortDtoList);
        } else {
            compilation.setEvents(List.of());
        }

        return compilation;
    }
}
