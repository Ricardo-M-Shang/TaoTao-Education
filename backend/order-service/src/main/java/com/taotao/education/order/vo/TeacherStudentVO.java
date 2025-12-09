package com.taotao.education.order.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讲师查看学员列表VO
 */
@Data
public class TeacherStudentVO {

    private Long userId;

    private String username;

    private String nickname;

    private Long courseId;

    private String courseTitle;

    private Integer progress;

    private Integer isFinished;

    private LocalDateTime lastStudyTime;

    private LocalDateTime payTime;
}

