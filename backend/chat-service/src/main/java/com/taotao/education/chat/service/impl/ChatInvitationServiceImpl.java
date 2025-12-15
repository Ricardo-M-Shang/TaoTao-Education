package com.taotao.education.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.chat.dto.InviteMembersDTO;
import com.taotao.education.chat.entity.ChatInvitation;
import com.taotao.education.chat.entity.ChatRoom;
import com.taotao.education.chat.mapper.ChatInvitationMapper;
import com.taotao.education.chat.mapper.ChatRoomMapper;
import com.taotao.education.chat.service.ChatInvitationService;
import com.taotao.education.chat.service.ChatMemberService;
import com.taotao.education.chat.util.RedisLockUtil;
import com.taotao.education.chat.vo.ChatInvitationVO;
import com.taotao.education.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天室邀请服务实现
 * 使用 Redis 分布式锁防止重复邀请
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatInvitationServiceImpl extends ServiceImpl<ChatInvitationMapper, ChatInvitation> implements ChatInvitationService {

    private final ChatRoomMapper roomMapper;
    private final ChatMemberService memberService;
    private final JdbcTemplate jdbcTemplate;
    private final RedisLockUtil redisLockUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteMembers(Long inviterId, InviteMembersDTO dto) {
        ChatRoom room = roomMapper.selectById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }

        // 检查邀请者权限（只有管理员/创建者可以邀请）
        if (!memberService.isMember(inviterId, dto.getRoomId())) {
            throw new BusinessException("您不是聊天室成员");
        }

        // 获取邀请人信息
        String userSql = "SELECT nickname, avatar FROM t_user WHERE id = ? AND deleted = 0";
        List<Map<String, Object>> userInfo = jdbcTemplate.queryForList(userSql, inviterId);
        if (userInfo.isEmpty()) {
            throw new BusinessException("用户不存在");
        }
        Map<String, Object> inviter = userInfo.get(0);

        for (Long inviteeId : dto.getInviteeIds()) {
            // 检查是否已经是成员
            if (memberService.isMember(inviteeId, dto.getRoomId())) {
                continue;
            }

            // 检查是否已有待处理的邀请
            ChatInvitation existing = baseMapper.checkExistingInvitation(dto.getRoomId(), inviteeId);
            if (existing != null) {
                continue;
            }

            ChatInvitation invitation = new ChatInvitation();
            invitation.setRoomId(dto.getRoomId());
            invitation.setRoomName(room.getName());
            invitation.setInviteeId(inviteeId);
            invitation.setInviterId(inviterId);
            invitation.setInviterName((String) inviter.get("nickname"));
            invitation.setInviterAvatar((String) inviter.get("avatar"));
            invitation.setStatus(0);
            invitation.setMessage(dto.getMessage());
            invitation.setExpireTime(LocalDateTime.now().plusDays(7)); // 7天有效期

            this.save(invitation);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteAllCourseStudents(Long inviterId, Long roomId) {
        ChatRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        if (!inviterId.equals(room.getCreatorId())) {
            throw new BusinessException("只有创建者才能邀请全部学员");
        }

        // 获取课程的所有学员
        String sql = """
            SELECT DISTINCT o.user_id
            FROM t_order o
            WHERE o.course_id = ? AND o.status = 1 AND o.deleted = 0
            """;
        List<Long> studentIds = jdbcTemplate.queryForList(sql, Long.class, room.getCourseId());

        if (!studentIds.isEmpty()) {
            InviteMembersDTO dto = new InviteMembersDTO();
            dto.setRoomId(roomId);
            dto.setInviteeIds(studentIds);
            dto.setMessage("讲师邀请您加入课程聊天室");
            inviteMembers(inviterId, dto);
        }
    }

    @Override
    public List<ChatInvitationVO> getPendingInvitations(Long userId) {
        List<ChatInvitation> invitations = baseMapper.getPendingInvitations(userId);
        return invitations.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatInvitationVO> getUserInvitations(Long userId) {
        List<ChatInvitation> invitations = baseMapper.getUserInvitations(userId);
        return invitations.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptInvitation(Long userId, Long invitationId) {
        // 使用分布式锁防止重复接受邀请
        String lockKey = "invitation:accept:" + invitationId;
        
        redisLockUtil.executeWithLock(lockKey, () -> {
            ChatInvitation invitation = this.getById(invitationId);
            if (invitation == null) {
                throw new BusinessException("邀请不存在");
            }
            if (!invitation.getInviteeId().equals(userId)) {
                throw new BusinessException("这不是您的邀请");
            }
            if (invitation.getStatus() != 0) {
                throw new BusinessException("邀请已处理");
            }
            if (invitation.getExpireTime().isBefore(LocalDateTime.now())) {
                invitation.setStatus(3);
                this.updateById(invitation);
                throw new BusinessException("邀请已过期");
            }

            // 获取用户信息
            String userSql = "SELECT nickname, avatar FROM t_user WHERE id = ? AND deleted = 0";
            List<Map<String, Object>> userInfo = jdbcTemplate.queryForList(userSql, userId);
            if (userInfo.isEmpty()) {
                throw new BusinessException("用户不存在");
            }
            Map<String, Object> user = userInfo.get(0);

            // 加入聊天室
            memberService.joinRoom(userId, invitation.getRoomId(),
                (String) user.get("nickname"),
                (String) user.get("avatar"),
                1); // 角色1-普通成员

            // 更新邀请状态
            invitation.setStatus(1);
            invitation.setHandleTime(LocalDateTime.now());
            this.updateById(invitation);
            
            log.info("用户接受邀请: userId={}, invitationId={}, roomId={}", 
                    userId, invitationId, invitation.getRoomId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectInvitation(Long userId, Long invitationId) {
        ChatInvitation invitation = this.getById(invitationId);
        if (invitation == null) {
            throw new BusinessException("邀请不存在");
        }
        if (!invitation.getInviteeId().equals(userId)) {
            throw new BusinessException("这不是您的邀请");
        }
        if (invitation.getStatus() != 0) {
            throw new BusinessException("邀请已处理");
        }

        invitation.setStatus(2);
        invitation.setHandleTime(LocalDateTime.now());
        this.updateById(invitation);
    }

    @Override
    public int getPendingCount(Long userId) {
        List<ChatInvitation> invitations = baseMapper.getPendingInvitations(userId);
        return invitations.size();
    }

    private ChatInvitationVO convertToVO(ChatInvitation invitation) {
        ChatInvitationVO vo = new ChatInvitationVO();
        BeanUtils.copyProperties(invitation, vo);
        return vo;
    }
}

