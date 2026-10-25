package com.linyuan.music.service;

import com.linyuan.music.model.dto.AdminDTO;
import com.linyuan.music.model.entity.Admin;
import com.linyuan.music.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
public interface IAdminService extends IService<Admin> {

    // 管理员注册
    Result register(AdminDTO adminDTO);

    // 管理员登录
    Result login(AdminDTO adminDTO);

    // 退出登录
    Result logout(String token);
}
