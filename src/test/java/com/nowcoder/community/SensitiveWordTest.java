package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveWordTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public  void FilterTest()
    {
        String filter = sensitiveFilter.filter("这里可以赌博，可以吸毒");
        String filter1 = sensitiveFilter.filter("赌博吸毒***ni");
        String filter2 = sensitiveFilter.filter("这里可以☆赌☆博☆,可以☆嫖☆娼☆,可以☆吸☆毒☆,可以☆开☆票☆,哈哈哈!");
        System.out.println(filter);
        System.out.println(filter1);
        System.out.println(filter2);
    }

}
