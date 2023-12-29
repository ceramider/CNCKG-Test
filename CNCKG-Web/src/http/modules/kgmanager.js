import axios from '../axios'

export const getCypherResult = (data) => {
  return axios({
    url: '/getcypherresult',
    method: 'post',
    params: data
  })
}

export const createDomain = (data) => {
  return axios({
    url: '/createdomain',
    method: 'post',
    params: data
  })
}

export const getGraph = (data) => {
  return axios({
    url: '/getgraph',
    method: 'post',
    params: data
  })
}

export const getDomainGraph = (data) => {
  return axios({
    url: '/getdomaingraph',
    method: 'post',
    params: data
  })
}

export const deleteDomain = (data) => {
  return axios({
    url: '/deletedomain',
    method: 'post',
    params: data
  })
}

export const saveProperties = (data) => {
  return axios({
    url: '/saveProperties',
    method: 'post',
    params: data
  })
}

export const createNode = (data) => {
  return axios({
    url: '/createnode',
    method: 'post',
    params: data
  })
}

export const getProperties = (data) => {
  return axios({
    url: '/getProperties',
    method: 'get',
    params: data
  })
}

export const getMoreRelationNode = (data) => {
  return axios({
    url: '/getmorerelationnode',
    method: 'post',
    params: data
  })
}

export const getRelationNodeCount = (data) => {
  return axios({
    url: '/getrelationnodecount',
    method: 'post',
    params: data
  })
}

export const deleteNode = (data) => {
  return axios({
    url: '/deletenode',
    method: 'post',
    params: data
  })
}

export const deleteLink = (data) => {
  return axios({
    url: '/deletelink',
    method: 'post',
    params: data
  })
}

export const createLink = (data) => {
  return axios({
    url: '/createlink',
    method: 'post',
    params: data
  })
}

export const updateLink = (data) => {
  return axios({
    url: '/updatelink',
    method: 'post',
    params: data
  })
}

export const updateNodeName = (data) => {
  return axios({
    url: '/updatenodename',
    method: 'post',
    params: data
  })
}
