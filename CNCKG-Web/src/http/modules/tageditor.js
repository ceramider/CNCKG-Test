import axios from '../axios'

export const getFileIDByLabel = (data) => {
  return axios({
    url: '/getFileIDByLabel',
    method: 'post',
    params: data
  })
}

export const deleteFileLabel = (data) => {
  return axios({
    url: '/deleteFileLabel',
    method: 'post',
    params: data
  })
}

export const getLabels = (data) => {
  return axios({
    url: '/getLabels',
    method: 'post',
    params: data
  })
}

export const createLabel = (data) => {
  return axios({
    url: '/createLabel',
    method: 'post',
    params: data
  })
}

export const getRelatedLabels = (data) => {
  return axios({
    url: '/getRelatedLabels',
    method: 'post',
    params: data
  })
}
