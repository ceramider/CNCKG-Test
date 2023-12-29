import { baseUrl } from '../utils/global'

export default {
  method: 'get',
  // 基础url前缀
  baseUrl: baseUrl,
  // 请求头信息
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
    "Access-Control-Allow-Headers":"Authorization,Origin, X-Requested-With, Content-Type, Accept",
    "Access-Control-Allow-Methods":"GET,POST",
    "Access-Control-Allow-Origin": "*"
  },
  // 参数
  data: {},
  // 设置超时时间
  // timeout: 10000,
  timeout: 100000,
  // 携带凭证
  withCredentials: true,
  // 返回数据类型
  responseType: 'json'
}
