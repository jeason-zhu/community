package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    //根据评论对应的实体(对什么进行评论)查询评论
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);
    //查询评论数量
    int selectCountByEntity(int entityType,int entityId);
    //插入一条评论
    int insertComment(Comment comment);
}
