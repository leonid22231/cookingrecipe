package com.lyadev.cookingrecipes.controller;

import com.lyadev.cookingrecipes.entity.CommentEntity;
import com.lyadev.cookingrecipes.storage.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/files")
@EnableWebSecurity
@AllArgsConstructor
public class FilesController {
    StorageService service;
    @RequestMapping(value = "/Add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity uploadFile(@RequestParam MultipartFile file) {
        service.store(file);
        return ResponseEntity.ok("Ok " + file.getOriginalFilename());
    }
    @RequestMapping(value = "get/{filename:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = service.loadAsResource(filename);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(file);
    }

    }

