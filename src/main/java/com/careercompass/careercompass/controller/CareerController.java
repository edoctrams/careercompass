package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.entity.CareerPath;
import com.careercompass.careercompass.repository.CareerPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/careers")
public class CareerController {

    private static final Set<Long> MVP_CAREER_IDS = Set.of(1L, 3L, 4L, 6L);

    @Autowired
    private CareerPathRepository careerPathRepository;

    @GetMapping("/all")
    public List<CareerPath> getAllCareerPaths(
            @RequestParam(required = false, defaultValue = "true") boolean mvpOnly) {
        List<CareerPath> all = careerPathRepository.findAll();
        if (!mvpOnly) {
            return all;
        }
        return all.stream()
                .filter(cp -> MVP_CAREER_IDS.contains(cp.getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}")
    public Optional<CareerPath> getCareerPathByName(@PathVariable String name) {
        return careerPathRepository.findByName(name);
    }
}