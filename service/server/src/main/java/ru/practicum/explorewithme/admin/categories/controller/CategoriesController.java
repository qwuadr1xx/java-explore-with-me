package ru.practicum.explorewithme.admin.categories.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.admin.categories.service.CategoriesService;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.categories.NewCategoryDto;

@Slf4j
@RequiredArgsConstructor
@RestController("adminCategoriesController")
@RequestMapping("/admin/categories")
public class CategoriesController {
    private final CategoriesService categoriesService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("POST /admin/categories - добавление категории {}", newCategoryDto);
        return categoriesService.addCategory(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{} - удаление категории", catId);
        categoriesService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@Valid @RequestBody NewCategoryDto newCategoryDto, @PathVariable Long catId) {
        log.info("PATCH /admin/categories/{} - обновление категории {}", catId, newCategoryDto);
        return categoriesService.updateCategory(newCategoryDto, catId);
    }
}
