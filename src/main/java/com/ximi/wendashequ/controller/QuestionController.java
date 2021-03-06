package com.ximi.wendashequ.controller;

import com.ximi.wendashequ.model.*;
import com.ximi.wendashequ.service.CommentService;
import com.ximi.wendashequ.service.LikeService;
import com.ximi.wendashequ.service.QuestionService;
import com.ximi.wendashequ.service.UserService;
import com.ximi.wendashequ.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 单广美 on 2017/10/16.
 *
 * @Description: 问题功能
 */
@Controller
@RequestMapping("/question")
public class QuestionController {
    private static final Logger log = LoggerFactory.getLogger(QuestionController.class);
    // 注入 问题服务层
    @Autowired
    private QuestionService questionService;
    // 注入用户全局对象
    @Autowired
    private HostHolder hostHolder;
    // 注入用户服务层
    @Autowired
    private UserService userService;
    // 注入评论服务层
    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    //添加问题
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content") String content){
        //构建问题对象
        Question question = new Question();
        try{
            question.setTitle(title);
            question.setContent(content);
            question.setCreateDate(new Date());
            question.setCommentCount(0);
            if (hostHolder.getUser() == null){
                //强制跳转  返回状态码  999
                return WendaUtil.getJSONObject(999);
            }
            //设置id
            question.setUserId(hostHolder.getUser().getId());
            int count = questionService.addQuestion(question);
            if (count > 0){
                return WendaUtil.getJSONObject(0);
            }
        }catch (Exception e){
            log.error("发布问题失败"+e.getMessage());
        }

        return WendaUtil.getJSONObject(1,"失败");
    }
    //查看唯一的问题
    @RequestMapping(value = "/{id}")
    public String showQuestion(Model model, @PathVariable("id") int id){
        // 获取问题对象
        Question question = questionService.findQuestionById(id);

        model.addAttribute("question",question);
        model.addAttribute("user",userService.findUserById(question.getUserId()));
        //获取该问题的所有评论
        List<Comment> commentLists = commentService.selectComments(EntityType.ENTITY_QUESTION,id);
        List<ViewObject> details = new ArrayList<>();
        for (Comment comment:commentLists){
            ViewObject o = new ViewObject();
            if (hostHolder.getUser() == null){
                o.set("liked",0);
            }else {
                o.set("liked",likeService.getLikeStatus(hostHolder.getUser().getId(),EntityType.ENTITY_COMMENT,comment.getId()));
            }
            o.set("likeCount",likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId()));
            o.set("comment",comment);
            o.set("user",userService.findUserById(comment.getUserId()));
            details.add(o);
        }
        //把数据添加到模型
        model.addAttribute("details",details);
        return "detail";
    }
}
