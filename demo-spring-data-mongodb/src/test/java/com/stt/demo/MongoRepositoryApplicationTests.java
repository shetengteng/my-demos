package com.stt.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.List;

@SpringBootTest
class MongoRepositoryApplicationTests {

    @Autowired
    private UserRepository repository;

    @Test
    public void createUser() {
        User user = new User();
        user.setName("stt");
        user.setAge(22);
        user.setEmail("work_stt@163.com");
        User re = repository.save(user);
        System.out.println(re);
        // result
        // User(id=61acceebdfa8a231d23eda37, name=stt, age=22, email=work_stt@163.com, createDate=null)
    }

    @Test
    public void findAllUser() {
        List<User> all = repository.findAll();
        System.out.println(all);
    }

    @Test
    public void findById() {
        User user = repository.findById("61acceebdfa8a231d23eda37").get();
        System.out.println(user);
    }

    // 条件查询
    @Test
    public void findUserList() {
        User user = new User();
        user.setName("stt");
        user.setAge(22);
        Example<User> example = Example.of(user);
        List<User> re = repository.findAll(example);
        System.out.println(re);
    }

    @Test
    public void findUserLikeName() {
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) // 字符串包含
                .withIgnoreCase(true); // 忽略大消息
        User user = new User();
        user.setName("st");
        Example<User> example = Example.of(user,exampleMatcher);
        List<User> re = repository.findAll(example);
        System.out.println(re);
    }

    @Test
    public void findUserPage(){
        // 查询条件
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) // 字符串包含
                .withIgnoreCase(true); // 忽略大消息
        User user = new User();
        user.setName("st");
        Example<User> example = Example.of(user,exampleMatcher);

        // 分页参数
        Sort sort  = Sort.by(Sort.Direction.DESC,"age");
        // 0 为第一页
        Pageable pageable = PageRequest.of(0,10,sort);

        Page<User> page = repository.findAll(example, pageable);
        System.out.println(page);
    }

    @Test
    public void updateUser(){
        User user = repository.findById("61acceebdfa8a231d23eda37").get();
        user.setName("stt_new");
        user.setAge(23);
        User newUser = repository.save(user);
        System.out.println(newUser);
    }

    @Test
    public void removeUser(){
        repository.deleteById("61acceebdfa8a231d23eda37");
    }

}
