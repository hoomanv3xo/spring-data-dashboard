package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class HistogramController {

    public record Histogram(List<String> labels, List<Integer> counts) {}

    private final SaleRepository repo;

    public HistogramController(SaleRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/histogram")
    public Histogram histogram(@RequestParam(defaultValue = "0") int bins) {
        List<Double> v = repo.findAll().stream().map(Sale::getAmount).toList();
        int n = v.size();
        if (n == 0) return new Histogram(List.of(), List.of());

        if (bins <= 0) {
            bins = (int) Math.ceil(Math.log(n) / Math.log(2)) + 1;
        }
        double min = Collections.min(v);
        double max = Collections.max(v);
        double width = (max - min) / bins;
        if (width == 0) {
            return new Histogram(List.of(String.valueOf(min)), List.of(n));
        }

        int[] counts = new int[bins];
        for (double x : v) {
            int i = (int) ((x - min) / width);
            if (i == bins) i = bins - 1;
            counts[i]++;
        }

        List<String> labels = new ArrayList<>();
        List<Integer> countList = new ArrayList<>();
        for (int i = 0; i < bins; i++) {
            labels.add(String.format("%.1f to %.1f", min + i * width, min + (i + 1) * width));
            countList.add(counts[i]);
        }
        return new Histogram(labels, countList);
    }
}
