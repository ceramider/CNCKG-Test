package com.ceramider.cnckg.dal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IKnowledgegraphRepository {
	List<Map<String,Object>> getDomains();
	List<Map<String,Object>> getDomainList(@Param("domainname")String domainname,@Param("createuser")String createuser,@Param("fileID")String fileID);
	void saveDomain(@Param("params") Map<String, Object> map);
	void updateDomain(@Param("params") Map<String, Object> map);
	void deleteDomain(@Param("id") Integer id);
	List<Map<String,Object>> getDomainByName(@Param("domainname") String domainname,@Param("fileID")String fileID);
	List<Map<String,Object>> getDomainById(@Param("domainid")Integer domainid);

	}
