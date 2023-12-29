package com.ceramider.cnckg.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ceramider.cnckg.entity.QAEntityItem;
import com.ceramider.cnckg.service.IKGGraphService;
import com.ceramider.cnckg.service.IKnowledgegraphService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ceramider.cnckg.query.GraphQuery;
import com.ceramider.cnckg.util.GraphPageRecord;
import com.ceramider.cnckg.util.Neo4jUtil;
import com.ceramider.cnckg.util.R;
import com.ceramider.cnckg.util.StringUtil;
import org.neo4j.driver.v1.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/")
public class KGManagerController extends BaseController {
    @Autowired
    private Neo4jUtil neo4jUtil;
    @Autowired
    private IKGGraphService KGGraphService;
    @Autowired
    private IKnowledgegraphService kgservice;
    @Autowired
    private Driver neo4jDriver;


    @ResponseBody
    @PostMapping("/saveProperties")
    public R saveProperties(@RequestParam("label") String label, @RequestParam("id") String id, @RequestParam("properties") String properties) {
        R result = new R();
        JSONObject jsonObject = JSON.parseObject(properties);
        try {
            if (KGGraphService.saveProperties(label, id, jsonObject) != null) {
                result.code = 200;
                result.setMsg("保存成功");
            } else {
                result.code = 400;
                result.msg = "属性值错误";
                result.msg = "属性值错误";
            }


        } catch (Exception e) {
            result.code = 500;
            result.msg = "服务器错误";
        }
        return result;
    }

    @ResponseBody
    @GetMapping("/getProperties")
    public R<Map<String, Object>> getProperties(String label, String id) {
        R<Map<String, Object>> result = new R<>();
        try {
            Map<String, Object> temp = KGGraphService.getProperties(label, id);
            if (temp == null) {
                result.code = 404;
                result.setMsg("无对应记录");
            } else {
                result.code = 200;
                result.setMsg("查询成功");
                result.setData(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getgraph") // call db.labels
    public R<GraphPageRecord<Map<String, Object>>> getgraph(GraphQuery queryItem) {
        R<GraphPageRecord<Map<String, Object>>> result = new R<GraphPageRecord<Map<String, Object>>>();
        GraphPageRecord<Map<String, Object>> resultRecord = new GraphPageRecord<Map<String, Object>>();
        try {
            String name = "tc";
            PageHelper.startPage(queryItem.getPageIndex(), queryItem.getPageSize(), true);
            List<Map<String, Object>> domainList = kgservice.getDomainList(queryItem.getDomain(), name, queryItem.getFileID());
            PageInfo<Map<String, Object>> pageInfo = new PageInfo<Map<String, Object>>(domainList);
            long total = pageInfo.getTotal();
            resultRecord.setPageIndex(queryItem.getPageIndex());
            resultRecord.setPageSize(queryItem.getPageSize());
            resultRecord.setTotalCount(new Long(total).intValue());
            resultRecord.setNodeList(pageInfo.getList());
            result.code = 200;
            result.setData(resultRecord);
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getdomaingraph")
    public R<HashMap<String, Object>> getDomainGraph(GraphQuery query) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        try {
            HashMap<String, Object> graphData = KGGraphService.getdomaingraph(query);
            result.code = 200;
            result.data = graphData;

        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getcypherresult")
    public R<HashMap<String, Object>> getcypherresult(String cypher) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        String error = "";
        try {
            HashMap<String, Object> graphData = neo4jUtil.GetGraphNodeAndShip(cypher);
            result.code = 200;
            result.data = graphData;
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            error = e.getMessage();
            result.setMsg("服务器错误");
        } finally {
            if (StringUtil.isNotBlank(error)) {
                result.code = 500;
                result.setMsg(error);
            }
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getrelationnodecount")
    public R<String> getrelationnodecount(String domain, long nodeid) {
        R<String> result = new R<String>();
        try {
            long totalcount = 0;
            if (!StringUtil.isBlank(domain)) {
                totalcount = KGGraphService.getrelationnodecount(domain, nodeid);
                result.code = 200;
                result.setData(String.valueOf(totalcount));
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/createdomain")
    public R<String> createdomain(String domain, String fileID) {
        R<String> result = new R<String>();
        try {
            if (!StringUtil.isBlank(domain)) {
                List<Map<String, Object>> domainItem = kgservice.getDomainByName(domain, fileID);
                if (domainItem.size() > 0) {
                    result.code = 300;
                    result.setMsg("领域已存在");
                } else {
                    String name = "tc";
                    Map<String, Object> maps = new HashMap<String, Object>();
                    maps.put("name", domain);
                    maps.put("nodecount", 1);
                    maps.put("shipcount", 0);
                    maps.put("status", 1);
                    maps.put("createuser", name);
                    maps.put("file_id", fileID);
                    kgservice.saveDomain(maps);// 保存到mysql
                    KGGraphService.createdomain(domain, fileID);// 保存到图数据
                    result.code = 200;
                }
                List<Map<String, Object>> ALLItem = kgservice.getDomainByName("ALL", fileID);
                if (ALLItem.size() <= 0) {
                    String name = "tc";
                    Map<String, Object> maps = new HashMap<String, Object>();
                    maps.put("name", "ALL");
                    maps.put("nodecount", 1);
                    maps.put("shipcount", 0);
                    maps.put("status", 1);
                    maps.put("createuser", name);
                    maps.put("file_id", fileID);
                    kgservice.saveDomain(maps);// 保存到mysql
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/getmorerelationnode")
    public R<HashMap<String, Object>> getmorerelationnode(String domain, String nodeid) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        try {
            if (!StringUtil.isBlank(domain)) {
                HashMap<String, Object> graphModel = KGGraphService.getmorerelationnode(domain, nodeid);
                if (graphModel != null) {
                    result.code = 200;
                    result.setData(graphModel);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updatenodename")
    public R<HashMap<String, Object>> updatenodename(String domain, String nodeid, String nodename) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        HashMap<String, Object> graphNodeList = new HashMap<String, Object>();
        try {
            if (!StringUtil.isBlank(domain)) {
                graphNodeList = KGGraphService.updatenodename(domain, nodeid, nodename);
                if (graphNodeList.size() > 0) {
                    result.code = 200;
                    result.setData(graphNodeList);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updateCorrdOfNode")
    public R<String> updateCorrdOfNode(String domain, String uuid, Double fx, Double fy) {
        R<String> result = new R<String>();
        try {
            KGGraphService.updateCorrdOfNode(domain, uuid, fx, fy);
            result.code = 200;
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/createnode")
    public R<HashMap<String, Object>> createnode(QAEntityItem entity, HttpServletRequest request,
                                                 HttpServletResponse response) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        HashMap<String, Object> graphNode = new HashMap<String, Object>();
        try {
            String domain = request.getParameter("domain");
            if (!StringUtil.isBlank(domain)) {
                List<Map<String, Object>> domainItem = kgservice.getDomainByName(domain, entity.getFileID());
                if (domainItem.size() <= 0) {
                    String name = "tc";
                    Map<String, Object> maps = new HashMap<String, Object>();
                    maps.put("name", domain);
                    maps.put("nodecount", 1);
                    maps.put("shipcount", 0);
                    maps.put("status", 1);
                    maps.put("createuser", name);
                    maps.put("file_id", entity.getFileID());
                    kgservice.saveDomain(maps);
                }
                graphNode = KGGraphService.createnode(domain, entity);
                if (graphNode != null && graphNode.size() > 0) {
                    result.code = 200;
                    result.setData(graphNode);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }

        return result;
    }


    @ResponseBody
    @RequestMapping(value = "/createlink")
    public R<HashMap<String, Object>> createlink(String domain, long sourceid, long targetid, String ship) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        try {
            HashMap<String, Object> cypherResult = KGGraphService.createlink(domain, sourceid, targetid, ship);
            result.code = 200;
            result.setData(cypherResult);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updatelink")
    public R<HashMap<String, Object>> updatelink(String domain, long shipid, String shipname) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        try {
            HashMap<String, Object> cypherResult = KGGraphService.updatelink(domain, shipid, shipname);
            result.code = 200;
            result.setData(cypherResult);
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/deletenode")
    public R<List<HashMap<String, Object>>> deletenode(String domain, long nodeid) {
        R<List<HashMap<String, Object>>> result = new R<List<HashMap<String, Object>>>();
        try {
            List<HashMap<String, Object>> rList = KGGraphService.deletenode(domain, nodeid);
            result.code = 200;
            result.setData(rList);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    /**
     * fileID 不需要传，前端传递的domainId绑定了File
     *
     * @param domainid
     * @param domain
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/deletedomain")
    public R<List<HashMap<String, Object>>> deletedomain(Integer domainid, String domain) {
        R<List<HashMap<String, Object>>> result = new R<List<HashMap<String, Object>>>();
        try {
            kgservice.deleteDomain(domainid);
            KGGraphService.deleteKGdomain(domain);
            result.code = 200;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/deletelink")
    public R<HashMap<String, Object>> deletelink(String domain, long shipid) {
        R<HashMap<String, Object>> result = new R<HashMap<String, Object>>();
        try {
            KGGraphService.deletelink(domain, shipid);
            result.code = 200;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.code = 500;
            result.setMsg("服务器错误");
        }
        return result;
    }


}
