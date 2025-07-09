package ru.practicum.explorewithme.publicapi.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.publicapi.categories.repository.CategoriesRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCategoriesServiceImpl implements CategoriesService {
    private final CategoriesRepository categoriesRepository;

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoriesRepository.getCategories(from, size);
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        return categoriesRepository.getCategory(catId);
    }
}
