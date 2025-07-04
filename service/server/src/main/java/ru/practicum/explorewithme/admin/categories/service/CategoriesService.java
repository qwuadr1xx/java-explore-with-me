package ru.practicum.explorewithme.admin.categories.service;

import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.categories.NewCategoryDto;

public interface CategoriesService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId);
}
