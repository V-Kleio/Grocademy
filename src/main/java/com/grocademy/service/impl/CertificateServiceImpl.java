package com.grocademy.service.impl;

import com.grocademy.entity.Course;
import com.grocademy.entity.User;
import com.grocademy.service.CertificateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class CertificateServiceImpl implements CertificateService {
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String generateCertificate(User user, Course course, LocalDateTime completionDate) {
        try {
            String certificateHtml = generateCertificateHtml(user, course, completionDate);

            String fileName = "certificate_" + user.getId() + "_" + course.getId() + "_" + UUID.randomUUID() + ".html";
            Path certificatePath = Paths.get(uploadDir, "certificates");

            if (!Files.exists(certificatePath)) {
                Files.createDirectories(certificatePath);
            }

            Path filePath = certificatePath.resolve(fileName);
            Files.write(filePath, certificateHtml.getBytes());

            return "/certificates/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate certificate", e);
        }
    }

    private String generateCertificateHtml(User user, Course course, LocalDateTime completionDate) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Certificate of Completion</title>
                <style>
                    body { 
                        font-family: 'Georgia', serif; 
                        text-align: center; 
                        margin: 0; 
                        padding: 50px;
                        background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%);
                    }
                    .certificate { 
                        border: 8px solid #2c3e50; 
                        padding: 60px; 
                        background: white;
                        box-shadow: 0 0 30px rgba(0,0,0,0.1);
                        max-width: 800px;
                        margin: 0 auto;
                    }
                    h1 { 
                        color: #2c3e50; 
                        font-size: 48px;
                        margin-bottom: 20px;
                        border-bottom: 3px solid #3498db;
                        padding-bottom: 10px;
                    }
                    h2 { 
                        color: #34495e; 
                        font-size: 24px;
                        margin: 15px 0;
                    }
                    .recipient { 
                        color: #e74c3c; 
                        font-size: 36px;
                        font-weight: bold;
                        margin: 20px 0;
                    }
                    .course-title { 
                        color: #27ae60; 
                        font-size: 28px;
                        font-style: italic;
                        margin: 20px 0;
                    }
                    .date { 
                        color: #7f8c8d;
                        font-size: 18px;
                        margin-top: 30px;
                    }
                    .logo {
                        font-size: 24px;
                        color: #3498db;
                        font-weight: bold;
                        margin-bottom: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="certificate">
                    <div class="logo">🎓 GROCADEMY</div>
                    <h1>Certificate of Completion</h1>
                    <h2>This certifies that</h2>
                    <div class="recipient">%s</div>
                    <h2>has successfully completed the course</h2>
                    <div class="course-title">"%s"</div>
                    <h2>Instructor: %s</h2>
                    <div class="date">Completed on: %s</div>
                    <div class="date">Certificate ID: GROC-%d-%d-%s</div>
                </div>
            </body>
            </html>
            """, 
            user.getUsername(),
            course.getTitle(),
            course.getInstructor(),
            completionDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            user.getId(),
            course.getId(),
            UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
    }
}
