package ru.practicum.explorewithme.publicapi.categories.repository;

import ru.practicum.explorewithme.categories.CategoryDto;

import java.util.List;

public interface CategoriesRepository {
    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategory(Long catId);
}
