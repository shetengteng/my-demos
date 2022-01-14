import request from '@/utils/request'

const api_name = '/admin/cmn/dict'

export function findByDictCode(dictCode) {
  return request({
    url: `${api_name}/findByDictCode/${dictCode}`,
    method: 'get'
  })
}

export function findByParentId(parentId) {
  return request({
    url: `${api_name}/findChildData/${parentId}`,
    method: 'get'
  })
}
