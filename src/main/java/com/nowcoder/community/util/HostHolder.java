package com.nowcoder.community.util;


import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.
 */
//设置线程变量
@Component
public class HostHolder {

    private  ThreadLocal<User>  threadLocal = new ThreadLocal<>();

    public  void setUser(User user)
    {
        threadLocal.set(user);
    }

    public User getUser()
    {
        return threadLocal.get();
    }

    //每次线程结束之后，清除线程状态，以免放入线程池之后，对下次调用线程有影响
    public  void clear()
    {
        threadLocal.remove();
    }

}
