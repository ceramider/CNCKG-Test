package com.ceramider.cnckg.dal.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.ceramider.cnckg.dal.IKGraphRepository;
import com.ceramider.cnckg.entity.QAEntityItem;
import com.ceramider.cnckg.util.Neo4jUtil;
import com.ceramider.cnckg.query.GraphQuery;
import com.ceramider.cnckg.util.StringUtil;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

@Repository
public class KGraphRepository implements IKGraphRepository {
    @Autowired
    private Neo4jUtil neo4jUtil;
    
    
    /**
     * 删除Neo4j 标签
     *
     * @param domain
     */
    @Override
    public void deleteKGdomain(String domain) {
        try {
            String rSql = String.format("MATCH (n:`%s`) -[r]-(m)  delete r", domain);
            neo4jUtil.excuteCypherSql(rSql);
            String deleteNodeSql = String.format("MATCH (n:`%s`) delete n", domain);
            neo4jUtil.excuteCypherSql(deleteNodeSql);
            
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    
    /**
     * 查询图谱节点和关系
     *
     * @param query
     * @return node relationship
     */
    @Override
    // todo fileId
    public HashMap<String, Object> getdomaingraph(GraphQuery query) {
        HashMap<String, Object> nr = new HashMap<String, Object>();
        try {
            String domain = query.getDomain();
            // MATCH (n:`症状`) -[r]-(m:症状) where r.name='治疗' or r.name='危险因素' return n,m
            if (!StringUtil.isBlank(domain)) {
                String cqr = "";
                List<String> lis = new ArrayList<String>();
                if (query.getRelation() != null && query.getRelation().length > 0) {
                    for (String r : query.getRelation()) {
                        String it = String.format("r.name='%s'", r);
                        lis.add(it);
                    }
                    cqr = String.join(" or ", lis);
                }
                String cqWhere = "";
                if (!StringUtil.isBlank(query.getNodename()) || !StringUtil.isBlank(cqr) || !StringUtil.isBlank(query.getFileID())) {
                    
                    if (!StringUtil.isBlank(query.getNodename())) {
                        if (query.getMatchtype() == 1) {
                            cqWhere = String.format("where n.name ='%s' ", query.getNodename());
                            
                        } else {
                            cqWhere = String.format("where n.name contains('%s')", query.getNodename());
                        }
                    }
                    
                    if (!StringUtil.isBlank(cqr)) {
                        if (StringUtil.isBlank(cqWhere)) {
                            cqWhere = String.format(" where ( %s )", cqr);
                            
                        } else {
                            cqWhere += String.format(" and ( %s )", cqr);
                        }
                        
                    }
                    if (!StringUtil.isBlank(query.getFileID())) {
                        String s = String.format("n.fileID = %s", query.getFileID());
                        if (StringUtil.isBlank(cqWhere)) {
                            cqWhere = String.format(" where ( %s )", s);
                            
                        } else {
                            cqWhere += String.format(" and ( %s )", s);
                        }
                    }
                    String nodeOnly = cqWhere;
                    // 下边的查询查不到单个没有关系的节点,考虑要不要左箭头
                    String nodeSql = String.format("MATCH (n:`%s`) <-[r]->(m) %s return * limit %s", domain, cqWhere,
                            query.getPageSize());
                    if ("ALL".equals(domain)) {
                        nodeSql = String.format("MATCH (n) <-[r]->(m) %s return * limit %s", cqWhere,
                                query.getPageSize());
                    }
                    HashMap<String, Object> graphNode = neo4jUtil.GetGraphNodeAndShip(nodeSql);
                    Object node = graphNode.get("node");
                    // 没有关系显示则显示节点
                    if (node != null) {
                        nr.put("node", graphNode.get("node"));
                        nr.put("relationship", graphNode.get("relationship"));
                    } else {
                        String nodecql = String.format("MATCH (n:`%s`) %s RETURN distinct(n) limit %s", domain,
                                nodeOnly, query.getPageSize());
                        if ("ALL".equals(domain)) {
                            nodecql = String.format("MATCH (n) %s RETURN distinct(n) limit %s",
                                    nodeOnly, query.getPageSize());
                        }
                        List<HashMap<String, Object>> nodeItem = neo4jUtil.GetGraphNode(nodecql);
                        nr.put("node", nodeItem);
                        nr.put("relationship", new ArrayList<HashMap<String, Object>>());
                    }
                } else {
                    String nodeSql;
                    if ("ALL".equals(domain)) {
                        nodeSql = String.format("MATCH (n) %s RETURN distinct(n) limit %s", cqWhere,
                                query.getPageSize());
                    } else {
                        nodeSql = String.format("MATCH (n:`%s`) %s RETURN distinct(n) limit %s", domain, cqWhere,
                                query.getPageSize());
                    }
                    
                    List<HashMap<String, Object>> graphNode = neo4jUtil.GetGraphNode(nodeSql);
                    nr.put("node", graphNode);
                    String domainSql;
                    if ("ALL".equals(domain)) {
                        domainSql = String.format("MATCH (n)<-[r]-> (m) %s RETURN distinct(r) limit %s",
                                cqWhere, query.getPageSize());
                    } else {
                        domainSql = String.format("MATCH (n:`%s`)<-[r]-> (m) %s RETURN distinct(r) limit %s", domain,
                                cqWhere, query.getPageSize());
                    }
                    List<HashMap<String, Object>> graphRelation = neo4jUtil.GetGraphRelationShip(domainSql);
                    nr.put("relationship", graphRelation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nr;
    }
    
    
    /**
     * 获取某个领域指定节点拥有的上下级的节点数
     *
     * @param domain
     * @param nodeid
     * @return long 数值
     */
    @Override
    public long getrelationnodecount(String domain, long nodeid) {
        long totalcount = 0;
        try {
            if (!StringUtil.isBlank(domain)) {
                String nodeSql = String.format("MATCH (n:`%s`) <-[r]->(m)  where id(n)=%s return count(m)", domain,
                        nodeid);
                totalcount = neo4jUtil.GetGraphValue(nodeSql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalcount;
    }
    
    /**
     * 创建领域,默认创建一个新的节点,给节点附上默认属性
     *
     * @param domain
     */
    @Override
    public void createdomain(String domain) {
        try {
            String cypherSql = String.format(
                    "create (n:`%s`{entitytype:0,name:''}) return id(n)", domain);
            neo4jUtil.excuteCypherSql(cypherSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void createdomain(String domain, String fileID) {
        try {
            String cypherSql = String.format(
                    "create (n:`%s`{entitytype:0,name:'',fileID:%s}) return id(n)", domain, fileID);
            neo4jUtil.excuteCypherSql(cypherSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取/展开更多节点,找到和该节点有关系的节点
     *
     * @param domain
     * @param nodeid
     * @return
     */
    @Override
    public HashMap<String, Object> getmorerelationnode(String domain, String nodeid) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        try {
            String cypherSql = String.format("MATCH (n:`%s`) -[r]-(m) where id(n)=%s  return * limit 100", domain,
                    nodeid);
            result = neo4jUtil.GetGraphNodeAndShip(cypherSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 更新节点名称
     *
     * @param domain
     * @param nodeid
     * @param nodename
     * @return 修改后的节点
     */
    @Override
    public HashMap<String, Object> updatenodename(String domain, String nodeid, String nodename) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        List<HashMap<String, Object>> graphNodeList = new ArrayList<HashMap<String, Object>>();
        try {
            String cypherSql = String.format("MATCH (n:`%s`) where id(n)=%s set n.name='%s' return n", domain, nodeid,
                    nodename);
            graphNodeList = neo4jUtil.GetGraphNode(cypherSql);
            if (graphNodeList.size() > 0) {
                return graphNodeList.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 创建单个节点
     * Uuid不为零，创建
     * 否则修改已有节点
     *
     * @param domain
     * @param entity
     * @return
     */
    @Override
    //todo fileId
    public HashMap<String, Object> createnode(String domain, QAEntityItem entity) {
        HashMap<String, Object> rss = new HashMap<String, Object>();
        List<HashMap<String, Object>> graphNodeList = new ArrayList<HashMap<String, Object>>();
        try {
            if (entity.getUuid() != 0) {
                String sqlkeyval = neo4jUtil.getkeyvalCyphersql(entity);
                String cypherSql = String.format("match (n:`%s`) where id(n)=%s set %s return n", domain,
                        entity.getUuid(), sqlkeyval);
                graphNodeList = neo4jUtil.GetGraphNode(cypherSql);
            } else {
                entity.setR(30);// 默认半径
                SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
                filter.getExcludes().add("uuid");
                String propertiesString = neo4jUtil.getFilterPropertiesJson(JSON.toJSONString(entity, filter));
                String cypherSql = String.format("create (n:`%s` %s) return n", domain, propertiesString);
                graphNodeList = neo4jUtil.GetGraphNode(cypherSql);
            }
            if (graphNodeList.size() > 0) {
                rss = graphNodeList.get(0);
                return rss;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return rss;
    }
    
    /**
     * 批量创建节点和关系
     *
     * @param domain      领域
     * @param sourcename  源节点
     * @param relation    关系
     * @param targetnames 目标节点数组
     * @return
     */
    @Override
    public HashMap<String, Object> batchcreatenode(String domain, String sourcename, String relation,
                                                   String[] targetnames) {
        HashMap<String, Object> rss = new HashMap<String, Object>();
        List<HashMap<String, Object>> nodes = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> ships = new ArrayList<HashMap<String, Object>>();
        try {
            String cypherSqlFmt = "create (n:`%s` {name:'%s',color:'#ff4500',r:30}) return n";
            String cypherSql = String.format(cypherSqlFmt, domain, sourcename);// 概念实体
            List<HashMap<String, Object>> graphNodeList = neo4jUtil.GetGraphNode(cypherSql);
            if (graphNodeList.size() > 0) {
                HashMap<String, Object> sourceNode = graphNodeList.get(0);
                nodes.add(sourceNode);
                String sourceuuid = String.valueOf(sourceNode.get("uuid"));
                for (String tn : targetnames) {
                    String targetnodeSql = String.format(cypherSqlFmt, domain, tn);
                    List<HashMap<String, Object>> targetNodeList = neo4jUtil.GetGraphNode(targetnodeSql);
                    if (targetNodeList.size() > 0) {
                        HashMap<String, Object> targetNode = targetNodeList.get(0);
                        nodes.add(targetNode);
                        String targetuuid = String.valueOf(targetNode.get("uuid"));
                        String rSql = String.format(
                                "match(n:`%s`),(m:`%s`) where id(n)=%s and id(m)=%s create (n)-[r:RE {name:'%s'}]->(m) return r",
                                domain, domain, sourceuuid, targetuuid, relation);
                        List<HashMap<String, Object>> rshipList = neo4jUtil.GetGraphRelationShip(rSql);
                        ships.addAll(rshipList);
                    }
                    
                }
            }
            rss.put("nodes", nodes);
            rss.put("ships", ships);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return rss;
    }
    
    /**
     * 批量创建下级节点
     *
     * @param domain      领域
     * @param sourceid    源节点id
     * @param entitytype  节点类型
     * @param targetnames 目标节点名称数组
     * @param relation    关系
     * @return
     */
    @Override
    public HashMap<String, Object> batchcreatechildnode(String domain, String sourceid, Integer entitytype,
                                                        String[] targetnames, String relation) {
        HashMap<String, Object> rss = new HashMap<String, Object>();
        List<HashMap<String, Object>> nodes = new ArrayList<HashMap<String, Object>>();
        List<HashMap<String, Object>> ships = new ArrayList<HashMap<String, Object>>();
        try {
            String cypherSqlFmt = "create (n:`%s`{name:'%s',color:'#ff4500',r:30}) return n";
            String cypherSql = String.format("match (n:`%s`) where id(n)=%s return n", domain, sourceid);
            List<HashMap<String, Object>> sourcenodeList = neo4jUtil.GetGraphNode(cypherSql);
            if (sourcenodeList.size() > 0) {
                nodes.addAll(sourcenodeList);
                for (String tn : targetnames) {
                    String targetnodeSql = String.format(cypherSqlFmt, domain, tn);
                    List<HashMap<String, Object>> targetNodeList = neo4jUtil.GetGraphNode(targetnodeSql);
                    if (targetNodeList.size() > 0) {
                        HashMap<String, Object> targetNode = targetNodeList.get(0);
                        nodes.add(targetNode);
                        String targetuuid = String.valueOf(targetNode.get("uuid"));
                        // 创建关系
                        String rSql = String.format(
                                "match(n:`%s`),(m:`%s`) where id(n)=%s and id(m)=%s create (n)-[r:RE {name:'%s'}]->(m) return r",
                                domain, domain, sourceid, targetuuid, relation);
                        List<HashMap<String, Object>> shipList = neo4jUtil.GetGraphRelationShip(rSql);
                        ships.addAll(shipList);
                    }
                }
            }
            rss.put("nodes", nodes);
            rss.put("ships", ships);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return rss;
    }
    
    /**
     * 批量创建同级节点
     *
     * @param domain      领域
     * @param entitytype  节点类型
     * @param sourcenames 节点名称
     * @return
     */
    @Override
    public List<HashMap<String, Object>> batchcreatesamenode(String domain, Integer entitytype, String[] sourcenames) {
        List<HashMap<String, Object>> rss = new ArrayList<HashMap<String, Object>>();
        try {
            String cypherSqlFmt = "create (n:`%s`{name:'%s',color:'#ff4500',r:30}) return n";
            for (String tn : sourcenames) {
                String sourcenodeSql = String.format(cypherSqlFmt, domain, tn, entitytype);
                List<HashMap<String, Object>> targetNodeList = neo4jUtil.GetGraphNode(sourcenodeSql);
                rss.addAll(targetNodeList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return rss;
    }
    
    /**
     * 添加关系
     *  todo 删除节点的类型
     *
     * @param domain   领域
     * @param sourceid 源节点id
     * @param targetid 目标节点id
     * @param ship     关系
     * @return
     */
    @Override
    public HashMap<String, Object> createlink(String domain, long sourceid, long targetid, String ship) {
        HashMap<String, Object> rss = new HashMap<String, Object>();
        try {
            if (ship == "") {
                ship = domain;
            }
            String cypherSql = String.format("MATCH (n),(m) WHERE id(n)=%s AND id(m) = %s "
                    + "CREATE (n)-[r:`%s`{name:'%s'}]->(m)" + "RETURN r", sourceid, targetid, domain, ship);
            List<HashMap<String, Object>> cypherResult = neo4jUtil.GetGraphRelationShip(cypherSql);
            if (cypherResult.size() > 0) {
                rss = cypherResult.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return rss;
    }
    
    /**
     * 更新关系
     *
     * @param domain   领域
     * @param shipid   关系id
     * @param shipname 关系名称
     * @return
     */
    @Override
    public HashMap<String, Object> updatelink(String domain, long shipid, String shipname) {
        HashMap<String, Object> rss = new HashMap<String, Object>();
        try {
            String cypherSql = String.format("MATCH (n) -[r]->(m) where id(r)=%s set r.name='%s' return r",
                    shipid, shipname);
            List<HashMap<String, Object>> cypherResult = neo4jUtil.GetGraphRelationShip(cypherSql);
            if (cypherResult.size() > 0) {
                rss = cypherResult.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rss;
    }
    
    /**
     * 删除节点(先删除关系再删除节点)
     *
     * @param domain
     * @param nodeid
     * @return
     */
    @Override
    public List<HashMap<String, Object>> deletenode(String domain, long nodeid) {
        List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        try {
            String nSql = String.format("MATCH (n:`%s`)  where id(n)=%s return n", domain, nodeid);
            result = neo4jUtil.GetGraphNode(nSql);
            String rSql = String.format("MATCH (n:`%s`) <-[r]->(m) where id(n)=%s return r", domain, nodeid);
            neo4jUtil.GetGraphRelationShip(rSql);
            String deleteRelationSql = String.format("MATCH (n:`%s`) <-[r]->(m) where id(n)=%s delete r", domain, nodeid);
            neo4jUtil.excuteCypherSql(deleteRelationSql);
            String deleteNodeSql = String.format("MATCH (n:`%s`) where id(n)=%s delete n", domain, nodeid);
            neo4jUtil.excuteCypherSql(deleteNodeSql);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 删除关系
     *
     * @param domain
     * @param shipid
     */
    @Override
    public void deletelink(String domain, long shipid) {
        try {
            String cypherSql = String.format("MATCH (n:`%s`) -[r]->(m) where id(r)=%s delete r", domain, shipid);
            neo4jUtil.excuteCypherSql(cypherSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void updateCorrdOfNode(String domain, String uuid, Double fx, Double fy) {
        String cypher = null;
        if (fx == null && fy == null) {
            cypher = " MATCH (n:" + domain + ") where ID(n)=" + uuid
                    + " set n.fx=null, n.fy=null; ";
        } else {
            if ("0.0".equals(fx.toString()) && "0.0".equals(fy.toString())) {
                cypher = " MATCH (n:" + domain + ") where ID(n)=" + uuid
                        + " set n.fx=null, n.fy=null; ";
            } else {
                cypher = " MATCH (n:" + domain + ") where ID(n)=" + uuid
                        + " set n.fx=" + fx + ", n.fy=" + fy + ";";
            }
        }
        neo4jUtil.excuteCypherSql(cypher);
    }
    
    
    @Override
    public Map<String, Object> getProperties(String label, String id) {
        String cypher = "MATCH (n:" + label + ") where id(n)=" + id + " RETURN properties(n)";
        StatementResult result = neo4jUtil.excuteCypherSql(cypher);
        if (result.hasNext()) {
            Record record = result.next();
            return (Map<String, Object>) record.asMap().get("properties(n)");
        }
        return null;
    }
    
    @Override
    public StatementResult saveProperties(String label, String id, Map<String, Object> properties) {
        StringBuilder cypher = new StringBuilder("MATCH (n:" + label + ") where id(n)=" + id + " set");
        Iterator<String> it = properties.keySet().iterator();
        String[] propertiesString = new String[properties.size()];
        int i = 0;
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        while (it.hasNext()) {
            
            String key = it.next();
            String value = pattern.matcher(properties.get(key).toString()).matches() ? properties.get(key).toString() : "'" + properties.get(key) + "'";
            propertiesString[i++] = " n." + key + "=" + value;
        }
        cypher.append(String.join(",", propertiesString));
        return neo4jUtil.excuteCypherSql(cypher.toString());
    }
    
    
}
