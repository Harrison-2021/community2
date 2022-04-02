package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {
    @Autowired
    ElasticsearchTemplate elasticTemplate;

    @Autowired
    DiscussPostRepository discussRepository;

    @Autowired
    DiscussPostMapper discussPostMapper;

    /**
     * 测试添加文档数据-一条数据
     */
    @Test
    public void testPUT() {
        discussRepository.save(discussPostMapper.selectPost(241));
        discussRepository.save(discussPostMapper.selectPost(242));
        discussRepository.save(discussPostMapper.selectPost(243));
    }

    /**
     * 测试添加多条数据-
     */
    @Test
    public void testPUTList() {
        discussRepository.saveAll(discussPostMapper.getPosts(101, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(102, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(103, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(111, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(112, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(131, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(132, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(133, 0, 100));
        discussRepository.saveAll(discussPostMapper.getPosts(134, 0, 100));
    }

    /**
     * 修改数据
     * 为保证数据同步，先在mysql中修改数据，再将数据重新保存覆盖进ES库中
     */
    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectPost(231);
        post.setContent("我其实不是新人，哈哈哈");
        // discussPostMapper.update(231, post.getContent());
        discussRepository.save(post);
    }

    /**
     * 删除数据
     */
    @Test
    public void testDelete() {
//        discussRepository.delete(discussPostMapper.selectDiscussPostById(231));
        discussRepository.deleteAll();
    }

    /**
     * 使用ElasticsearchRepository搜索匹配的数据
     */
    @Test
    public void testSearchByRepository() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        // elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)
        // 底层获取得到了高亮显示的值, 但是没有返回.

        Page<DiscussPost> page = discussRepository.search(searchQuery);
        System.out.println(page.getTotalElements());    // 一共匹配的数据条数
        System.out.println(page.getTotalPages());       // 一共多少页
        System.out.println(page.getNumber());           // 当前页码，默认从0开始
        System.out.println(page.getSize());             // 每页显示的数量
        for (DiscussPost post : page) {                 // 当前页的10条数据
            System.out.println(post);
        }
    }

    /**
     * 使用ElasticsearchTemplate 搜索匹配的数据
     */
    @Test
    public void testSearchByTemplate() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits = response.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }

                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
            }
        });

        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }
}
