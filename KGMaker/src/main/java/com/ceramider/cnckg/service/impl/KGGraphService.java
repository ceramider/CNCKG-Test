package com.ceramider.cnckg.service.impl;

import com.ceramider.cnckg.dal.IKGraphRepository;
import com.ceramider.cnckg.entity.QAEntityItem;
import com.ceramider.cnckg.service.IKGGraphService;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ceramider.cnckg.query.GraphQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KGGraphService implements IKGGraphService {

    @Autowired
    @Qualifier("KGraphRepository")
    private IKGraphRepository kgRepository;

    @Override
    public Map<String, Object> getProperties(String label, String id) {
        return kgRepository.getProperties(label, id);
    }

    @Override
    public StatementResult saveProperties(String label, String id, Map<String, Object> properties) {
        return kgRepository.saveProperties(label, id, properties);
    }
    
    @Override
    public void deleteKGdomain(String domain) {
        kgRepository.deleteKGdomain(domain);
    }

    @Override
    public HashMap<String, Object> getdomaingraph(GraphQuery query) {
        return kgRepository.getdomaingraph(query);
    }
    
    @Override
    public long getrelationnodecount(String domain, long nodeid) {
        return kgRepository.getrelationnodecount(domain, nodeid);
    }

    @Override
    public void createdomain(String domain) {
        kgRepository.createdomain(domain);
    }
    
    @Override
    public void createdomain(String domain, String fileID) {
        kgRepository.createdomain(domain,fileID);
    }
    
    @Override
    public HashMap<String, Object> getmorerelationnode(String domain, String nodeid) {
        return kgRepository.getmorerelationnode(domain, nodeid);
    }

    @Override
    public HashMap<String, Object> updatenodename(String domain, String nodeid, String nodename) {
        return kgRepository.updatenodename(domain, nodeid, nodename);
    }

    @Override
    public HashMap<String, Object> createnode(String domain, QAEntityItem entity) {
        return kgRepository.createnode(domain, entity);
    }

    @Override
    public HashMap<String, Object> batchcreatenode(String domain, String sourcename, String relation,
                                                   String[] targetnames) {
        return kgRepository.batchcreatenode(domain, sourcename, relation, targetnames);
    }

    @Override
    public HashMap<String, Object> batchcreatechildnode(String domain, String sourceid, Integer entitytype,
                                                        String[] targetnames, String relation) {
        return kgRepository.batchcreatechildnode(domain, sourceid, entitytype, targetnames, relation);
    }

    @Override
    public List<HashMap<String, Object>> batchcreatesamenode(String domain, Integer entitytype, String[] sourcenames) {
        return kgRepository.batchcreatesamenode(domain, entitytype, sourcenames);
    }

    @Override
    public HashMap<String, Object> createlink(String domain, long sourceid, long targetid, String ship) {
        return kgRepository.createlink(domain, sourceid, targetid, ship);
    }

    @Override
    public HashMap<String, Object> updatelink(String domain, long shipid, String shipname) {
        return kgRepository.updatelink(domain, shipid, shipname);
    }

    @Override
    public List<HashMap<String, Object>> deletenode(String domain, long nodeid) {
        return kgRepository.deletenode(domain, nodeid);
    }

    @Override
    public void deletelink(String domain, long shipid) {
        kgRepository.deletelink(domain, shipid);
    }

    

    @Override
    public void updateCorrdOfNode(String domain, String uuid, Double fx, Double fy) {
        kgRepository.updateCorrdOfNode(domain,uuid,fx,fy);
    }




}
