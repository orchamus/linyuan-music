package com.linyuan.music.service.impl;

import com.linyuan.music.constant.MessageConstant;
import com.linyuan.music.service.StorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO 对象存储实现。
 *
 * <p>当 {@code linyuan-music.storage.type=minio} 时启用。
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "linyuan-music.storage.type", havingValue = "minio")
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioStorageServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            log.info("上传文件到 MinIO: {}", fileName);
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + "：" + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String filePath = fileUrl.replace(endpoint + "/" + bucketName + "/", "");
            log.info("删除 MinIO 文件: {}", filePath);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
}
