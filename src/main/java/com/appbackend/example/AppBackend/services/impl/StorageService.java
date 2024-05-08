package com.appbackend.example.AppBackend.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.utils.IoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


@Service
@Slf4j
public class StorageService {

    @Value("${aws.bucketName}")
    private String bucketName;


    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file){
        File fileObj = convertedMultiPartFiletoFile(file);
        String fileName=System.currentTimeMillis()+"_"+file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName,fileName,fileObj));
        fileObj.delete();
        return "File uploaded"+fileName;
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
