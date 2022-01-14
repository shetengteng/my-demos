import request from '@/utils/request'

export function createNative(orderId) {
  return request({
    url: `/api/order/weixin/createNative/${orderId}`,
    method: 'get'
  })
}

export function queryPayStatus(orderId) {
  return request({
    url: `/api/order/weixin/queryPayStatus/${orderId}`,
    method: 'get'
  })
}

export function cancelOrder(orderId) {
  return request({
    url: `/api/order/orderInfo/auth/cancelOrder/${orderId}`,
    method: 'get'
  })
}

