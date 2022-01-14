<template>
  <div class="app-container">
    <!-- :inline表示在一行显示 -->
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item>
        <el-input v-model="searchObj.hosname" placeholder="医院名称"></el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="searchObj.hoscode" placeholder="医院编号"></el-input>
      </el-form-item>
      <el-button type="primary" icon="el-icon-search" @click="fetchList">查询</el-button>
    </el-form>

    <!-- 工具条 -->
    <div>
      <el-button type="danger" size="mini" @click="removeRows()">批量删除</el-button>
    </div>

    <!-- banner列表 -->
    <el-table :data="list" stripe style="width: 100%" @selection-change="handleSelectionChange">
      <!-- 多选列 -->
      <el-table-column type="selection" width="55"/>
      <el-table-column type="index" width="50"/>
      <el-table-column prop="hosname" label="医院名称"/>
      <el-table-column prop="hoscode" label="医院编号"/>
      <el-table-column prop="apiUrl" label="api基础路径" width="200"/>
      <el-table-column prop="contactsName" label="联系人姓名"/>
      <el-table-column prop="contactsPhone" label="联系人手机"/>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          {{ scope.row.status === 1 ? '可用' : '不可用' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center">
        <template slot-scope="scope">
          <el-button type="danger" size="mini" icon="el-icon-delete" @click="removeDataById(scope.row.id)">删除
          </el-button>
          <el-button v-if="scope.row.status==1" type="primary" size="mini" icon="el-icon-delete"
                     @click="lockHostSet(scope.row.id,0)">锁定
          </el-button>
          <el-button v-if="scope.row.status==0" type="danger" size="mini" icon="el-icon-delete"
                     @click="lockHostSet(scope.row.id,1)">取消锁定
          </el-button>
          <router-link :to="'/hospSet/edit/'+scope.row.id">
            <el-button style="margin-left: 10px;" type="primary" size="mini" icon="el-icon-edit"></el-button>
          </router-link>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <el-pagination
      :current-page.sync="current"
      :page-size="limit"
      :total="total"
      style="padding: 30px 0; text-align: center;"
      layout="total, prev, pager, next, jumper"
      @current-change="fetchList"/>
  </div>
</template>

<script>

import hospset from '@/api/hospset'

export default {
  // 定义变量和初始值
  data() {
    return {
      current: 1, // 定义当前页
      limit: 1, // 定义每页记录数
      searchObj: { hosname: '', hoscode: '' }, // 条件封装的对象
      list: [], // 每页数据的集合
      total: 0, // 总记录数
      multipleSelection: [] // 多选项
    }
  },
  created() { // 在页面渲染之前
    // 调用methods中的方法，得到数据
    this.fetchList()
  },
  methods: {
    fetchList() {
      hospset.getHospSetList(this.current, this.limit, this.searchObj)
        .then(res => { // response 返回的数据
          console.log(res)
          // 记录的列表
          this.list = res.data.records
          // 总记录数
          this.total = res.data.total
        }) // 请求成功
        .catch(error => {
          console.error(error)
        }) // 请求失败
    },
    removeDataById(_id) {
      this.$confirm('此操作将永久删除医院是设置信息, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => { // 确定执行then方法
        hospset.deleteHospSet(_id) // 调用接口
          .then(res => {
            // 提示
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.refreshTable()
          })
      })
    },
    handleSelectionChange(selection) {
      this.multipleSelection = selection
    },
    removeRows() {
      this.$confirm('此操作将永久删除医院是设置信息, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const idList = []
        // 遍历数组得到每个id值，设置到idList里面
        for (let i = 0; i < this.multipleSelection.length; i++) {
          idList.push(this.multipleSelection[i].id)
        }
        // 调用接口
        hospset.batchDeleteHospSet(idList)
          .then(res => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.refreshTable()
          })
      })
    },
    lockHostSet(id, status) { // 锁定和取消锁定
      hospset.lockHospSet(id, status)
        .then(response => {
          this.fetchList()
        })
    },
    refreshTable() {
      // 刷新页面
      this.current = 1
      this.fetchList()
    }
  }
}
</script>
