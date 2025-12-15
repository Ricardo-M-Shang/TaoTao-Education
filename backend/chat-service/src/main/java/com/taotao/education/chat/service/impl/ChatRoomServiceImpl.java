package com.taotao.education.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taotao.education.chat.dto.CreateRoomDTO;
import com.taotao.education.chat.entity.ChatMember;
import com.taotao.education.chat.entity.ChatMessage;
import com.taotao.education.chat.entity.ChatRoom;
import com.taotao.education.chat.mapper.ChatMemberMapper;
import com.taotao.education.chat.mapper.ChatMessageMapper;
import com.taotao.education.chat.mapper.ChatRoomMapper;
import com.taotao.education.chat.service.ChatMemberService;
import com.taotao.education.chat.service.ChatRoomService;
import com.taotao.education.chat.util.RedisLockUtil;
import com.taotao.education.chat.vo.ChatRoomVO;
import com.taotao.education.chat.vo.CourseStudentVO;
import com.taotao.education.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天室服务实现
 * 使用 Redis 分布式锁防止并发问题
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom> implements ChatRoomService {

    private final ChatMemberMapper memberMapper;
    private final ChatMessageMapper messageMapper;
    private final ChatMemberService memberService;
    private final JdbcTemplate jdbcTemplate;
    private final RedisLockUtil redisLockUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRoom(Long userId, CreateRoomDTO dto) {
        // 使用分布式锁防止同一课程并发创建聊天室
        String lockKey = "chatroom:create:course:" + dto.getCourseId();
        
        return redisLockUtil.executeWithLock(lockKey, () -> {
            log.info("开始创建聊天室: userId={}, courseId={}", userId, dto.getCourseId());
            
            // 检查该课程是否已有聊天室
            ChatRoom existingRoom = baseMapper.getByCourseId(dto.getCourseId());
            if (existingRoom != null) {
                throw new BusinessException("该课程已存在聊天室");
            }

            // 验证用户是否是该课程的讲师
            String sql = "SELECT teacher_id, title, cover FROM t_course WHERE id = ? AND deleted = 0";
            List<Map<String, Object>> courseInfo = jdbcTemplate.queryForList(sql, dto.getCourseId());
            if (courseInfo.isEmpty()) {
                throw new BusinessException("课程不存在");
            }
            
            Map<String, Object> course = courseInfo.get(0);
            // JDBC 返回的数值类型可能是 Integer 或 BigInteger，需要安全转换
            Object teacherIdObj = course.get("teacher_id");
            Long teacherId = teacherIdObj instanceof Number ? ((Number) teacherIdObj).longValue() : null;
            if (teacherId == null || !userId.equals(teacherId)) {
                throw new BusinessException("只有课程讲师才能创建聊天室");
            }

            // 获取用户信息
            String userSql = "SELECT nickname, avatar FROM t_user WHERE id = ? AND deleted = 0";
            List<Map<String, Object>> userInfo = jdbcTemplate.queryForList(userSql, userId);
            if (userInfo.isEmpty()) {
                throw new BusinessException("用户不存在");
            }
            Map<String, Object> user = userInfo.get(0);

            // 创建聊天室
            ChatRoom room = new ChatRoom();
            room.setName(dto.getName());
            room.setDescription(dto.getDescription());
            room.setCover(dto.getCover() != null ? dto.getCover() : (String) course.get("cover"));
            room.setCourseId(dto.getCourseId());
            room.setCourseTitle((String) course.get("title"));
            room.setCreatorId(userId);
            room.setCreatorName((String) user.get("nickname"));
            room.setCreatorAvatar((String) user.get("avatar"));
            room.setMemberCount(1);
            room.setMaxMembers(dto.getMaxMembers());
            room.setStatus(1);
            room.setNeedApproval(dto.getNeedApproval());
            room.setAnnouncement(dto.getAnnouncement());

            this.save(room);

            // 创建者自动加入聊天室
            memberService.joinRoom(userId, room.getId(), 
                (String) user.get("nickname"), 
                (String) user.get("avatar"), 
                3); // 角色3-创建者

            log.info("聊天室创建成功: roomId={}", room.getId());
            return room.getId();
        });
    }

    @Override
    public ChatRoomVO getRoomDetail(Long userId, Long roomId) {
        ChatRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }

        ChatRoomVO vo = convertToVO(room);
        
        // 获取用户在该聊天室的信息
        ChatMember member = memberMapper.getMember(roomId, userId);
        if (member != null) {
            vo.setUnreadCount(member.getUnreadCount());
            vo.setIsPinned(member.getIsPinned());
            vo.setMyRole(member.getRole());
        }
        
        vo.setIsCreator(userId.equals(room.getCreatorId()));

        // 获取最新消息
        ChatMessage latestMsg = messageMapper.getLatestMessage(roomId);
        if (latestMsg != null) {
            vo.setLatestMessage(latestMsg.getContent());
            vo.setLatestMessageTime(latestMsg.getCreateTime());
        }

        return vo;
    }

    @Override
    public List<ChatRoomVO> getUserRooms(Long userId) {
        List<ChatRoom> rooms = baseMapper.getUserRooms(userId);
        return rooms.stream()
            .map(room -> {
                ChatRoomVO vo = convertToVO(room);
                // 获取用户在该聊天室的信息
                ChatMember member = memberMapper.getMember(room.getId(), userId);
                if (member != null) {
                    vo.setUnreadCount(member.getUnreadCount());
                    vo.setIsPinned(member.getIsPinned());
                    vo.setMyRole(member.getRole());
                }
                vo.setIsCreator(userId.equals(room.getCreatorId()));
                
                // 获取最新消息
                ChatMessage latestMsg = messageMapper.getLatestMessage(room.getId());
                if (latestMsg != null) {
                    vo.setLatestMessage(latestMsg.getContent());
                    vo.setLatestMessageTime(latestMsg.getCreateTime());
                }
                return vo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomVO> getCreatorRooms(Long userId) {
        List<ChatRoom> rooms = baseMapper.getCreatorRooms(userId);
        return rooms.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public ChatRoomVO getRoomByCourse(Long userId, Long courseId) {
        ChatRoom room = baseMapper.getByCourseId(courseId);
        if (room == null) {
            return null;
        }
        return getRoomDetail(userId, room.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(Long userId, Long roomId, CreateRoomDTO dto) {
        ChatRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        if (!userId.equals(room.getCreatorId())) {
            throw new BusinessException("只有创建者才能修改聊天室");
        }

        room.setName(dto.getName());
        room.setDescription(dto.getDescription());
        if (dto.getCover() != null) {
            room.setCover(dto.getCover());
        }
        room.setMaxMembers(dto.getMaxMembers());
        room.setNeedApproval(dto.getNeedApproval());
        if (dto.getAnnouncement() != null) {
            room.setAnnouncement(dto.getAnnouncement());
        }

        this.updateById(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long userId, Long roomId) {
        ChatRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        if (!userId.equals(room.getCreatorId())) {
            throw new BusinessException("只有创建者才能删除聊天室");
        }

        this.removeById(roomId);
    }

    @Override
    public void updateAnnouncement(Long userId, Long roomId, String announcement) {
        ChatRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        
        ChatMember member = memberMapper.getMember(roomId, userId);
        if (member == null || member.getRole() < 2) {
            throw new BusinessException("只有管理员或创建者才能修改公告");
        }

        room.setAnnouncement(announcement);
        this.updateById(room);
    }

    @Override
    public List<CourseStudentVO> getCourseStudents(Long userId, Long roomId) {
        ChatRoom room = this.getById(roomId);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        if (!userId.equals(room.getCreatorId())) {
            throw new BusinessException("只有创建者才能查看学员列表");
        }

        // 获取课程的学员列表（通过订单表查询已购买的学员）
        String sql = """
            SELECT DISTINCT u.id as user_id, u.username, u.nickname, u.avatar
            FROM t_order o
            JOIN t_user u ON o.user_id = u.id
            WHERE o.course_id = ? AND o.status = 1 AND o.deleted = 0 AND u.deleted = 0
            """;
        List<Map<String, Object>> students = jdbcTemplate.queryForList(sql, room.getCourseId());

        // 获取已在聊天室的成员
        List<ChatMember> members = memberMapper.getRoomMembers(roomId);
        List<Long> memberUserIds = members.stream()
            .map(ChatMember::getUserId)
            .collect(Collectors.toList());

        // 获取已被邀请但未处理的用户
        String inviteSql = """
            SELECT invitee_id FROM t_chat_invitation
            WHERE room_id = ? AND status = 0 AND deleted = 0
            """;
        List<Long> invitedUserIds = jdbcTemplate.queryForList(inviteSql, Long.class, roomId);

        List<CourseStudentVO> result = new ArrayList<>();
        for (Map<String, Object> student : students) {
            Object userIdObj = student.get("user_id");
            Long studentUserId = userIdObj instanceof Number ? ((Number) userIdObj).longValue() : null;
            // 排除创建者自己或无效用户
            if (studentUserId == null || studentUserId.equals(userId)) {
                continue;
            }
            
            CourseStudentVO vo = new CourseStudentVO();
            vo.setUserId(studentUserId);
            vo.setUsername((String) student.get("username"));
            vo.setNickname((String) student.get("nickname"));
            vo.setAvatar((String) student.get("avatar"));
            vo.setInRoom(memberUserIds.contains(studentUserId));
            vo.setInvited(invitedUserIds.contains(studentUserId));
            result.add(vo);
        }

        return result;
    }

    private ChatRoomVO convertToVO(ChatRoom room) {
        ChatRoomVO vo = new ChatRoomVO();
        BeanUtils.copyProperties(room, vo);
        return vo;
    }
}

