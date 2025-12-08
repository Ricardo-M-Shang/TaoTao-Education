package com.taotao.education.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.user.dto.LoginDTO;
import com.taotao.education.user.dto.RegisterDTO;
import com.taotao.education.user.dto.UserUpdateDTO;
import com.taotao.education.user.entity.User;
import com.taotao.education.user.vo.LoginVO;
import com.taotao.education.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 退出登录
     */
    void logout(Long userId);
}

