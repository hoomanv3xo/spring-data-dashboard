package com.example.demo;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChartController {

    public record ChartData(List<String> labels, List<Double> values) {}
    public record NewSale(String label, double amount, Double amount2) {}
    public record Stats(int count, double mean, double median, double min,
                        double max, double stdDev, double q1, double q3) {}

    private final SaleRepository repo;

    public ChartController(SaleRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/sales")
    public ChartData sales() {
        List<Sale> sales = repo.findAll(Sort.by("id"));
        return new ChartData(
                sales.stream().map(Sale::getLabel).toList(),
                sales.stream().map(Sale::getAmount).toList()
        );
    }

    @PostMapping("/api/sales")
    public Sale add(@RequestBody NewSale body) {
        return repo.save(new Sale(body.label(), body.amount(), body.amount2()));
    }

    @DeleteMapping("/api/sales")
    public void clear() {
        repo.deleteAll();
    }

    @GetMapping("/api/stats")
    public Stats stats() {
        List<Double> v = repo.findAll().stream()
                .map(Sale::getAmount).sorted().toList();
        int n = v.size();
        if (n == 0) return new Stats(0, 0, 0, 0, 0, 0, 0, 0);

        double mean = v.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = n > 1
                ? v.stream().mapToDouble(x -> (x - mean) * (x - mean)).sum() / (n - 1)
                : 0;

        return new Stats(n, mean, percentile(v, 50), v.get(0), v.get(n - 1),
                Math.sqrt(variance), percentile(v, 25), percentile(v, 75));
    }

    private double percentile(List<Double> sorted, double p) {
        double rank = p / 100.0 * (sorted.size() - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        return sorted.get(lo) + (rank - lo) * (sorted.get(hi) - sorted.get(lo));
    }
}
