<template>
  <!-- header -->
  <div class="nav-container page-component">
    <!--左侧导航 #start -->
    <div class="nav left-nav">
      <div class="nav-item "><span class="v-link clickable dark" @click="toPage('/user')">实名认证</span></div>
      <div class="nav-item "><span class="v-link clickable dark" @click="toPage('/order')">挂号订单</span></div>
      <div class="nav-item selected">
        <span class="v-link selected dark" @click="toPage('/patient')">就诊人管理</span>
      </div>
      <div class="nav-item "><span class="v-link clickable dark">修改账号信息</span></div>
      <div class="nav-item "><span class="v-link clickable dark">意见反馈</span></div>
    </div>
    <!-- 右侧内容 #start -->
    <div class="page-container">
      <div class="personal-patient">
        <div class="title" style="margin-top: 0;font-size: 16px;">就诊人详情</div>
        <div>
          <div class="sub-title"><i class="block"></i>就诊人信息</div>
          <div class="content-wrapper">
            <el-form :model="patient" label-width="110px" label-position="left">
              <el-form-item label="姓名：">{{ patient.name }}</el-form-item>
              <el-form-item label="证件类型：">{{ patient.param.certificatesTypeString }}</el-form-item>
              <el-form-item label="证件号码：">{{ patient.certificatesNo }}</el-form-item>
              <el-form-item label="性别：">{{ patient.sex == 1 ? '男' : '女' }}</el-form-item>
              <el-form-item label="出生日期：">{{ patient.birthdate }}</el-form-item>
              <el-form-item label="手机号码：">{{ patient.phone }}</el-form-item>
              <el-form-item label="婚姻状况：">{{ patient.isMarry == 1 ? '已婚' : '未婚' }}</el-form-item>
              <el-form-item label="当前住址：">
                {{ patient.param.provinceString }}/{{ patient.param.cityString }}/{{ patient.param.districtString }}
              </el-form-item>
              <el-form-item label="详细地址：">{{ patient.address }}</el-form-item>
              <el-form-item>
                <el-button class="v-button" type="primary" @click="remove()">删除就诊人</el-button>
                <el-button class="v-button" type="primary white" @click="edit()">修改就诊人</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div><!-- 右侧内容 #end -->
  </div><!-- footer -->
</template>
<script>
import '~/assets/css/hospital_personal.css'
import '~/assets/css/hospital.css'
import '~/assets/css/personal.css'

import { getById, removeById } from '@/api/user/patient'

export default {
  data() {
    return {
      patient: {
        param: {}
      }
    }
  },
  created() {
    this.fetchDataById()
  },
  methods: {
    toPage(path) {
      window.location = path
    },
    fetchDataById() {
      getById(this.$route.query.id).then(res => {
        this.patient = res.data
      })
    },
    remove() {
      removeById(this.patient.id).then(res => {
        this.$message.success('删除成功')
        window.location.href = '/patient'
      })
    },
    edit() {
      window.location.href = '/patient/add?id=' + this.patient.id
    }
  }
}
</script>
<style>

.content-wrapper {
  color: #333;
  font-size: 14px;
  padding-bottom: 0;
}

</style>
