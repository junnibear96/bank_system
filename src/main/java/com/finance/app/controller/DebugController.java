package com.finance.app.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/assets")
    public List<String> listAssets() throws IOException {
        List<String> results = new ArrayList<>();

        // Check standard static locations
        String[] locations = { "static", "static/assets", "public", "resources" };

        for (String loc : locations) {
            Resource resource = new ClassPathResource(loc);
            results.add("Checking: " + loc + " -> Exists? " + resource.exists());

            if (resource.exists()) {
                try {
                    File file = resource.getFile();
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (files != null) {
                            results.add("Contents of " + loc + ":");
                            results.addAll(Arrays.stream(files).map(File::getName).collect(Collectors.toList()));
                        }
                    } else {
                        results.add("Found file: " + file.getName());
                    }
                } catch (Exception e) {
                    results.add("Could not list files for " + loc + " (probably inside JAR): " + e.getMessage());
                }
            }
        }
        return results;
    }
}
