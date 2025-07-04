package ru.practicum.explorewithme.admin.categories.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.categories.NewCategoryDto;
import ru.practicum.explorewithme.exception.NotFoundException;

@Repository
@RequiredArgsConstructor
public class CategoriesRepositoryImpl implements CategoriesRepository {
    private final DSLContext dsl;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        return dsl.insertInto(Categories.CATEGORIES)
                .set(Categories.CATEGORIES.NAME, newCategoryDto.getName())
                .returning()
                .fetchOne()
                .into(CategoryDto.class);
    }

    @Override
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId) {
        return dsl.update(Categories.CATEGORIES)
                .set(Categories.CATEGORIES.NAME, newCategoryDto.getName())
                .where(Categories.CATEGORIES.ID.eq(catId))
                .returning()
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Category with id %s does not exist in " +
                        "the database", catId)))
                .into(CategoryDto.class);
    }

    @Override
    public void deleteCategory(Long catId) {
        dsl.delete(Categories.CATEGORIES)
                .where(Categories.CATEGORIES.ID.eq(catId))
                .execute();
    }
}