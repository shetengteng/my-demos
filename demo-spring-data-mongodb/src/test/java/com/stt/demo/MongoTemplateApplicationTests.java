package com.stt.demo;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SpringBootTest
class MongoTemplateApplicationTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void createUser() {
        User user = new User();
        user.setName("stt");
        user.setAge(22);
        user.setEmail("work_stt@163.com");
        User re = mongoTemplate.insert(user);
        System.out.println(re);
    }

    @Test
    public void findAll() {
        List<User> all = mongoTemplate.findAll(User.class);
        System.out.println(all);
    }

    @Test
    public void getById() {
        User re = mongoTemplate.findById("61acc4a46eeb333442e675d9", User.class);
        System.out.println(re);
    }

    @Test
    public void findUserList() {
        Query query = new Query(Criteria
                .where("name").is("stt")
                .and("age").is(22));
        List<User> re = mongoTemplate.find(query, User.class);
        System.out.println(re);
    }

    @Test
    public void findUserLikeName() {
        String name = "st";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        // CASE_INSENSITIVE 表示大小写不敏感
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));
        List<User> re = mongoTemplate.find(query, User.class);
        System.out.println(re);
    }

    @Test
    public void findUserPage() {
        String name = "st";
        String regex = String.format("%s%s%s", "^.*", name, ".*$");
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));
        long total = mongoTemplate.count(query, User.class);

        int pageNo = 1;
        int pageSize = 10;
        query = query.skip((pageNo - 1) * pageSize).limit(pageSize);
        List<User> users = mongoTemplate.find(query, User.class);

        Map<String, Object> re = new HashMap<>();
        re.put("list", users);
        re.put("total", total);
        System.out.println(re);
    }

    @Test
    public void updateUser(){
        User user = mongoTemplate.findById("61acc4a46eeb333442e675d9",User.class);
        user.setName("stt_new");
        user.setAge(23);

        Query query = new Query(Criteria.where("_id").is(user.getId()));

        Update update = new Update();
        update.set("name",user.getName());
        update.set("age",user.getAge());

        UpdateResult re = mongoTemplate.upsert(query, update, User.class);
        long modifiedCount = re.getModifiedCount();
        System.out.println(modifiedCount);
    }

    @Test
    public void deleteUser(){
        Query query = new Query(Criteria.where("_id").is("61acc4a46eeb333442e675d9"));
        DeleteResult re = mongoTemplate.remove(query, User.class);
        System.out.println(re.getDeletedCount());
    }

}
