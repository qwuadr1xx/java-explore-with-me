package ru.practicum.explorewithme.publicapi.categories.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PublicCategoriesRepositoryImpl implements CategoriesRepository {
    private final DSLContext dsl;

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return dsl.selectFrom(Categories.CATEGORIES)
                .offset(from)
                .limit(size)
                .fetchInto(CategoryDto.class);
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        return dsl.selectFrom(Categories.CATEGORIES)
                .where(Categories.CATEGORIES.ID.eq(catId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %s does not exist in " +
                        "the database", catId)))
                .into(CategoryDto.class);
    }
}
