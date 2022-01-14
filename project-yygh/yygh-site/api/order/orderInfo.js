import request from '@/utils/request'

const api_name = `/api/order/orderInfo`

export function submitOrder(scheduleId, patientId) {
  return request({
    url: `${api_name}/auth/submitOrder/${scheduleId}/${patientId}`,
    method: 'post'
  })
}


//订单列表
export function getPageList(page, limit, searchObj) {
  return request({
    url: `${api_name}/auth/${page}/${limit}`,
    method: `get`,
    params: searchObj
  })
}

//订单状态
export function getStatusList() {
  return request({
    url: `${api_name}/auth/getStatusList`,
    method: 'get'
  })
}


//订单详情
export function  getOrders(orderId) {
  return request({
    url: `${api_name}/auth/getOrders/${orderId}`,
    method: `get`
  })
}
