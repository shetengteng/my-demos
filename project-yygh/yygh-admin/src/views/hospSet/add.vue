<template>
  <div class="app-container">
    <h2>医院设置添加</h2>
    <el-form label-width="120px">
      <el-form-item label="医院名称">
        <el-input v-model="hospitalSet.hosname"/>
      </el-form-item>
      <el-form-item label="医院编号">
        <el-input v-model="hospitalSet.hoscode"/>
      </el-form-item>
      <el-form-item label="api基础路径">
        <el-input v-model="hospitalSet.apiUrl"/>
      </el-form-item>
      <el-form-item label="联系人姓名">
        <el-input v-model="hospitalSet.contactsName"/>
      </el-form-item>
      <el-form-item label="联系人手机">
        <el-input v-model="hospitalSet.contactsPhone"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveOrUpdate">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>

import hospset from '@/api/hospset'

export default {
  data() {
    return {
      hospitalSet: {
        id: '',
        hosname: '',
        hoscode: '',
        apiUrl: '',
        contactsName: '',
        contactsPhone: ''
      }
    }
  },
  created() {
    // 获取路由id值 调用接口得到医院设置信息
    if (this.$route.params && this.$route.params.id) {
      const id = this.$route.params.id
      this.getHostSet(id)
    }
  },
  methods: {
    // 根据id查询
    getHostSet(id) {
      hospset.getHospSet(id)
        .then(res => {
          this.hospitalSet = res.data
        })
    },
    saveOrUpdate() {
      // 判断添加还是修改 , 注意，使用 this.hospitalSet.id == null 的情况是避免 id=0情况下的逻辑错误
      if (this.hospitalSet.id == null || this.hospitalSet.id === '') {
        // 没有id，做添加
        this.save()
      } else {
        this.update()
      }
    },
    save() {
      hospset.saveHospSet(this.hospitalSet)
        .then(res => {
          this.$message({ type: 'success', message: '添加成功!' })
          // 跳转列表页面，使用路由跳转方式实现
          this.$router.push({ path: '/hospSet/list' })
        })
    },
    update() {
      hospset.updateHospSet(this.hospitalSet)
        .then(res => {
          this.$message({ type: 'success', message: '修改成功!' })
          this.$router.push({ path: '/hospSet/list' })
        })
    }
  }
}
</script>
