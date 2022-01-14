package com.stt.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.stt.yygh.hosp.repository.DepartmentRepository;
import com.stt.yygh.hosp.service.DepartmentService;
import com.stt.yygh.model.hosp.Department;
import com.stt.yygh.vo.hosp.DepartmentQueryVo;
import com.stt.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository repository;

    @Override
    public void save(Map<String, Object> paramMap) {
        Department department = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Department.class);
        Department targetDepartment = repository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        // 表示不存在该科室，则添加
        if (Objects.isNull(targetDepartment)) {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            repository.save(department);
        } else {
            BeanUtils.copyProperties(department, targetDepartment, Department.class);
            targetDepartment.setUpdateTime(new Date());
            targetDepartment.setIsDeleted(0);
            repository.save(targetDepartment);
        }
    }

    @Override
    public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        // 分页参数
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        // 创建匹配器，条件查询
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        // 查询参数
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);
        Example<Department> example = Example.of(department, exampleMatcher);

        Page<Department> re = repository.findAll(example, pageable);
        return re;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        Department department = repository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (Objects.isNull(department)) {
            return;
        }
        repository.deleteById(department.getId());
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        // 通过医院编号查询所有科室信息
        Department d = new Department();
        d.setHoscode(hoscode);
        List<Department> all = repository.findAll(Example.of(d));

        // 通过科室bigcode进行分组
        Map<String, List<Department>> departmentMap = all.stream()
                .collect(Collectors.groupingBy(Department::getBigcode));

        List<DepartmentVo> re = new ArrayList<>();
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            // 大科室编号
            String bigcode = entry.getKey();
            // 封装大科室输出对象
            DepartmentVo parent = new DepartmentVo();
            parent.setDepcode(bigcode);
            parent.setDepname(entry.getValue().get(0).getDepname());
            parent.setChildren(new ArrayList<>());
            // 封装小科室
            for (Department department : entry.getValue()) {
                DepartmentVo subVo = new DepartmentVo();
                subVo.setDepname(department.getDepname());
                subVo.setDepcode(department.getDepcode());
                parent.getChildren().add(subVo);
            }
            re.add(parent);
        }
        return re;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = repository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        return Objects.isNull(department) ? "" : department.getDepname();
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return repository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}