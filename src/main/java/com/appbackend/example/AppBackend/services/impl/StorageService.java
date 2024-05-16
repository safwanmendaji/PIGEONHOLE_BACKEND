package com.appbackend.example.AppBackend.services.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@Service
@Slf4j
public class StorageService {

    @Value("${aws.bucketName}")
    private String bucketName;

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretKey}")
    private String secretKey;
    @Autowired
    private AmazonS3 s3Client;

//    public String uploadFileToS3(MultipartFile file) {
//        String fileName = file.getOriginalFilename();
//        File fileObj = convertedMultiPartFiletoFile(file);
//
//        try {
//            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
//            fileObj.delete();
//            System.out.println("File uploaded to S3 bucket: " + fileName);
//            return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
//        } catch (AmazonServiceException | SdkClientException e) {
//            System.err.println("Error uploading file: " + e.getMessage());
//            return null; // Return null to indicate failure
//        }
//    }

    public String uploadFileToS3(MultipartFile file) {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
            s3Client.putObject(putObjectRequest);

            String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
            log.info("File uploaded to S3 bucket: {}", fileUrl);
            return fileUrl;
        } catch (AmazonServiceException | IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipart file to file", e);
        }
        return convertedFile;
    }
}