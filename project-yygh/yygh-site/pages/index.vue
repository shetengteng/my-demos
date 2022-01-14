<template>
  <div class="home page-component">
    <el-carousel indicator-position="outside">
      <el-carousel-item v-for="item in 2" :key="item">
        <img src="~assets/images/web-banner1.png" alt="">
      </el-carousel-item>
    </el-carousel>
    <!-- 搜索 -->
    <div class="search-container">
      <div class="search-wrapper">
        <div class="hospital-search">
          <el-autocomplete class="search-input" prefix-icon="el-icon-search" v-model="state"
                           :fetch-suggestions="querySearchAsync" placeholder="点击输入医院名称"
                           @select="handleSelect">
            <span slot="suffix" class="search-btn v-link highlight clickable selected">搜索</span>
          </el-autocomplete>
        </div>
      </div>
    </div>
    <!-- bottom -->
    <div class="bottom">
      <div class="left">
        <div class="home-filter-wrapper">
          <div class="title">医院</div>
          <div>
            <div class="filter-wrapper">
              <span class="label">等级：</span>
              <div class="condition-wrapper">
                <span class="item v-link clickable" :class="hostypeActiveIndex === index ? 'highlight selected':''"
                      v-for="(item,index) in hostypeList" @click="hostypeSelect(item.value,index)"
                      :key="index">
                  {{ item.name }}
                </span>
              </div>
            </div>
            <div class="filter-wrapper">
              <span class="label">地区：</span>
              <div class="condition-wrapper">
                 <span class="item v-link clickable" :class="provinceActiveIndex === index ? 'highlight selected':''"
                       v-for="(item,index) in districtList" @click="districtSelect(item.value,index)"
                       :key="index">
                  {{ item.name }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="v-scroll-list hospital-list">
          <div class="v-card clickable list-item space"
               v-for="(item,index) in list" @click="show(item.hoscode)" :key="index">
            <div class="">
              <div class="hospital-list-item hos-item" :index="index">
                <div class="wrapper">
                  <div class="hospital-title">{{ item.hosname }}</div>
                  <div class="bottom-container">
                    <div class="icon-wrapper"><span class="iconfont"></span>{{ item.param.hostypeString }}</div>
                    <div class="icon-wrapper"><span class="iconfont"></span>每天{{ item.bookingRule.releaseTime }}放号
                    </div>
                  </div>
                </div>
                <img :src="imageBase64(item)" :alt="item.hosname" class="hospital-img">
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="right">
        <div class="common-dept">
          <div class="header-wrapper">
            <div class="title">常见科室</div>
            <div class="all-wrapper"><span>全部</span><span class="iconfont icon"></span></div>
          </div>
          <div class="content-wrapper">
            <span class="item v-link clickable dark">神经内科 </span>
            <span class="item v-link clickable dark">消化内科 </span>
            <span class="item v-link clickable dark">呼吸内科 </span>
            <span class="item v-link clickable dark">内科 </span>
            <span class="item v-link clickable dark">神经外科 </span>
            <span class="item v-link clickable dark">妇科 </span>
            <span class="item v-link clickable dark"> 产科 </span>
            <span class="item v-link clickable dark">儿科 </span>
          </div>
        </div>
        <div class="space">
          <div class="header-wrapper">
            <div class="title-wrapper">
              <div class="icon-wrapper"><span class="iconfont title-icon"></span></div>
              <span class="title">平台公告</span>
            </div>
            <div class="all-wrapper"><span>全部</span><span class="iconfont icon"></span></div>
          </div>
          <div class="content-wrapper">
            <div class="notice-wrapper">
              <div class="point"></div>
              <span class="notice v-link clickable dark">关于延长北京大学国际医院放假的通知</span>
            </div>
            <div class="notice-wrapper">
              <div class="point"></div>
              <span class="notice v-link clickable dark">北京中医药大学东方医院部分科室医生门诊医</span>
            </div>
            <div class="notice-wrapper">
              <div class="point"></div>
              <span class="notice v-link clickable dark">武警总医院号源暂停更新通知</span>
            </div>
          </div>
        </div>
        <div class="suspend-notice-list space">
          <div class="header-wrapper">
            <div class="title-wrapper">
              <div class="icon-wrapper">
                <span class="iconfont title-icon"></span>
              </div>
              <span class="title">停诊公告</span>
            </div>
            <div class="all-wrapper">
              <span>全部</span>
              <span class="iconfont icon"></span>
            </div>
          </div>
          <div class="content-wrapper">
            <div class="notice-wrapper">
              <div class="point"></div>
              <span class="notice v-link clickable dark">中国人民解放军总医院第六医学中心(原海军总医院)呼吸内科门诊停诊公告 </span>
            </div>
            <div class="notice-wrapper">
              <div class="point"></div>
              <span class="notice v-link clickable dark">首都医科大学附属北京潞河医院老年医学科门诊停诊公告 </span>
            </div>
            <div class="notice-wrapper">
              <div class="point"></div>
              <span class="notice v-link clickable dark">中日友好医院中西医结合心内科门诊停诊公告 </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { findByDictCode } from '~/api/cmn/dict'
import { getByHosname, getPageList } from '~/api/hosp/hosp'

export default {
  // nuxt 对vue的增强，在渲染页面之前调用
  asyncData({ params, error }) {
    return getPageList(1, 10, null).then(res => {
      return {
        list: res.data.content, // 等价于 data 中有list
        pages: res.data.totalPages // 等价于 data 中有 pages
      }
    })
  },
  data() {
    return {
      state: '',
      searchObj: {},
      page: 1,
      limit: 10,

      hosname: '', //医院名称
      hostypeList: [], //医院等级集合
      districtList: [], //地区集合

      hostypeActiveIndex: 0,
      provinceActiveIndex: 0
    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      //查询医院等级列表
      findByDictCode('Hostype')
        .then(res => {
          //向hostypeList添加 【全部】
          //把接口返回数据，添加到hostypeList
          this.hostypeList = [{ 'name': '全部', 'value': '' }, ...res.data]
        })
      //查询地区数据
      findByDictCode('Beijing')
        .then(res => {
          this.districtList = [{ 'name': '全部', 'value': '' }, ...res.data]
        })
    },
    //查询医院列表
    getList() {
      getPageList(this.page, this.limit, this.searchObj)
        .then(res => {
          this.list = [...res.data.content]
          this.page = res.data.totalPages
        })
    },
    //根据医院等级查询
    hostypeSelect(hostype, index) {
      //准备数据
      this.list = []
      this.page = 1
      this.hostypeActiveIndex = index
      this.searchObj.hostype = hostype
      //调用查询医院列表方法
      this.getList()
    },
    //根据地区查询医院
    districtSelect(districtCode, index) {
      this.list = []
      this.page = 1
      this.provinceActiveIndex = index
      this.searchObj.districtCode = districtCode
      this.getList()
    },
    //在输入框输入值，弹出下拉框，显示相关内容
    querySearchAsync(queryString, cb) {
      this.searchObj = []
      if (queryString === '') return
      getByHosname(queryString).then(res => {
        for (let i = 0, len = res.data.length; i < len; i++) {
          res.data[i].value = res.data[i].hosname
        }
        cb(res.data)
      })
    },
    imageBase64(item) {
      return 'data:image/jpeg;base64,' + item.logoData
    },
    //在下拉框选择某一个内容，执行下面方法，跳转到详情页面中
    handleSelect(item) {
      window.location.href = '/hospital/' + item.hoscode
    },
    //点击某个医院名称，跳转到详情页面中
    show(hoscode) {
      window.location.href = '/hospital/' + hoscode
    }
  }
}
</script>
