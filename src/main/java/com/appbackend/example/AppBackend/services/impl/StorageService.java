package com.appbackend.example.AppBackend.services.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public String uploadFileToS3(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        File fileObj = convertedMultiPartFiletoFile(file);

        try {
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));

            fileObj.delete();

            System.out.println("File uploaded to S3 bucket: " + fileName);

            return "https://" + bucketName + ".s3.amazonaws.com/" + fileName;
        } catch (AmazonServiceException | SdkClientException e) {
            System.err.println("Error uploading file: " + e.getMessage());
            return null; // Return null to indicate failure
        }
    }




//    public byte[] downloadFile(String fileName) {
//        S3Object s3Object = s3Client.getObject(bucketName, fileName);
//        S3ObjectInputStream inputStream = s3Object.getObjectContent();
//        try {
//            byte[] content = IoUtils.toByteArray(inputStream);
//            return content;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public String deleteFile(String fileName){
//        s3Client.deleteObject(bucketName,fileName);
//        return fileName+"_"+fileName;
//    }

    private File convertedMultiPartFiletoFile(MultipartFile file){
        File convertedFile = new File(file.getOriginalFilename());
        try(FileOutputStream fos = new FileOutputStream(convertedFile)) {
        }
        catch (IOException e){
            log.error("Error converting multipartfile to file",e);
        }
        return convertedFile;
    }
}
