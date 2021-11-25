package com.stt.demo.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stt.demo.mybatisplus.entity.User;
import com.stt.demo.mybatisplus.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DemoMybatisPlusApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testFindAll() {
        List<User> users = userMapper.selectList(null);
        System.out.println(users);
    }

    @Test
    public void testAddUser() {
        User user = new User();
        user.setName("stt2");
        user.setAge(22);
        user.setEmail("work_stt@163.com");
        int count = userMapper.insert(user);
        System.out.println(count);
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setId(1463493922784559105L);
        user.setName("stt-new");
        int count = userMapper.updateById(user);
        System.out.println(count);
    }

    @Test
    public void testOptimisticLocker() {
        User user = userMapper.selectById(1463512055645134850L);
        user.setName("stt2");
        userMapper.updateById(user);
    }

    // 批量查询
    @Test
    public void testBatchSelectById() {
        List<User> users = userMapper.selectBatchIds(Arrays.asList(1L, 2L, 3L));
        System.out.println(users);
    }

    @Test
    public void testSelectByParamMap() {
        // SELECT id,name,age,email,create_time,update_time,version FROM user WHERE name = ? AND age = ?
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("name", "stt");
        columnMap.put("age", 22);
        List<User> users = userMapper.selectByMap(columnMap);
        System.out.println(users);
    }

    @Test
    public void testPageSelect() {
        // 传入当前页，每页显示的条数
        Page<User> page = new Page(1, 3);
        Page<User> re = userMapper.selectPage(page, null);
        System.out.println(re.getPages()); // 总页数
        System.out.println(re.getTotal()); // 总记录数
        System.out.println(re.hasNext()); // 是否有下一页
        System.out.println(re.hasPrevious()); // 是否有上一页
        System.out.println(re.getCurrent()); // 当前页
        System.out.println(re.getRecords()); // 详细记录列表
    }

    @Test
    public void testSelectMapsPage() {
        //Page不需要泛型
        Page<Map<String, Object>> page = new Page<>(1, 5);
        Page<Map<String, Object>> pageParam = userMapper.selectMapsPage(page, null);
        List<Map<String, Object>> records = pageParam.getRecords();
        records.forEach(System.out::println);
        System.out.println(pageParam.getCurrent());
        System.out.println(pageParam.getPages());
        System.out.println(pageParam.getSize());
        System.out.println(pageParam.getTotal());
        System.out.println(pageParam.hasNext());
        System.out.println(pageParam.hasPrevious());
    }

    @Test
    public void testDeleteById() {
        int count = userMapper.deleteById(1463493922784559105L);
        System.out.println(count);
    }

    @Test
    public void testDeleteBatchIds() {
        int count = userMapper.deleteBatchIds(Arrays.asList(8, 9, 10));
        System.out.println(count);
    }

    @Test
    public void testDeleteByMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "stt");
        map.put("age", 22);
        int result = userMapper.deleteByMap(map);
        System.out.println(result);
    }


    @Test
    public void testWrapperQuery1() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .ge("age", 22)
                .isNotNull("email");
        List<User> users = userMapper.selectList(queryWrapper);
        System.out.println(users);
    }

    @Test
    public void testWrapperQuery2() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", "work_stt@163.com");
        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);
    }

    @Test
    public void testWrapperQuery3() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("age", 18, 20);
        Integer count = userMapper.selectCount(queryWrapper);
        System.out.println(count);
    }

    @Test
    public void testWrapperQuery4() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .select("name", "age")
                .like("name", "st") // like '%st%'
                .likeRight("email", "work_"); // like 'work_%'
        List<Map<String, Object>> maps = userMapper.selectMaps(queryWrapper);
        maps.forEach(System.out::println);
    }

    @Test
    public void testWrapperQuery5(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("age","id");
        List<User> users = userMapper.selectList(queryWrapper);
        System.out.println(users);
    }

}
