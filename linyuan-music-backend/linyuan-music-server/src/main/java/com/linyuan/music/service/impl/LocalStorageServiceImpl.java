package com.linyuan.music.service.impl;

import com.linyuan.music.constant.MessageConstant;
import com.linyuan.music.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现（默认）。
 *
 * <p>文件写入 {@code linyuan-music.storage.local-dir}，通过
 * {@code /files/**} 静态资源映射对外访问，不依赖 MinIO 即可完整演示上传功能。
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "linyuan-music.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageServiceImpl implements StorageService {

    @Value("${linyuan-music.storage.local-dir}")
    private String localDir;

    @Value("${linyuan-music.storage.public-base-url}")
    private String publicBaseUrl;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            // 去掉非法字符，避免路径穿越
            String safeName = originalName.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
            String fileName = UUID.randomUUID() + "-" + safeName;

            Path folderPath = Paths.get(localDir, folder).toAbsolutePath().normalize();
            Files.createDirectories(folderPath);
            Path target = folderPath.resolve(fileName).normalize();
            if (!target.startsWith(folderPath)) {
                throw new IllegalArgumentException("非法的文件路径");
            }

            file.transferTo(target.toFile());
            log.info("上传文件到本地: {}", target);

            return trimTrailingSlash(publicBaseUrl) + "/" + folder + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + "：" + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        String prefix = trimTrailingSlash(publicBaseUrl) + "/";
        // 非本服务存储的地址（例如外部图片）不处理
        if (!fileUrl.startsWith(prefix)) {
            return;
        }
        try {
            String relative = fileUrl.substring(prefix.length());
            Path basePath = Paths.get(localDir).toAbsolutePath().normalize();
            Path target = basePath.resolve(relative).normalize();
            if (!target.startsWith(basePath)) {
                return;
            }
            boolean deleted = Files.deleteIfExists(target);
            log.info("删除本地文件: {} ({})", target, deleted ? "已删除" : "不存在");
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    private String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
