package com.grocademy.service;

import com.grocademy.entity.Course;
import com.grocademy.entity.User;

public interface CertificateService {
    String generateCertificate(User user, Course course);
}
