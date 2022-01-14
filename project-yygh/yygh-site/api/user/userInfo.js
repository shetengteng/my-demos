import request from '~/utils/request'

const api_name = `/api/user`

export function login(userInfo) {
  return request({
    url: `${api_name}/login`,
    method: `post`,
    data: userInfo
  })
}

export function getUserInfo() {
  return request({
    url: `${api_name}/auth/getUserInfo`,
    method: `get`
  })
}

export function saveUserAuth(userAuth) {
  return request({
    url: `${api_name}/auth/userAuth`,
    method: 'post',
    data: userAuth
  })
}

