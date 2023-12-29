package com.ceramider.cnckg.service;


import com.ceramider.cnckg.entity.QAEntityItem;
import com.ceramider.cnckg.query.GraphQuery;
import org.neo4j.driver.v1.StatementResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IKGGraphService {
	Map<String, Object> getProperties(String label, String id);
	StatementResult saveProperties(String label, String id, Map<String, Object> properties);
	/**
	 * 删除Neo4j 标签
	 * 
	 * @param domain
	 */
	void deleteKGdomain(String domain);

	/**
	 * 查询图谱节点和关系
	 * 
	 * @param query
	 * @return node relationship
	 */
	HashMap<String, Object> getdomaingraph(GraphQuery query);
	
	/**
	 * 获取某个领域指定节点拥有的上下级的节点数
	 * 
	 * @param domain
	 * @param nodeid
	 * @return long 数值
	 */
	long getrelationnodecount(String domain, long nodeid);

	/**
	 * 创建领域,默认创建一个新的节点,给节点附上默认属性
	 * 
	 * @param domain
	 */
	void createdomain(String domain);
	void createdomain(String domain,String fileID);

	/**
	 * 获取/展开更多节点,找到和该节点有关系的节点
	 * 
	 * @param domain
	 * @param nodeid
	 * @return
	 */
	HashMap<String, Object> getmorerelationnode(String domain, String nodeid);

	/**
	 * 更新节点名称
	 * 
	 * @param domain
	 * @param nodeid
	 * @param nodename
	 * @return 修改后的节点
	 */
	HashMap<String, Object> updatenodename(String domain, String nodeid, String nodename);

	/**
	 * 创建单个节点
	 * 
	 * @param domain
	 * @param entity
	 * @return
	 */
	HashMap<String, Object> createnode(String domain, QAEntityItem entity);

	/**
	 * 批量创建节点和关系
	 * 
	 * @param domain
	 *            领域
	 * @param sourcename
	 *            源节点
	 * @param relation
	 *            关系
	 * @param targetnames
	 *            目标节点数组
	 * @return
	 */
	HashMap<String, Object> batchcreatenode(String domain, String sourcename, String relation, String[] targetnames);

	/**
	 * 批量创建下级节点
	 * 
	 * @param domain
	 *            领域
	 * @param sourceid
	 *            源节点id
	 * @param entitytype
	 *            节点类型
	 * @param targetnames
	 *            目标节点名称数组
	 * @param relation
	 *            关系
	 * @return
	 */
	HashMap<String, Object> batchcreatechildnode(String domain, String sourceid, Integer entitytype,
			String[] targetnames, String relation);

	/**
	 * 批量创建同级节点
	 * 
	 * @param domain
	 *            领域
	 * @param entitytype
	 *            节点类型
	 * @param sourcenames
	 *            节点名称
	 * @return
	 */
	List<HashMap<String, Object>> batchcreatesamenode(String domain, Integer entitytype, String[] sourcenames);

	/**
	 * 添加关系
	 * 
	 * @param domain
	 *            领域
	 * @param sourceid
	 *            源节点id
	 * @param targetid
	 *            目标节点id
	 * @param ship
	 *            关系
	 * @return
	 */
	HashMap<String, Object> createlink(String domain, long sourceid, long targetid, String ship);

	/**
	 * 更新关系
	 * 
	 * @param domain
	 *            领域
	 * @param shipid
	 *            关系id
	 * @param shipname
	 *            关系名称
	 * @return
	 */
	HashMap<String, Object> updatelink(String domain, long shipid, String shipname);

	/**
	 * 删除节点(先删除关系再删除节点)
	 * 
	 * @param domain
	 * @param nodeid
	 * @return
	 */
	List<HashMap<String, Object>> deletenode(String domain, long nodeid);

	/**
	 * 删除关系
	 * 
	 * @param domain
	 * @param shipid
	 */
	void deletelink(String domain, long shipid);


	void updateCorrdOfNode(String domain, String uuid, Double fx, Double fy);
}
