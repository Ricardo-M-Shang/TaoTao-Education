package com.taotao.education.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.chat.entity.ChatMember;
import com.taotao.education.chat.entity.ChatRoom;
import com.taotao.education.chat.mapper.ChatMemberMapper;
import com.taotao.education.chat.mapper.ChatRoomMapper;
import com.taotao.education.chat.service.ChatMemberService;
import com.taotao.education.chat.vo.ChatMemberVO;
import com.taotao.education.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 聊天室成员服务实现
 */
@Service
@RequiredArgsConstructor
public class ChatMemberServiceImpl extends ServiceImpl<ChatMemberMapper, ChatMember> implements ChatMemberService {

    private final ChatRoomMapper roomMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ONLINE_USERS_KEY = "chat:online:room:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinRoom(Long userId, Long roomId, String nickname, String avatar, Integer role) {
        // 检查是否已经是成员
        ChatMember existingMember = baseMapper.getMember(roomId, userId);
        if (existingMember != null) {
            if (existingMember.getStatus() == 1) {
                throw new BusinessException("您已经是聊天室成员");
            }
            // 重新激活
            existingMember.setStatus(1);
            existingMember.setJoinTime(LocalDateTime.now());
            this.updateById(existingMember);
        } else {
            // 检查聊天室是否存在及成员数限制
            ChatRoom room = roomMapper.selectById(roomId);
            if (room == null) {
                throw new BusinessException("聊天室不存在");
            }
            if (room.getMaxMembers() > 0 && room.getMemberCount() >= room.getMaxMembers()) {
                throw new BusinessException("聊天室已满");
            }

            // 创建成员记录
            ChatMember member = new ChatMember();
            member.setRoomId(roomId);
            member.setUserId(userId);
            member.setNickname(nickname);
            member.setAvatar(avatar);
            member.setRole(role);
            member.setStatus(1);
            member.setJoinTime(LocalDateTime.now());
            member.setLastActiveTime(LocalDateTime.now());
            member.setUnreadCount(0);
            member.setIsPinned(0);
            member.setIsMuted(0);
            this.save(member);

            // 更新聊天室成员数
            room.setMemberCount(room.getMemberCount() + 1);
            roomMapper.updateById(room);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveRoom(Long userId, Long roomId) {
        ChatMember member = baseMapper.getMember(roomId, userId);
        if (member == null) {
            throw new BusinessException("您不是聊天室成员");
        }
        if (member.getRole() == 3) {
            throw new BusinessException("创建者不能退出聊天室，如需关闭请删除聊天室");
        }

        member.setStatus(0);
        this.updateById(member);

        // 更新聊天室成员数
        ChatRoom room = roomMapper.selectById(roomId);
        if (room != null && room.getMemberCount() > 0) {
            room.setMemberCount(room.getMemberCount() - 1);
            roomMapper.updateById(room);
        }
    }

    @Override
    public List<ChatMemberVO> getRoomMembers(Long roomId) {
        List<ChatMember> members = baseMapper.getRoomMembers(roomId);
        
        // 获取在线用户
        Set<Object> onlineUsers = redisTemplate.opsForSet().members(ONLINE_USERS_KEY + roomId);

        return members.stream()
            .map(member -> {
                ChatMemberVO vo = new ChatMemberVO();
                BeanUtils.copyProperties(member, vo);
                vo.setIsOnline(onlineUsers != null && onlineUsers.contains(member.getUserId().toString()));
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public boolean isMember(Long userId, Long roomId) {
        ChatMember member = baseMapper.getMember(roomId, userId);
        return member != null && member.getStatus() == 1;
    }

    @Override
    public ChatMember getMember(Long userId, Long roomId) {
        return baseMapper.getMember(roomId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long operatorId, Long roomId, Long targetUserId) {
        ChatMember operator = baseMapper.getMember(roomId, operatorId);
        if (operator == null || operator.getRole() < 2) {
            throw new BusinessException("您没有权限移除成员");
        }

        ChatMember target = baseMapper.getMember(roomId, targetUserId);
        if (target == null) {
            throw new BusinessException("成员不存在");
        }
        if (target.getRole() >= operator.getRole()) {
            throw new BusinessException("您不能移除同级或更高权限的成员");
        }

        target.setStatus(0);
        this.updateById(target);

        // 更新聊天室成员数
        ChatRoom room = roomMapper.selectById(roomId);
        if (room != null && room.getMemberCount() > 0) {
            room.setMemberCount(room.getMemberCount() - 1);
            roomMapper.updateById(room);
        }
    }

    @Override
    public void setAdmin(Long operatorId, Long roomId, Long targetUserId, boolean isAdmin) {
        ChatMember operator = baseMapper.getMember(roomId, operatorId);
        if (operator == null || operator.getRole() != 3) {
            throw new BusinessException("只有创建者才能设置管理员");
        }

        ChatMember target = baseMapper.getMember(roomId, targetUserId);
        if (target == null) {
            throw new BusinessException("成员不存在");
        }
        if (target.getRole() == 3) {
            throw new BusinessException("不能修改创建者权限");
        }

        target.setRole(isAdmin ? 2 : 1);
        this.updateById(target);
    }

    @Override
    public void muteMember(Long operatorId, Long roomId, Long targetUserId, boolean mute) {
        ChatMember operator = baseMapper.getMember(roomId, operatorId);
        if (operator == null || operator.getRole() < 2) {
            throw new BusinessException("您没有权限禁言成员");
        }

        ChatMember target = baseMapper.getMember(roomId, targetUserId);
        if (target == null) {
            throw new BusinessException("成员不存在");
        }
        if (target.getRole() >= operator.getRole()) {
            throw new BusinessException("您不能禁言同级或更高权限的成员");
        }

        target.setStatus(mute ? 2 : 1);
        this.updateById(target);
    }

    @Override
    public void updateMemberSettings(Long userId, Long roomId, Boolean isPinned, Boolean isMuted) {
        ChatMember member = baseMapper.getMember(roomId, userId);
        if (member == null) {
            throw new BusinessException("您不是聊天室成员");
        }

        if (isPinned != null) {
            member.setIsPinned(isPinned ? 1 : 0);
        }
        if (isMuted != null) {
            member.setIsMuted(isMuted ? 1 : 0);
        }
        this.updateById(member);
    }

    @Override
    public void clearUnreadCount(Long userId, Long roomId) {
        baseMapper.clearUnreadCount(roomId, userId);
    }
}

