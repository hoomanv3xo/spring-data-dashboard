package com.example.demo;

import org.apache.commons.math3.distribution.TDistribution;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class AnalysisController {

    public record Analysis(int count, Double skewness, Double kurtosis,
                           Double ciLow, Double ciHigh) {}
    public record Point(double x, double y) {}
    public record Scatter(List<Point> points, int pairs, Double r, Double rSquared,
                          Double slope, Double intercept) {}
    public record BoxPlot(int count, double min, double q1, double median, double q3,
                          double max, double whiskerLow, double whiskerHigh,
                          List<Double> outliers) {}

    private final SaleRepository repo;

    public AnalysisController(SaleRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/analysis")
    public Analysis analysis() {
        double[] v = amounts();
        int n = v.length;
        Double skew = null, kurt = null, lo = null, hi = null;

        if (n >= 2) {
            double mean = Arrays.stream(v).average().orElse(0);
            double s = sd(v, mean);
            double t = new TDistribution(n - 1).inverseCumulativeProbability(0.975);
            double margin = t * s / Math.sqrt(n);
            lo = mean - margin;
            hi = mean + margin;

            if (n >= 3 && s > 0) {
                double sum3 = 0;
                for (double x : v) sum3 += Math.pow((x - mean) / s, 3);
                skew = n * sum3 / ((n - 1.0) * (n - 2.0));
            }
            if (n >= 4 && s > 0) {
                double sum4 = 0;
                for (double x : v) sum4 += Math.pow((x - mean) / s, 4);
                kurt = n * (n + 1.0) * sum4 / ((n - 1.0) * (n - 2.0) * (n - 3.0))
                        - 3.0 * (n - 1.0) * (n - 1.0) / ((n - 2.0) * (n - 3.0));
            }
        }
        return new Analysis(n, skew, kurt, lo, hi);
    }

    @GetMapping("/api/boxplot")
    public BoxPlot boxplot() {
        double[] v = amounts();
        Arrays.sort(v);
        int n = v.length;
        if (n == 0) return new BoxPlot(0, 0, 0, 0, 0, 0, 0, 0, List.of());

        double q1 = percentile(v, 25);
        double med = percentile(v, 50);
        double q3 = percentile(v, 75);
        double iqr = q3 - q1;
        double loFence = q1 - 1.5 * iqr;
        double hiFence = q3 + 1.5 * iqr;

        double wLow = Double.MAX_VALUE;
        double wHigh = -Double.MAX_VALUE;
        List<Double> outliers = new ArrayList<>();
        for (double x : v) {
            if (x < loFence || x > hiFence) {
                outliers.add(x);
            } else {
                wLow = Math.min(wLow, x);
                wHigh = Math.max(wHigh, x);
            }
        }
        return new BoxPlot(n, v[0], q1, med, q3, v[n - 1], wLow, wHigh, outliers);
    }

    @GetMapping("/api/scatter")
    public Scatter scatter() {
        List<Point> pts = repo.findAll().stream()
                .filter(s -> s.getAmount2() != null)
                .map(s -> new Point(s.getAmount(), s.getAmount2()))
                .toList();
        int n = pts.size();
        Double r = null, r2 = null, slope = null, intercept = null;

        if (n >= 3) {
            double mx = pts.stream().mapToDouble(Point::x).average().orElse(0);
            double my = pts.stream().mapToDouble(Point::y).average().orElse(0);
            double sxx = 0, syy = 0, sxy = 0;
            for (Point p : pts) {
                sxx += (p.x() - mx) * (p.x() - mx);
                syy += (p.y() - my) * (p.y() - my);
                sxy += (p.x() - mx) * (p.y() - my);
            }
            if (sxx > 0) {
                slope = sxy / sxx;
                intercept = my - slope * mx;
            }
            if (sxx > 0 && syy > 0) {
                r = sxy / Math.sqrt(sxx * syy);
                r2 = r * r;
            }
        }
        return new Scatter(pts, n, r, r2, slope, intercept);
    }

    private double[] amounts() {
        return repo.findAll().stream().mapToDouble(Sale::getAmount).toArray();
    }

    private static double sd(double[] v, double mean) {
        if (v.length < 2) return 0;
        double ss = 0;
        for (double x : v) ss += (x - mean) * (x - mean);
        return Math.sqrt(ss / (v.length - 1));
    }

    private static double percentile(double[] sorted, double p) {
        double rank = p / 100.0 * (sorted.length - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        return sorted[lo] + (rank - lo) * (sorted[hi] - sorted[lo]);
    }
}