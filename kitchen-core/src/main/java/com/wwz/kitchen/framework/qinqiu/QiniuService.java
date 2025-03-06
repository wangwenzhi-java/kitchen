package com.wwz.kitchen.framework.qinqiu;

import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wwz.kitchen.framework.config.QiniuConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by wenzhi.wang.
 * on 2024/11/16.
 */
@Service
public class QiniuService {

    @Autowired
    private QiniuConfig qiniuConfig;

    // 获取上传凭证
    private String getUploadToken() {
        // 使用配置中的 accessKey 和 secretKey
        Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());

        // 设置上传的策略，允许上传的文件类型、大小等
        StringMap policy = new StringMap();
        policy.put("mimeLimit", "image/*");  // 限制上传图片
        policy.put("fsizeLimit", 10 * 1024 * 1024);  // 限制文件大小为 10MB

        // 生成上传凭证
        return auth.uploadToken(qiniuConfig.getBucketName(), null, 3600, policy);
    }

    /**
     * 上传头像
     * @param file
     * @param request
     * @return
     * @throws QiniuException
     * @throws IOException
     */
    public String uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws QiniuException,IOException{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 生成上传凭证
        String uploadToken =getUploadToken();
        Configuration cfg = new Configuration(Zone.autoZone());  //Zone.autoZone() 会根据七牛云的最佳选择自动选择区域配置
        UploadManager uploadManager = new UploadManager(cfg);
        // 获取文件字节数组
        byte[] fileBytes = file.getBytes();
        String key = "oldCatKitchen/" + username +"/"+ file.getOriginalFilename();  // 设置上传后的文件名
        // 上传文件到七牛云
        Response response = uploadManager.put(fileBytes, key, uploadToken);
        // 获取上传结果
        if (response.isOK()) {
            return qiniuConfig.getBucketDomain() + key;  // 返回文件的 URL
        } else {
            return "";
        }
    }

    /**
     * 上传菜单
     * @param file
     * @param request
     * @return
     * @throws QiniuException
     * @throws IOException
     */
    public String uploadMenu(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws QiniuException,IOException{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 生成上传凭证
        String uploadToken =getUploadToken();
        Configuration cfg = new Configuration(Zone.autoZone());  //Zone.autoZone() 会根据七牛云的最佳选择自动选择区域配置
        UploadManager uploadManager = new UploadManager(cfg);
        // 获取文件字节数组
        byte[] fileBytes = file.getBytes();
        String key = "oldCatKitchen/" + username +"/menu/"+ file.getOriginalFilename();  // 设置上传后的文件名
        // 上传文件到七牛云
        Response response = uploadManager.put(fileBytes, key, uploadToken);
        // 获取上传结果
        if (response.isOK()) {
            return qiniuConfig.getBucketDomain() + key;  // 返回文件的 URL
        } else {
            return "";
        }
    }

    /**
     * 上传聊天图片
     * @param file
     * @param request
     * @return
     * @throws QiniuException
     * @throws IOException
     */
    public String uploadChatImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws QiniuException,IOException{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 生成上传凭证
        String uploadToken =getUploadToken();
        Configuration cfg = new Configuration(Zone.autoZone());  //Zone.autoZone() 会根据七牛云的最佳选择自动选择区域配置
        UploadManager uploadManager = new UploadManager(cfg);
        // 获取文件字节数组
        byte[] fileBytes = file.getBytes();
        String key = "oldCatKitchen/" + username +"/chatImages/"+ file.getOriginalFilename();  // 设置上传后的文件名
        // 上传文件到七牛云
        Response response = uploadManager.put(fileBytes, key, uploadToken);
        // 获取上传结果
        if (response.isOK()) {
            return qiniuConfig.getBucketDomain() + key;  // 返回文件的 URL
        } else {
            return "";
        }
    }


    /**
     * 上传发帖图片
     * @param file
     * @param request
     * @return
     * @throws QiniuException
     * @throws IOException
     */
    public String uploadTopicImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws QiniuException,IOException{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 生成上传凭证
        String uploadToken =getUploadToken();
        Configuration cfg = new Configuration(Zone.autoZone());  //Zone.autoZone() 会根据七牛云的最佳选择自动选择区域配置
        UploadManager uploadManager = new UploadManager(cfg);
        // 获取文件字节数组
        byte[] fileBytes = file.getBytes();
        String key = "oldCatKitchen/" + username +"/topicImages/"+ file.getOriginalFilename();  // 设置上传后的文件名
        // 上传文件到七牛云
        Response response = uploadManager.put(fileBytes, key, uploadToken);
        // 获取上传结果
        if (response.isOK()) {
            return qiniuConfig.getBucketDomain() + key;  // 返回文件的 URL
        } else {
            return "";
        }
    }
}
