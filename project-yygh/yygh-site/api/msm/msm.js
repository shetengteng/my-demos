import request from '~/utils/request'

const api_name = `/api/msm`

export function sendCode(mobile) {
  return request({
    url: `${api_name}/send/${mobile}`,
    method: `get`
  })
}


