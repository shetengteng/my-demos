import request from '@/utils/request'

// 医院列表
export function getPageList(current, limit, searchObj) {
  return request({
    url: `/admin/hosp/hospital/list/${current}/${limit}`,
    method: 'get',
    params: searchObj // get 请求中的参数使用params，如果data一般用于post请求的requestBody中
  })
}

// 查询dictCode查询 数据字典子节点
export function findByDictCode(dictCode) {
  return request({
    url: `/admin/cmn/dict/findByDictCode/${dictCode}`,
    method: 'get'
  })
}

// 根据id查询下级数据字典
export function findChildByParentId(id) {
  return request({
    url: `/admin/cmn/dict/findChildData/${id}`,
    method: 'get'
  })
}

export function updateStatus(id, status) {
  return request({
    url: `/admin/hosp/hospital/updateStatus/${id}/${status}`,
    method: 'get'
  })
}

// 查看医院详情
export function getHospById(id) {
  return request({
    url: `/admin/hosp/hospital/show/${id}`,
    method: 'get'
  })
}

// 查看部门列表
export function getDeptList(hoscode) {
  return request({
    url: `/admin/hosp/department/getDeptList/${hoscode}`,
    method: 'get'
  })
}

// 查看排班列表
export function getScheduleRule(page, limit, hoscode, depcode) {
  return request({
    url: `/admin/hosp/schedule/getScheduleRule/${page}/${limit}/${hoscode}/${depcode}`,
    method: 'get'
  })
}

// 查询排班详情
export function getScheduleDetail(hoscode, depcode, workDate) {
  return request({
    url: `/admin/hosp/schedule/detail/${hoscode}/${depcode}/${workDate}`,
    method: 'get'
  })
}
