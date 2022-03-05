package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {
    // 增-将创建的登录凭证入库
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public int insertLoginTicket(LoginTicket loginTicket);

    // 查询-根据ticket查询
    @Select({
            "select id, user_id, ticket, status, expired ",
            "from login_ticket where ticket = #{ticket}"
    })
    public LoginTicket selectByTicket(String ticket);

    // 改-修改登录状态-根据ticket查询到一行对象数据
    @Update({
            "<script>",
            "update login_ticket set status = #{status} where ticket = #{ticket} ",
            "<if test = \"ticket != null\"> ",
            "and 1 = 1 ",
            "</if>",
            "</script>"
    })
    public int updateLoginTicket(String ticket, int status);
}
