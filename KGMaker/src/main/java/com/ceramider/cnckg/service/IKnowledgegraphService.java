package com.ceramider.cnckg.service;


import java.util.List;
import java.util.Map;

public interface IKnowledgegraphService {
	List<Map<String,Object>> getDomains();
	List<Map<String,Object>> getDomainList(String domainname,String createuser,String fileID);
	void saveDomain(Map<String, Object> map);
	void updateDomain(Map<String, Object> map);
	void deleteDomain(Integer id);
	List<Map<String,Object>> getDomainByName(String domainname,String fileID);
	List<Map<String,Object>> getDomainById(Integer domainid);
}
