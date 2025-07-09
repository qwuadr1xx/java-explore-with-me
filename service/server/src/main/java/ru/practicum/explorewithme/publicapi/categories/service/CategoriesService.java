package ru.practicum.explorewithme.publicapi.categories.service;

import ru.practicum.explorewithme.categories.CategoryDto;

import java.util.List;

public interface CategoriesService {
    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto getCategory(Long catId);
}
