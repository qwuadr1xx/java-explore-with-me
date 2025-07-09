package ru.practicum.explorewithme.publicapi.categories.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.publicapi.categories.service.CategoriesService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController("publicCategoriesController")
@RequestMapping("/categories")
public class CategoriesController {
    private final CategoriesService categoriesService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
                                           @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /categories - Получение категорий: from={}, size={}", from, size);
        return categoriesService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        log.info("GET /categories/{} - Получение категории по ID", catId);
        return categoriesService.getCategory(catId);
    }
}
