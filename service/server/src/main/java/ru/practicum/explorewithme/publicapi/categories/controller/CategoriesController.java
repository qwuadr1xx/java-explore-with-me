package ru.practicum.explorewithme.publicapi.categories.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.categories.CategoryDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController("publicCategoriesController")
@RequestMapping("/categories")
public class CategoriesController {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategories(@RequestParam(required = false) Integer from,
                                           @RequestParam(required = false) Integer size) {
        return null;
    }

    @GetMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable Long catId) {
        return null;
    }
}
