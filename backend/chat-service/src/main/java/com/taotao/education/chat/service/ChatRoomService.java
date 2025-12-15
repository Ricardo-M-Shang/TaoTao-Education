package com.taotao.education.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taotao.education.chat.dto.CreateRoomDTO;
import com.taotao.education.chat.entity.ChatRoom;
import com.taotao.education.chat.vo.ChatRoomVO;
import com.taotao.education.chat.vo.CourseStudentVO;

import java.util.List;

/**
 * 聊天室服务接口
 */
public interface ChatRoomService extends IService<ChatRoom> {

    /**
     * 创建聊天室
     */
    Long createRoom(Long userId, CreateRoomDTO dto);

    /**
     * 获取聊天室详情
     */
    ChatRoomVO getRoomDetail(Long userId, Long roomId);

    /**
     * 获取用户加入的聊天室列表
     */
    List<ChatRoomVO> getUserRooms(Long userId);

    /**
     * 获取讲师创建的聊天室列表
     */
    List<ChatRoomVO> getCreatorRooms(Long userId);

    /**
     * 获取课程对应的聊天室
     */
    ChatRoomVO getRoomByCourse(Long userId, Long courseId);

    /**
     * 更新聊天室信息
     */
    void updateRoom(Long userId, Long roomId, CreateRoomDTO dto);

    /**
     * 删除聊天室
     */
    void deleteRoom(Long userId, Long roomId);

    /**
     * 更新公告
     */
    void updateAnnouncement(Long userId, Long roomId, String announcement);

    /**
     * 获取课程学员列表（用于邀请）
     */
    List<CourseStudentVO> getCourseStudents(Long userId, Long roomId);
}

