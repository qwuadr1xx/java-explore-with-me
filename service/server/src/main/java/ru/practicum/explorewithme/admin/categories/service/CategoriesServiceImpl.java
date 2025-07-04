package ru.practicum.explorewithme.admin.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.admin.categories.repository.CategoriesRepository;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.categories.NewCategoryDto;

@Service
@RequiredArgsConstructor
public class CategoriesServiceImpl implements CategoriesService {
    CategoriesRepository categoriesRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        return categoriesRepository.createCategory(newCategoryDto);
    }

    @Override
    public void deleteCategory(Long catId) {
        categoriesRepository.deleteCategory(catId);
    }

    @Override
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId) {
        return categoriesRepository.updateCategory(newCategoryDto, catId);
    }
}
