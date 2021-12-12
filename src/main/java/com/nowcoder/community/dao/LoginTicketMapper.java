package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoginTicketMapper {

    //一般来说dao增删改查返回值为影响的行数
    //sql注解中可以只写一个参数，多个参数只是便于分行，会被自动拼接

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    int insertLoginTicket(LoginTicket loginTicket);

//    增加if判断的写法
//    @Update({
//            "<script>",
//            "update login_ticket set status=#{status} where ticket=#{ticket} ",
//            "<if test=\"ticket!=null\"> ",
//            "and 1=1 ",
//            "</if>",
//            "</script>"})
    @Update({"UPDATE login_ticket SET `status`=#{status} where ticket = #{ticket} "})
    int updateStatus(String ticket,int status);

    @Select({"SELECT `user_id`,`ticket`,`status`,`expired` FROM login_ticket where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticket );

}
