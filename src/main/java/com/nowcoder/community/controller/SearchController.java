package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticSearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    ElasticSearchService elasticSearchService;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    /**
     * 根据关键字搜索查询帖子请求
     * @param keyword  关键字
     * @param model
     * @param page
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(String keyword, Model model, Page page) {
        // 搜索帖子-分页搜索-页面默认设置当前页为1，limit为10
        // 注意-需要的分页信息是-当前页码和当前页数量，与mysql查询的分页条件不同（起始行-当前页显示多少行）
        org.springframework.data.domain.Page<DiscussPost> searchResult
                = elasticSearchService.searchPosts(keyword, page.getCurrent() - 1, page.getLimit());

        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 对帖子的点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);
        // 页面设置
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());
        return "/site/search";
    }
}
