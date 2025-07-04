package ru.practicum.explorewithme.admin.categories.repository;

import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.categories.NewCategoryDto;

public interface CategoriesRepository {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId);

    void deleteCategory(Long catId);
}
