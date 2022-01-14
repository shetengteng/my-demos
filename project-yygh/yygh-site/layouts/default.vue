<template>
  <div class="app-container">
    <div id="main">
      <!-- 公共头 -->
      <my-header/>
      <div class="main-container">
        <el-scrollbar class='page-component__scroll'>
          <!-- 内容区域 -->
          <nuxt/>
        </el-scrollbar>
      </div>
      <!-- 公共底 -->
      <my-footer/>
    </div>
  </div>
</template>
<script>
// 引入样式
import '~/assets/css/app.css'
import '~/assets/css/chunk.css'
import '~/assets/css/iconfont.css'
import '~/assets/css/main.css'

// 引入组件
import myheader from './my-header'
import myfooter from './my-footer'

export default {
  components: {
    'my-header': myheader,
    'my-footer': myfooter
  }
}
</script>

