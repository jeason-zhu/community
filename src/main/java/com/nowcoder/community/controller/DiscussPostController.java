package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.security.krb5.internal.ReplayCache;

import java.util.*;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String add(String title,String content)
    {
        User user = hostHolder.getUser();
        if(user==null)
        {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        // 异常等后续处理
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public  String getDiscussPost(@PathVariable int discussPostId, Model model, Page page)
    {
        //查询帖子
        DiscussPost post = discussPostService.findDiscussPost(discussPostId);
        model.addAttribute("post",post);
        //查询作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        //设置评论分页信息
        page.setLimit(5);
        page.setRows(post.getCommentCount());
        page.setPath("/discuss/detail/"+discussPostId);
        //查询帖子评论
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //评论：给帖子的评论
        //回复：给评论的评论
        //评论VO列表
        List<Map<String,Object>> commentVOList = new ArrayList<>();
        for(Comment comment:commentList)
        {
            Map<String,Object> commentVO = new HashMap<>();
            commentVO.put("comment",comment);
            //查询评论者信息
            User commentUser = userService.findUserById(comment.getUserId());
            commentVO.put("user",commentUser);
            //查询回复列表（评论不设置分页）
            List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
            //回复VO列表
            List<Map<String,Object>> replyVOList = new ArrayList<>();
            for(Comment reply: replyList)
            {
                Map<String,Object> replyVO = new HashMap<>();
                replyVO.put("reply",reply);
                //查询回复者信息
                User replyUser = userService.findUserById(reply.getUserId());
                replyVO.put("user",replyUser);
                //回复目标
                int targetId = reply.getTargetId();
                User target = userService.findUserById(targetId);
                replyVO.put("target",target);
                replyVOList.add(replyVO);
            }
            commentVO.put("replys",replyVOList);
            //查询评论回复数
            int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
            commentVO.put("replyCount",replyCount);
            commentVOList.add(commentVO);
        }
        model.addAttribute("comments",commentVOList);
        return "/site/discuss-detail";
    }
}
