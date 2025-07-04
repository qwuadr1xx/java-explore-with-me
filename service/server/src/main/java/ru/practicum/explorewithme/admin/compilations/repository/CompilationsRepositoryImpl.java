package ru.practicum.explorewithme.admin.compilations.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.explorewithme.jooq.tables.CompilationEvents;
import ru.explorewithme.jooq.tables.Compilations;
import ru.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;
import ru.practicum.explorewithme.events.EventShortDto;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompilationsRepositoryImpl implements CompilationsRepository {
    private final DSLContext dsl;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        CompilationDto compilation = dsl.insertInto(Compilations.COMPILATIONS)
                .set(Compilations.COMPILATIONS.PINNED, newCompilationDto.getPinned())
                .set(Compilations.COMPILATIONS.TITLE, newCompilationDto.getTitle())
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

            List<EventShortDto> eventShortDtoList = dsl.selectFrom(Events.EVENTS)
                    .where(Events.EVENTS.ID.in(newCompilationDto.getEvents()))
                    .fetchInto(EventShortDto.class);

            compilation.setEvents(eventShortDtoList);
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
    public CompilationDto updateCompilation(NewCompilationDto newCompilationDto, Long compId) {
        CompilationDto compilation = dsl.update(Compilations.COMPILATIONS)
                .set(Compilations.COMPILATIONS.TITLE, newCompilationDto.getTitle())
                .set(Compilations.COMPILATIONS.PINNED, newCompilationDto.getPinned())
                .where(Compilations.COMPILATIONS.ID.eq(compId))
                .returning()
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %s does not exist in " +
                        "the database", compId)))
                .into(CompilationDto.class);

        dsl.deleteFrom(CompilationEvents.COMPILATION_EVENTS)
                .where(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID.eq(compId))
                .execute();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            dsl.batch(
                    newCompilationDto.getEvents().stream()
                            .map(eventId -> dsl.insertInto(CompilationEvents.COMPILATION_EVENTS)
                                    .set(CompilationEvents.COMPILATION_EVENTS.COMPILATION_ID, compId)
                                    .set(CompilationEvents.COMPILATION_EVENTS.EVENT_ID, eventId))
                            .toList()
            ).execute();

            List<EventShortDto> eventShortDtoList = dsl.selectFrom(Events.EVENTS)
                    .where(Events.EVENTS.ID.in(newCompilationDto.getEvents()))
                    .fetchInto(EventShortDto.class);

            compilation.setEvents(eventShortDtoList);
        }

        return compilation;
    }
}
