import request from '~/utils/request'

const api_name = `/api/hosp/hospital`

export function getPageList(page, limit, searchObj) {
  return request({
    url: `${api_name}/${page}/${limit}`,
    method: 'get',
    params: searchObj
  })
}

export function getByHosname(hosname) {
  return request({
    url: `${api_name}/findByHosname/${hosname}`,
    method: 'get'
  })
}

export function detail(hoscode) {
  return request({
    url: `${api_name}/${hoscode}`,
    method: 'get'
  })
}

export function findDepartment(hoscode) {
  return request({
    url: `${api_name}/department/${hoscode}`,
    method: 'get'
  })
}

export function getBookingScheduleRule(page, limit, hoscode, depcode) {
  return request({
    url: `${api_name}/auth/getBookingScheduleRule/${page}/${limit}/${hoscode}/${depcode}`,
    method: 'get'
  })
}

export function findScheduleList(hoscode, depcode, workDate) {
  return request({
    url: `${api_name}/auth/findScheduleList/${hoscode}/${depcode}/${workDate}`,
    method: 'get'
  })
}


export function getSchedule(id) {
  return request({
    url: `${api_name}/getSchedule/${id}`,
    method: 'get'
  })
}
