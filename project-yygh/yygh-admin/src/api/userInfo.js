import request from '@/utils/request'

const api_name = '/admin/user'

export function getPageList(page, limit, searchObj) {
  return request({
    url: `${api_name}/${page}/${limit}`,
    method: 'get',
    params: searchObj
  })
}

export function lock(id, status) {
  return request({
    url: `${api_name}/lock/${id}/${status}`,
    method: 'get'
  })
}

// 用户详情
export function show(id) {
  return request({
    url: `${api_name}/show/${id}`,
    method: 'get'
  })
}

// 认证审批
export function approve(id, authStatus) {
  return request({
    url: `${api_name}/approve/${id}/${authStatus}`,
    method: 'get'
  })
}

