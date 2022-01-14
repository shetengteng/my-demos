<template>
  <div class="app-container">
    <div class="el-toolbar">
      <div class="el-toolbar-body" style="justify-content: flex-start;">
        <a href="http://localhost:8202/admin/cmn/dict/exportData" target="_blank">
          <el-button type="text"><i class="fa fa-plus"/> 导出</el-button>
        </a>
        <el-button style="margin-left: 10px;" type="text" @click="importData"><i class="fa fa-plus"/> 导入</el-button>
      </div>
    </div>
    <el-table :data="list" style="width: 100%" row-key="id" border lazy :load="loadChildrens"
              :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column label="名称" prop="name" width="230" align="left"/>
      <el-table-column label="编码" width="220">
        <template slot-scope="{row}">{{ row.dictCode }}</template>
      </el-table-column>
      <el-table-column label="值" prop="value" width="230" align="left"/>
      <el-table-column label="创建时间" prop="createTime" align="center"/>
    </el-table>
    <el-dialog title="导入" :visible.sync="dialogImportVisible" width="480px">
      <el-form label-position="right" label-width="170px">
        <el-form-item label="文件">
          <el-upload :multiple="false" :on-success="onUploadSuccess"
                     :action="'http://localhost:8202/admin/cmn/dict/importData'">
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">只能上传xls文件，且不超过500kb</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogImportVisible = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { dictList } from '@/api/dict'

export default {
  data() {
    return {
      list: [],
      dialogImportVisible: false
    }
  },
  created() {
    this.getDictList(1)
  },
  methods: {
    importData() {
      this.dialogImportVisible = true
    },
    onUploadSuccess(res, file) {
      this.$message.info('上传成功')
      this.dialogImportVisible = false
      this.getDictList(1)
    },
    // 数据字典列表
    getDictList(_id) {
      dictList(_id).then(res => {
        this.list = res.data
      })
    },
    // elementui 中的查看子节点回调函数
    loadChildrens(tree, treeNode, resolve) {
      dictList(tree.id).then(res => {
        // 将子节点的数据返回给table当前选中行，用于展开
        resolve(res.data)
      })
    }
  }
}
</script>
