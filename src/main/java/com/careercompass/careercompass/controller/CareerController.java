package com.careercompass.careercompass.controller;

import com.careercompass.careercompass.entity.CareerPath;
import com.careercompass.careercompass.repository.CareerPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/careers")
public class CareerController {

    @Autowired
    private CareerPathRepository careerPathRepository;

    @GetMapping("/all")
    public List<CareerPath> getAllCareerPaths() {
        return careerPathRepository.findAll();
    }

    @GetMapping("/{name}")
    public Optional<CareerPath> getCareerPathByName(@PathVariable String name) {
        return careerPathRepository.findByName(name);
    }
}