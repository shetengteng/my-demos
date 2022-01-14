<template>
  <div></div>
</template>
<script>
export default {
  layout: 'empty', // 指定布局，指定empty
  data() {
    return {}
  },
  mounted() {
    // 读取url中的参数，注意使用this.$route.query对象读取 url中?后面的参数
    let token = this.$route.query.token
    let name = this.$route.query.name
    let openid = this.$route.query.openid
    let hasPhone = this.$route.query.hasPhone
    // 调用父vue方法，实际上可以将loginEvent抽取成公共模块
    window.parent.loginEvent.$emit('loginCallback', name, token, openid, hasPhone)
  }
}
</script>
