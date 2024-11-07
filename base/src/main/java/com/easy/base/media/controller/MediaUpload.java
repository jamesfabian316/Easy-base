package com.easy.base.media.controller;

import com.easy.base.service.MediaFileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class MediaUpload {
    private final MediaFileService mediaFileService;

    public MediaUpload(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    @PostMapping("/upload")
    public ModelAndView fileUpload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return new ModelAndView("status", "message", "Please select a file and upload again");
        }
        try {
            InputStream is = file.getInputStream();
            mediaFileService.addFile(file.getOriginalFilename(), file.getContentType(),is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ModelAndView("status", "message", "Your File is Uploaded successfully");
    }

    @GetMapping("/media/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("upload").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
