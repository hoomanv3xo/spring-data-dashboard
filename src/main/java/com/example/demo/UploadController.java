package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UploadController {

    public record UploadResult(int imported) {}

    private final SaleRepository repo;

    public UploadController(SaleRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/api/upload")
    public UploadResult upload(@RequestParam("file") MultipartFile file) throws IOException {
        List<Sale> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNo = 0;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                line = line.replace("\uFEFF", "").trim();
                if (line.isEmpty()) continue;

                boolean isFirst = first;
                first = false;

                String[] parts = line.split(",", -1);
                String label;
                String v1;
                String v2 = null;
                if (parts.length >= 3) {
                    label = parts[0].trim();
                    v1 = parts[1].trim();
                    v2 = parts[2].trim();
                } else if (parts.length == 2) {
                    label = parts[0].trim();
                    v1 = parts[1].trim();
                } else {
                    label = String.valueOf(rows.size() + 1);
                    v1 = parts[0].trim();
                }

                try {
                    double a = Double.parseDouble(v1);
                    Double b = (v2 == null || v2.isEmpty()) ? null : Double.parseDouble(v2);
                    rows.add(new Sale(label, a, b));
                } catch (NumberFormatException e) {
                    if (isFirst) continue;   // header row
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Line " + lineNo + ": expected numbers, got '" + line + "'");
                }
            }
        }
        repo.saveAll(rows);
        return new UploadResult(rows.size());
    }
}