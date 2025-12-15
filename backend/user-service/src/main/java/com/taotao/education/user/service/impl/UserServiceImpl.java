package com.taotao.education.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.common.constants.RedisConstants;
import com.taotao.education.common.exception.BusinessException;
import com.taotao.education.common.result.ResultCode;
import com.taotao.education.common.utils.JwtUtils;
import com.taotao.education.user.dto.LoginDTO;
import com.taotao.education.user.dto.RegisterDTO;
import com.taotao.education.user.dto.UserUpdateDTO;
import com.taotao.education.user.entity.User;
import com.taotao.education.user.mapper.UserMapper;
import com.taotao.education.user.service.UserService;
import com.taotao.education.user.vo.LoginVO;
import com.taotao.education.user.vo.OrgOptionVO;
import com.taotao.education.user.vo.UserVO;
import com.taotao.education.user.vo.OpsUserOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        // 检查手机号是否已存在
        if (registerDTO.getPhone() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, registerDTO.getPhone());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setRole(1); // 默认为学员
        user.setStatus(1); // 默认正常状态

        this.save(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 生成Token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername());

        // 存储Token到Redis
        String key = RedisConstants.USER_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, token, RedisConstants.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setRole(user.getRole());

        return loginVO;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getProvince() != null) {
            user.setProvince(updateDTO.getProvince());
        }
        if (updateDTO.getCity() != null) {
            user.setCity(updateDTO.getCity());
        }
        if (updateDTO.getSignature() != null) {
            user.setSignature(updateDTO.getSignature());
        }

        this.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(user);

        // 清除Token
        String key = RedisConstants.USER_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    @Override
    public void logout(Long userId) {
        String key = RedisConstants.USER_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    @Override
    public List<OrgOptionVO> listOrgs() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 4)
               .eq(User::getStatus, 1)
               .eq(User::getDeleted, 0);
        List<User> users = this.list(wrapper);
        return users.stream().map(u -> {
            OrgOptionVO vo = new OrgOptionVO();
            vo.setId(u.getId());
            vo.setName(u.getNickname() != null ? u.getNickname() : u.getUsername());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OpsUserOverviewVO getOpsOverview() {
        OpsUserOverviewVO vo = new OpsUserOverviewVO();
        LambdaQueryWrapper<User> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(User::getDeleted, 0);
        vo.setTotal(this.count(baseWrapper));

        LambdaQueryWrapper<User> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(User::getDeleted, 0).eq(User::getRole, 1);
        vo.setStudents(this.count(studentWrapper));

        LambdaQueryWrapper<User> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(User::getDeleted, 0).eq(User::getRole, 2);
        vo.setTeachers(this.count(teacherWrapper));

        LambdaQueryWrapper<User> orgWrapper = new LambdaQueryWrapper<>();
        orgWrapper.eq(User::getDeleted, 0).eq(User::getRole, 4);
        vo.setOrgs(this.count(orgWrapper));

        LambdaQueryWrapper<User> opsWrapper = new LambdaQueryWrapper<>();
        opsWrapper.eq(User::getDeleted, 0).eq(User::getRole, 5);
        vo.setOps(this.count(opsWrapper));
        return vo;
    }
}

