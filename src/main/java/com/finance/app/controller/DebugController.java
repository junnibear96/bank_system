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
        org.springframework.core.io.support.PathMatchingResourcePatternResolver resolver = new org.springframework.core.io.support.PathMatchingResourcePatternResolver();

        try {
            Resource[] resources = resolver.getResources("classpath*:static/assets/*");
            for (Resource r : resources) {
                results.add("Found asset: " + r.getFilename() + " (URI: " + r.getURI() + ")");
            }

            Resource[] rootResources = resolver.getResources("classpath*:static/*");
            for (Resource r : rootResources) {
                results.add("Found root static: " + r.getFilename());
            }
        } catch (Exception e) {
            results.add("Error listing resources: " + e.getMessage());
        }

        return results;
    }
}
