package com.linyuan.music.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务抽象。
 *
 * <p>由 {@code linyuan-music.storage.type} 决定具体实现：
 * <ul>
 *     <li>{@code local}：本地磁盘存储（默认，开箱即用）</li>
 *     <li>{@code minio}：MinIO 对象存储</li>
 * </ul>
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param file   要上传的文件
     * @param folder 存储目录
     * @return 可访问的文件 URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 删除文件
     *
     * @param fileUrl 文件 URL
     */
    void deleteFile(String fileUrl);

}
