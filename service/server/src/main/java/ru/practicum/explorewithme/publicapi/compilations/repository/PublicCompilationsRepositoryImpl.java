package ru.practicum.explorewithme.publicapi.compilations.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.explorewithme.jooq.tables.Compilations;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.util.List;

import static org.jooq.impl.DSL.trueCondition;

@Repository
@RequiredArgsConstructor
public class PublicCompilationsRepositoryImpl implements CompilationsRepository {
    private final DSLContext dsl;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        var query = dsl.selectFrom(Compilations.COMPILATIONS)
                .where(trueCondition());

        if (pinned != null) {
            query = query.and(Compilations.COMPILATIONS.PINNED.eq(pinned));
        }

        return query.offset(from)
                .limit(size)
                .fetchInto(CompilationDto.class);
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return dsl.selectFrom(Compilations.COMPILATIONS)
                .where(Compilations.COMPILATIONS.ID.eq(compId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id %s does not exist in " +
                        "the database", compId)))
                .into(CompilationDto.class);
    }
}
