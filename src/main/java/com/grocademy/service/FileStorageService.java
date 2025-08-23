package com.grocademy.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String folder) throws IOException;
    void deleteFile(String filepath) throws IOException;
}
