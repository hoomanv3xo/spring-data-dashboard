package com.example.demo;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExportController {

    private final SaleRepository repo;

    public ExportController(SaleRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/api/export")
    public ResponseEntity<String> export() {
        StringBuilder sb = new StringBuilder("label,amount,amount2\n");
        for (Sale s : repo.findAll(Sort.by("id"))) {
            String label = s.getLabel() == null ? "" : s.getLabel().replace(",", " ");
            sb.append(label).append(',')
                    .append(s.getAmount()).append(',')
                    .append(s.getAmount2() == null ? "" : s.getAmount2())
                    .append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"data.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(sb.toString());
    }
}