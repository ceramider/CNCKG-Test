<template xmlns="" xmlns="">
  <div @mousedown="globalMousedown">
    <div>
      <div class="mind-box">
        <!-- 左侧 -->
        <el-scrollbar class="mind-l">
          <div class="ml-m">
            <div class="ml-ht">
              <div style="font-weight:bold; color: #156498">
                图谱列表
              </div>
              <div style="font-weight:bold;">
                当前展示:{{ currentDomain }}
              </div>
            </div>

            <div class="ml-a-box" style="min-height:48px; border-bottom:1px solid #d3e2ec">
              <tag-editor :fileID="currentFile.id" :tagEditorShow="tagEditorShow" @tagEditorShow="tagEditorShowChange"/>
              <el-button type="info" style="margin: 2px 0 4px 2px;" plain size="small" @click="tagEditorShow = true"
                         :disabled="currentFile.id === ''">文件标签编辑
              </el-button>
            </div>

            <div class="ml-a-box" style="min-height:280px">
              <el-button type="info" style="margin: 2px 0 4px 2px;" plain size="small" @click="createdomain">新建图谱
              </el-button>
              <a @click="matchdomaingraph(m,$event)" v-for="m in pageModel.nodeList" href="javascript:void(0)">
                <el-tag closable style="margin:2px" @close="deletedomain(m.name)">{{ m.name }}</el-tag>
              </a>
              <el-button v-if="pageModel.pageIndex<pageModel.totalPage" type="info" style="margin: 2px 0 4px 2px;" plain
                         size="small" @click="getmoredomain">加载更多
              </el-button>
            </div>
          </div>
        </el-scrollbar>
        <!-- 左侧over -->
        <!-- 右侧 -->
        <div class="mind-con">
          <!-- 头部 -->
          <div class="mind-top clearfix">
            <div class="fl">
              <div class="search" v-show="domain!==''">
                <el-button @click="getdomaingraph()">
                  <i class="el-icon-search"></i>
                </el-button>
                <el-input placeholder="请输入关键词" v-model="nodename" @keyup.enter.native="getdomaingraph"></el-input>
              </div>

            </div>
            <div class="fr">
              <!--              <a href="javascript:void(0)" @click="cypherjson" class="svg-a-sm">-->
              <!--                <i class="el-icon-tickets">显示json</i>-->
              <!--              </a>-->
              <a href="javascript:void(0)" @click="showCypher" class="svg-a-sm">
                <i class="el-icon-caret-right">执行Cypher</i>
              </a>
              <a href="javascript:void(0)" @click="updategraph" class="svg-a-sm">
                <i class="el-icon-refresh">刷新</i>
              </a>
              <a href="javascript:void(0)" @click="requestFullScreen" class="svg-a-sm">
                <i class="el-icon-full-screen">全屏</i>
              </a>
            </div>
          </div>
          <div class="cypher_toolbar clearfix" v-show="cyphertextshow">
            <div style="width: 75%;float: left">
              <el-input type="textarea" :rows="2" placeholder="请输入Cypher" v-model="cyphertext"></el-input>
            </div>
            <div style="padding: 7px;">
              <el-button type="success" @click="cypherrun" style="margin-left: 15px;" icon="el-icon-caret-right" plain>
                执行
              </el-button>
            </div>
          </div>
          <!-- 头部over -->
          <!-- 中部 -->
          <el-scrollbar class="mind-cen" id="graphcontainerdiv">
            <div id="nodedetail" class="node_detail">
              <h5>详细数据</h5>
              <span class="node_pd" v-for="(m,k) in nodedetail"
                    v-if="k!=='x' && k!=='y' && k!=='fx' && k!=='fy'&& k!=='r'&& k!=='vx'&& k!=='vy'&& k!=='color'&& k!=='index'&& k!=='fixed'">{{
                  k
                }}:{{ m }}</span>
            </div>


            <el-scrollbar v-show="jsonshow" id="jsoncontainer" class="jsoncontainer">
              <json-viewer :value="graph"></json-viewer>
            </el-scrollbar>
            <div id="graphcontainer" class="graphcontainer" @click.right="graphContainerRightClick"
                 @click="graphContainerClick"></div>
          </el-scrollbar>
          <span v-show="domain!==''">
            <span class="dibmr">
              <span>显示节点个数：</span>
              <a v-for="(m,index) in pagesizelist" @click="setmatchsize(m,this)" :title="m.size"
                 href="javascript:void(0)"
                 :class="[m.isactive ? 'sd-active' : '', 'sd']">{{ m.size }}</a>
            </span>
          </span>
          <!-- 中部over -->
          <div class="svg-set-box"></div>
          <!-- 对话框-->
          <!--          <el-dialog id="editform1" title="属性编辑" :visible.sync="tagEditorShow" >-->
          <!--            <el-form :model="graphEntity">-->
          <!--              <el-form-item label="节点名称" label-width="70px">-->
          <!--                <el-input v-model="graphEntity.name"></el-input>-->
          <!--              </el-form-item>-->
          <!--              <el-form-item label="节点半径" label-width="70px">-->
          <!--                <el-slider v-model="graphEntity.r"></el-slider>-->
          <!--              </el-form-item>-->
          <!--            </el-form>-->
          <!--          </el-dialog>-->

          <el-dialog id="editform" title="属性编辑" :visible.sync="isedit">
            <el-tabs type="card" tab-position="top" v-model="propactiveName" @tab-click="prophandleClick">
              <el-tab-pane label="UI编辑" name="propedit">
                <el-form :model="graphEntity">
                  <el-form-item label="节点名称" label-width="70px">
                    <el-input v-model="graphEntity.name"></el-input>
                  </el-form-item>
                  <el-form-item label="选择颜色" label-width="70px">
                    <el-color-picker id="colorpicker"
                                     v-model="graphEntity.color" :predefine="predefineColors">
                    </el-color-picker>
                  </el-form-item>
                  <el-form-item label="节点半径" label-width="70px">
                    <el-slider v-model="graphEntity.r"></el-slider>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
              <el-tab-pane label="节点属性" name="properties">
                <el-form :model="properties">
                  <el-form-item v-for="(value, key) in properties" label-width="70px" :label="key" :key="key">
                    <el-input type="textarea" autosize v-model="properties[key]"></el-input>
                  </el-form-item>
                </el-form>
              </el-tab-pane>
            </el-tabs>
            <div slot="footer" class="dialog-footer">
              <el-button v-show="propactiveName==='properties'" type="primary" @click="propertyCreate=true"
                         class="btn-line cur">新增
              </el-button>
              <el-button v-show="propactiveName==='properties'" type="primary" @click="saveproperties"
                         class="btn-line cur">保存
              </el-button>

              <el-button v-show="propactiveName==='propedit'&&graphEntity.uuid!==0" type="primary" @click="createnode">
                更新
              </el-button>
              <el-button v-show="propactiveName==='propedit'&&graphEntity.uuid===0" type="primary" @click="createnode">
                创建
              </el-button>
              <el-button @click="resetsubmit">取消</el-button>
            </div>
          </el-dialog>
          <el-dialog id="newProperty" title="新增属性" :visible.sync="propertyCreate">
            <el-form>
              <el-form-item label="新增属性名" inline>
                <el-input id="newPropertyInput" style="width: 250px;" type="textarea" autosize></el-input>
              </el-form-item>
              <el-button @click="newProperty">确定</el-button>
            </el-form>
          </el-dialog>
          <!-- 底部over -->
        </div>
        <!-- 右侧over -->
        <!-- 空白处右键 -->
        <ul class="el-dropdown-menu el-popper blankmenubar" id="blank_menubar" style="display: none;"
            @mouseleave="blankMenubarLeave" @click="blankMenubarClick">
          <li class="el-dropdown-menu__item" @click="btnaddsingle">
            <i class="el-icon-circle-plus-outline"></i>
            <span class="pl-15">添加节点</span>
          </li>
        </ul>
        <!-- 连线按钮组 -->
        <ul class="el-dropdown-menu el-popper linkmenubar" id="link_menubar" style="display: none;"
            @mouseleave="linkMenubarLeave">
          <li class="el-dropdown-menu__item" @click="updatelinkName">
            <i class="el-icon-edit-outline"></i>
            <span class="pl-15">编辑</span>
          </li>
          <li class="el-dropdown-menu__item" @click="deletelink">
            <i class="el-icon-delete"></i>
            <span class="pl-15">删除</span>
          </li>
        </ul>

      </div>
    </div>
  </div>
</template>

<script>
import JsonViewer from 'vue-json-viewer'
import _ from 'lodash'
import * as d3 from 'd3'

import TagEditor from "./TagEditor";

export default {
  name: "Home",
  comments: {
    JsonViewer
  },
  computed: {
    fid() {
      return this.$route.query.fid || ''
    }
  },
  watch: {
    fid: {
      immediate: true,
      handler(fid) {
        this.currentFile.id = fid;
        this.$nextTick(() => {
          this.doKG();
        })
      }
    }
  },
  components: {
    TagEditor,
  },
  data() {
    return {
      tagEditorShow: false,
      svg: null,
      simulation: null,
      linkGroup: null,
      linktextGroup: null,
      nodeGroup: null,
      nodetextGroup: null,
      nodesymbolGroup: null,
      nodebuttonGroup: null,
      nodebuttonAction: '',
      tooltip: null,
      txx: {},
      tyy: {},
      nodedetail: null,
      pagesizelist: [{size: 100, isactive: true}, {size: 500, isactive: false}, {
        size: 1000,
        isactive: false
      }, {size: 2000, isactive: false}],
      predefineColors: ['#ff4500', '#ff8c00', '#90ee90', '#00ced1', '#1e90ff', '#c71585'],
      dataconfigactive: '',
      isedit: false,
      isaddnode: false,
      isaddlink: false,
      isdeletelink: false,
      isbatchcreate: false,
      selectnodeid: 0,
      selectnodename: '',
      selectsourcenodeid: 0,
      selecttargetnodeid: 0,
      domain: '',
      domainid: 0,
      nodename: '',
      pagesize: 100,
      cyphertext: '',
      cyphertextshow: false,
      jsonshow: false,
      propactiveName: 'propedit',
      properties: {},
      propertyCreate: false,
      selectnode: {
        name: '',
        count: 0
      },
      pageModel: {
        pageIndex: 1,
        pageSize: 10,
        totalCount: 0,
        totalPage: 0,
        nodeList: []
      },
      graph: {
        nodes: [],
        links: []
      },
      graphEntity: {
        fileID: "",
        uuid: 0,
        name: '',
        color: 'ff4500',
        r: 30,
        x: "",
        y: ""
      },
      currentDomain: "无",
      currentFile: {
        file: true,
        id: "1",
        leaf: true,
        name: "",
        pdf: ""
      }
    };
  },
  filters: {
    labelformat: function (value) {
      var domain = value.substring(1, value.length - 1);
      return domain;
    },
  },
  mounted() {
    this.initgraph();
  },

  created() {
    // this.getlabels();
  },

  methods: {
    tagEditorShowChange(val) {
      this.tagEditorShow = val
    },
    testFileID() {
      this.doKG();
    },
    doKG() {
      this.getlabels();
      this.graph = {nodes: [], links: []};
      this.updategraph();
    },

    blankMenubarLeave() {
      document.getElementById("blank_menubar").style.display = 'none'
    },
    blankMenubarClick() {
      document.getElementById("blank_menubar").style.display = 'none'
    },
    graphContainerRightClick(event) {
      this.svg.selectAll(".buttongroup").classed("circle_opreate", true);
      var left = event.clientX;
      var top = event.clientY;
      document.getElementById('blank_menubar').style.position = 'absolute';
      document.getElementById('blank_menubar').style.left = left + 'px';
      document.getElementById('blank_menubar').style.top = top + 'px';
      document.getElementById('blank_menubar').style.display = "block";
      event.preventDefault();
    },
    graphContainerClick(event) {
      if (event.target.tagName !== "circle" && event.target.tagName !== "link") {
        d3.select('#nodedetail').style('display', 'none');
      }
      if (!(event.target.id === "jsoncontainer")) {
        this.jsonshow = false;
      }
      var cursor = document.getElementById("graphcontainer").style.cursor;
      if (cursor === 'crosshair') {
        d3.select('.graphcontainer').style("cursor", "");
        this.txx = event.offsetX;
        this.tyy = event.offsetY;
        console.log("添加走这")
        this.createSingleNode();
      }

      if (event.target.id !== "link_menubar" && event.target.parentElement.id !== "link_menubar" && event.target.parentElement.parentElement.id !== "link_menubar") {
        document.getElementById("link_menubar").style.display = "none"
      }
      event.preventDefault();
    },
    linkMenubarLeave() {
      d3.select('#link_menubar').style('display', 'none');
    },
    globalMousedown(event) {
      if (event.target.id !== "link_menubar" && event.target.parentElement.id !== "link_menubar" && event.target.parentElement.parentElement.id !== "link_menubar") {
        document.getElementById("link_menubar").style.display = "none"
      }
    },

    createdomain(value) {
      var _this = this;
      _this.$prompt('请输入领域名称', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(function (res) {
        value = res.value;
        var data = {
          fileID: _this.currentFile.id,
          domain: value
        };
        _this.$api.kgManager.createDomain(data).then((result) => {
          if (result.code === 200) {
            _this.getlabels();
            _this.domain = value;
            _this.getdomaingraph();
          } else {
            _this.$message({
              showClose: true,
              message: result.msg,
              type: 'warning'
            });
          }
        }).catch((e) => {
          _this.$message.error("createdomain: " + e);
        });
      }).catch(function () {
        this.$message({
          type: 'info',
          message: '取消输入'
        });
      });
    },

    getlabels() {
      var _this = this;
      var data = {
        fileID: _this.currentFile.id
      };
      _this.$api.kgManager.getGraph(data).then((result) => {
        if (result.code === 200) {
          _this.pageModel = result.data;
          _this.pageModel.totalPage = parseInt((result.data.totalCount - 1) / result.data.pageSize) + 1
        }
      }).catch((e) => {
        _this.$message.error("getlabels: " + e);
      });
    },

    getdomaingraph() {
      var _this = this;
      _this.loading = true;
      var data = {
        fileID: _this.currentFile.id,
        domain: _this.domain,
        nodename: _this.nodename,
        pageSize: _this.pagesize
      };
      _this.$api.kgManager.getDomainGraph(data).then((result) => {
        if (result.code === 200) {
          var graphModel = result.data;
          if (graphModel != null) {
            _this.graph.nodes = graphModel.node;
            _this.graph.links = graphModel.relationship;
            _this.updategraph();
          }
        }
      }).catch((e) => {
        _this.$message.error("getdomaingraph: " + e);
      });
    },

    matchdomaingraph(domain, event) {
      this.domain = domain.name;
      this.domainid = domain.id;
      this.currentDomain = domain.name;
      this.getdomaingraph()
    },

    deletedomain(value) {
      var _this = this;
      _this.$confirm('此操作将删除该标签及其下节点和关系(不可恢复), 是否继续?', '三思而后行', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function (res) {
        var data = {
          fileID: _this.currentFile.id,
          domain: value
        };
        _this.$api.kgManager.deleteDomain(data).then((result) => {
          if (result.code === 200) {
            _this.getlabels();
            _this.domain = "";
          } else {
            _this.$message({
              showClose: true,
              message: result.msg,
              type: 'warning'
            });
          }
        }).catch((e) => {
          _this.$message.error("deletedomain: " + e);
        });
      }).catch(function () {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },

    getmoredomain() {
      var _this = this;
      _this.pageModel.pageIndex = _this.pageModel.pageIndex + 1
      var data = {
        fileID: _this.currentFile.id,
        pageIndex: _this.pageModel.pageIndex
      };
      _this.$api.kgManager.getGraph(data).then((result) => {
        if (result.code === 200) {
          _this.pageModel.nodeList.push.apply(_this.pageModel.nodeList, result.data.nodeList);
        }
      }).catch((e) => {
        _this.$message.error("getmoredomain: " + e);
      });
    },

    setmatchsize(m, event) {
      for (var i = 0; i < this.pagesizelist.length; i++) {
        this.pagesizelist[i].isactive = false;
        if (this.pagesizelist[i].size === m.size) {
          this.pagesizelist[i].isactive = true;
        }
      }
      this.pagesize = m.size;
      this.getdomaingraph();
    },

    showCypher() {
      this.cyphertextshow = !this.cyphertextshow;
    },
    cypherjson() {
      if (this.graph.nodes.length === 0 && this.graph.links.length === 0) {
        this.$message.error("请先选择领域或者执行cypher");
        return;
      }
      this.jsonshow = !this.jsonshow;
    },
    cypherrun() {
      var _this = this;
      if (_this.cyphertext === "") {
        _this.$message.error("请输入cypher语句");
        return;
      }
      var data = {
        fileID: _this.currentFile.id,
        cypher: _this.cyphertext
      };
      _this.$api.kgManager.getCypherResult(data).then((result) => {
        if (result.code === 200) {
          _this.graph.nodes = result.data.node;
          _this.graph.links = result.data.relationship;
          _this.updategraph();
        } else {
          _this.$message.error(result.msg);
        }
      }).catch((e) => {
        _this.$message.error("cypherrun: " + e);
      });
    },

    prophandleClick(tab, event) {
      if (tab.name === 'properties') {
        console.log(this.graphEntity.type, this.graphEntity.uuid)
        this.getProperties(this.graphEntity.type, this.graphEntity.uuid)
      }
    },

    initgraph() {
      var graphcontainer = d3.select(".graphcontainer");
      var width = graphcontainer._groups[0][0].offsetWidth;
      var height = window.screen.height - 154;//
      this.svg = graphcontainer.append("svg");
      this.svg.attr("width", width);
      this.svg.attr("height", height);
      this.simulation = d3.forceSimulation()
        .force("link", d3.forceLink().distance(function (d) {
          return Math.floor(Math.random() * (700 - 200)) + 200;
        }).id(function (d) {
          return d.uuid
        }))
        .force("charge", d3.forceManyBody().strength(-400).distanceMax(400))
        .force("collide", d3.forceCollide())
        .force("center", d3.forceCenter(width / 2, (height - 200) / 2));
      this.linkGroup = this.svg.append("g").attr("class", "line");
      this.linktextGroup = this.svg.append("g").attr("class", "linetext");
      this.nodeGroup = this.svg.append("g").attr("class", "node");
      this.nodetextGroup = this.svg.append("g").attr("class", "nodetext");
      this.nodesymbolGroup = this.svg.append("g").attr("class", "nodesymbol");
      this.nodebuttonGroup = this.svg.append("g").attr("class", "nodebutton");
      this.addmaker();
      this.tooltip = this.svg.append("div").style("opacity", 0);
      this.svg.on('click', function () {
        d3.selectAll(".buttongroup").classed("circle_opreate", true);
      }, 'false');

    },

    updategraph() {
      var _this = this;
      var lks = this.graph.links;
      var nodes = this.graph.nodes;
      var links = [];
      //由后端传过来的节点坐标，固定节点，由于是字符串，需要转换
      nodes.forEach(function (n) {
        if (typeof (n.fx) == "undefined" || n.fx === "" || n.fx == null) {
          n.fx = null;
        }
        if (typeof (n.fy) == "undefined" || n.fy === "" || n.fy == null) {
          n.fy = null;
        }
        if ((typeof n.fx) == "string") n.fx = parseFloat(n.fx);
        if ((typeof n.fy) == "string") n.fy = parseFloat(n.fy);
      });
      lks.forEach(function (m) {
        var sourceNode = nodes.filter(function (n) {
          return n.uuid === m.sourceid;
        })[0];
        if (typeof (sourceNode) == 'undefined') return;
        var targetNode = nodes.filter(function (n) {
          return n.uuid === m.targetid;
        })[0];
        if (typeof (targetNode) == 'undefined') return;
        links.push({source: sourceNode.uuid, target: targetNode.uuid, lk: m});
      });
      //为每一个节点定制按钮组
      _this.addnodebutton();
      if (links.length > 0) {
        _.each(links, function (link) {
          console.log("link", link)
          var same = _.filter(links, {
            'source': link.source,
            'target': link.target
          });
          var sameAlt = _.filter(links, {
            'source': link.target,
            'target': link.source
          });
          var sameAll = same.concat(sameAlt);
          _.each(sameAll, function (s, i) {
            s.sameIndex = (i + 1);
            s.sameTotal = sameAll.length;
            s.sameTotalHalf = (s.sameTotal / 2);
            s.sameUneven = ((s.sameTotal % 2) !== 0);
            s.sameMiddleLink = ((s.sameUneven === true) && (Math.ceil(s.sameTotalHalf) === s.sameIndex));
            s.sameLowerHalf = (s.sameIndex <= s.sameTotalHalf);
            s.sameArcDirection = 1;
            //s.sameArcDirection = s.sameLowerHalf ? 0 : 1;
            s.sameIndexCorrected = s.sameLowerHalf ? s.sameIndex : (s.sameIndex - Math.ceil(s.sameTotalHalf));
          });
        });
        var maxSame = _.chain(links)
          .sortBy(function (x) {
            return x.sameTotal;
          })
          .last()
          .value().sameTotal;

        _.each(links, function (link) {
          link.maxSameHalf = Math.round(maxSame / 2);
        });
      }
      // 更新连线 links
      var link = _this.linkGroup.selectAll(".line >path").data(links, function (d) {
        return d.uuid;
      });
      link.exit().remove();
      var linkEnter = _this.drawlink(link);
      link = linkEnter.merge(link);
      // 更新连线文字
      _this.linktextGroup.selectAll("text").data(links, function (d) {
        return d.uuid;
      }).exit().remove();//移除多余的text dom
      var linktext = _this.linktextGroup.selectAll("text >textPath").data(links, function (d) {
        return d.uuid;
      });
      linktext.exit().remove();
      var linktextEnter = _this.drawlinktext(linktext);
      linktext = linktextEnter.merge(linktext).text(function (d) {
        return d.lk.name;
      });
      // 更新节点按钮组

      d3.selectAll(".nodebutton >g").remove();
      var nodebutton = _this.nodebuttonGroup.selectAll(".nodebutton").data(nodes, function (d) {
        return d
      });
      nodebutton.exit().remove();
      var nodebuttonEnter = _this.drawnodebutton(nodebutton);
      nodebutton = nodebuttonEnter.merge(nodebutton);
      // 更新节点
      var node = _this.nodeGroup.selectAll("circle").data(nodes, function (d) {
        return d.uuid;
      });
      node.exit().remove();
      var nodeEnter = _this.drawnode(node);
      node = nodeEnter.merge(node).text(function (d) {
        return d.name;
      });
      // 更新节点文字
      var nodetext = _this.nodetextGroup.selectAll("text").data(nodes, function (d) {
        return d.uuid
      });
      nodetext.exit().remove();
      var nodetextEnter = _this.drawnodetext(nodetext);
      nodetext = nodetextEnter.merge(nodetext).text(function (d) {
        if (typeof (d.name) == 'undefined') return '';
        if (d.name.length > 4) {
          var s = d.name.slice(0, 3) + "...";
          return s;
        }
        return d.name;
      });
      nodetext.append("title")// 为每个节点设置title
        .text(function (d) {
          return d.name;
        });
      // 更新节点标识
      var nodesymbol = _this.nodesymbolGroup.selectAll("path").data(nodes, function (d) {
        return d.uuid;
      });
      nodesymbol.exit().remove();
      var nodesymbolEnter = _this.drawnodesymbol(nodesymbol);
      nodesymbol = nodesymbolEnter.merge(nodesymbol);
      nodesymbol.attr("fill", "#e15500");
      nodesymbol.attr("display", function (d) {
        if (typeof (d.hasfile) != "undefined" && d.hasfile > 0) {
          return "block";
        }
        return "none";
      })
      _this.simulation.nodes(nodes).on("tick", ticked);
      _this.simulation.force("link").links(links);
      _this.simulation.alphaTarget(1).restart();

      function linkArc(d) {
        var dx = (d.target.x - d.source.x),
          dy = (d.target.y - d.source.y),
          dr = Math.sqrt(dx * dx + dy * dy),
          unevenCorrection = (d.sameUneven ? 0 : 0.5);
        var curvature = 2,
          arc = (1.0 / curvature) * ((dr * d.maxSameHalf) / (d.sameIndexCorrected - unevenCorrection));
        if (d.sameMiddleLink) {
          arc = 0;
        }
        var dd = "M" + d.source.x + "," + d.source.y + "A" + arc + "," + arc + " 0 0," + d.sameArcDirection + " " + d.target.x + "," + d.target.y;
        return dd;
      }

      function ticked() {
        link.attr("d", linkArc)
        // 更新节点坐标
        node.attr("cx", function (d) {
          return d.x;
        })
          .attr("cy", function (d) {
            return d.y;
          });
        // 更新节点操作按钮组坐标
        nodebutton.attr("cx", function (d) {
          return d.x;
        })
          .attr("cy", function (d) {
            return d.y;
          });
        nodebutton.attr("transform", function (d) {
          return "translate(" + d.x + "," + d.y + ") scale(1)";
        })

        // 更新文字坐标
        nodetext.attr("x", function (d) {
          return d.x;
        })
          .attr("y", function (d) {
            return d.y;
          });
        // 更新回形针坐标
        nodesymbol.attr("transform", function (d) {
          return "translate(" + (d.x + 8) + "," + (d.y - 30) + ") scale(0.015,0.015)";
        })
      }

      // 鼠标滚轮缩放
      //_this.svg.call(d3.zoom().transform, d3.zoomIdentity);//缩放至初始倍数
      _this.svg.call(d3.zoom().on("zoom", function () {
        d3.select('#link_menubar').style('display', 'none');
        d3.select('#nodedetail').style('display', 'none');
        d3.selectAll('.node').attr("transform", d3.event.transform);
        d3.selectAll('.nodetext').attr("transform", d3.event.transform);
        d3.selectAll('.line').attr("transform", d3.event.transform);
        d3.selectAll('.linetext').attr("transform", d3.event.transform);
        d3.selectAll('.nodesymbol').attr("transform", d3.event.transform);
        d3.selectAll('.nodebutton').attr("transform", d3.event.transform);
        //_this.svg.selectAll("g").attr("transform", d3.event.transform);
      }));
      _this.svg.on("dblclick.zoom", null); // 静止双击缩放
      //按钮组事件
      _this.svg.selectAll(".buttongroup").on("click", function (d, i) {
        if (_this.nodebuttonAction) {
          switch (_this.nodebuttonAction) {
            case "EDIT":
              _this.isedit = true;
              _this.propactiveName = 'propedit';
              _this.txx = d.x;
              _this.tyy = d.y;
              break;
            case "MORE":
              _this.getmorenode(d);
              break;
            case "LINK":
              _this.isaddlink = true;
              _this.selectsourcenodeid = d.uuid;
              break;
            case "DELETE":
              _this.selectnodeid = d.uuid;
              var out_buttongroup_id = '.out_buttongroup_' + i;
              _this.deletenode(out_buttongroup_id);
              break;
          }
          let ACTION = '';//重置 ACTION
        }

      });
      //按钮组事件绑定
      _this.svg.selectAll(".action_0").on("click", function (d) {
        _this.nodebuttonAction = 'EDIT';
      });
      _this.svg.selectAll(".action_1").on("click", function (d) {
        _this.nodebuttonAction = 'MORE';
      });
      _this.svg.selectAll(".action_2").on("click", function (d) {
        _this.nodebuttonAction = 'LINK';
      });
      _this.svg.selectAll(".action_3").on("click", function (d) {
        _this.nodebuttonAction = 'DELETE';
      });
    },

    requestFullScreen() {
      var element = document.getElementById("graphcontainerdiv");
      var width = window.screen.width;
      var height = window.screen.height;
      this.svg.attr("width", width);
      this.svg.attr("height", height);
      if (element.requestFullscreen) {
        element.requestFullscreen();
      }
      // FireFox
      else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      }
      // Chrome等
      else if (element.webkitRequestFullScreen) {
        element.webkitRequestFullScreen();
      }
      // IE11
      else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },

    saveproperties() {
      // alert(JSON.stringify(this.properties))
      var _this = this;
      var data = {
        fileID: _this.currentFile.id,
        label: _this.domain,
        id: _this.graphEntity.uuid,
        properties: JSON.stringify(_this.properties)
      };
      _this.$api.kgManager.saveProperties(data).then((result) => {
        if (result.code === 200) {
          _this.$message({
            message: '操作成功',
            type: 'success'
          });
        } else {
          _this.$message(
            {
              message: result.msg,
              type: 'error'
            }
          )
        }
      }).catch((e) => {
        _this.$message.error("saveproperties: " + e);
      });
    },

    createnode() {
      var _this = this;
      var data = {
        domain: _this.domain,
        fileID: _this.currentFile.id,
        name: _this.graphEntity.name
      };
      _this.$api.kgManager.createNode(data).then((result) => {
        if (result.code === 200) {
          if (_this.graphEntity.uuid !== 0) {
            for (var i = 0; i < _this.graph.nodes.length; i++) {
              if (_this.graph.nodes[i].uuid === _this.graphEntity.uuid) {
                _this.graph.nodes.splice(i, 1);
              }
            }
          }
          var newnode = result.data;
          newnode.x = _this.txx;
          newnode.y = _this.tyy;
          newnode.fx = _this.txx;
          newnode.fy = _this.tyy;
          _this.graph.nodes.push(newnode);
          _this.resetentity();
          _this.updategraph();
          _this.isedit = false;
          _this.resetsubmit();
        }
      }).catch((e) => {
        _this.$message.error("createnode: " + e);
      });
    },

    resetentity() {
      this.graphEntity = {
        fileID: this.currentFile.id,
        uuid: 0,
        color: 'ff4500',
        name: '',
        r: 30,
        x: '',
        y: ''
      };
    },

    resetsubmit() {
      this.isaddnode = false;
      this.isedit = false;
      this.resetentity();
      this.fieldDataList = [];
      this.dataconfigactive = '';
      this.isbatchcreate = false;
      this.selectnodeid = 0;
    },

    newProperty() {
      if ({} === this.properties) return
      this.properties[document.getElementById("newPropertyInput").value] = "";
      this.propertyCreate = false;
    },

    addnodebutton(r) {
      //先删除所有为节点自定义的按钮组
      d3.selectAll("svg >defs").remove();
      var nodes = this.graph.nodes;
      var database = [1, 1, 1, 1];
      var pie = d3.pie();
      var piedata = pie(database);
      var nodebutton = this.svg.append("defs");
      nodes.forEach(function (m) {
        var nbtng = nodebutton.append("g")
          .attr("id", "out_circle" + m.uuid);//为每一个节点定制一个按钮组，在画按钮组的时候为其指定该id
        var buttonEnter = nbtng.selectAll(".buttongroup")
          .data(piedata)
          .enter()
          .append("g")
          .attr("class", function (d, i) {
            return "action_" + i;
          });
        var defaultR = 30;
        if (typeof (m.r) == 'undefined') {
          m.r = defaultR;
        }
        var arc = d3.arc()
          .innerRadius(m.r)
          .outerRadius(m.r + 30);
        buttonEnter.append("path")
          .attr("d", function (d) {
            return arc(d)
          })
          .attr("fill", "#D2D5DA")
          .style("opacity", 0.6)
          .attr("stroke", "#f0f0f4")
          .attr("stroke-width", 2);
        buttonEnter.append("text")
          .attr("transform", function (d, i) {
            return "translate(" + arc.centroid(d) + ")";
          })
          .attr("text-anchor", "middle")
          .text(function (d, i) {
            var zi = new Array()
            zi[0] = "编辑";
            zi[1] = "展开";
            zi[2] = "连线";
            zi[3] = "删除";
            return zi[i]
          })
          .attr("font-size", 10);
      })
    },

    btnaddsingle() {
      d3.select('.graphcontainer').style("cursor", "crosshair");//进入新增模式，鼠标变成＋
    },

    getProperties(type, id) {
      var _this = this;
      var data = {
        fileID: _this.currentFile.id,
        label: type,
        id: id
      };
      _this.$api.kgManager.getProperties(data).then((result) => {
        if (result.code === 200) {
          _this.properties = result.data;
        }
      }).catch((e) => {
        _this.$message.error("getProperties: " + e);
      });

    },

    getcurrentnodeinfo(node) {
      var _this = this;
      var data = {
        fileID: _this.currentFile.id,
        domain: _this.domain,
        nodeid: node.uuid
      };
      _this.$api.kgManager.getRelationNodeCount(data).then((result) => {
        if (result.code === 200) {
          _this.selectnode.name = node.name;
          _this.selectnode.count = result.data;
        }
      }).catch((e) => {
        _this.$message.error("getcurrentnodeinfo: " + e);
      });
    },

    getmorenode(d) {
      var _this = this;
      var data = {
        fileID: _this.currentFile.id,
        domain: d.type,
        nodeid: _this.selectnodeid
      };
      _this.$api.kgManager.getMoreRelationNode(data).then((result) => {
        if (result.code === 200) {
          var newnodes = result.data.node;
          var newships = result.data.relationship;
          var oldnodescount = _this.graph.nodes.length;
          newnodes.forEach(function (m) {
            var sobj = _this.graph.nodes.find(function (x) {
              return x.uuid === m.uuid
            })
            if (typeof (sobj) == "undefined") {
              _this.graph.nodes.push(m);
            }
          })
          var newnodescount = _this.graph.nodes.length;
          if (newnodescount <= oldnodescount) {
            _this.$message({
              message: '没有更多节点信息',
              type: 'success'
            });
            return;
          }
          newships.forEach(function (m) {
            var sobj = _this.graph.links.find(function (x) {
              return x.uuid === m.uuid
            })
            if (typeof (sobj) == "undefined") {
              _this.graph.links.push(m);
            }
          })
          _this.updategraph();
        }
      }).catch((e) => {
        _this.$message.error("getmorenode: " + e);
      });
    },

    createSingleNode() {
      var _this = this;
      var data = {
        fileID: _this.currentFile.id,
        name: '',
      };

      if (_this.domain === "ALL" || _this.domain === "") {
        _this.$prompt('请输入节点类型', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputValue: this.selectlinkname
        }).then(function (res) {
          data.domain = res.value;
          _this.$api.kgManager.createNode(data).then((result) => {
            if (result.code === 200) {
              d3.select('.graphcontainer').style("cursor", "");
              var newnode = result.data;
              newnode.x = _this.txx;
              newnode.y = _this.tyy;
              newnode.fx = _this.txx;
              newnode.fy = _this.tyy;
              _this.graph.nodes.push(newnode);
              _this.updategraph();
              _this.getlabels();
            }
          }).catch((e) => {
            _this.$message.error("createSingleNode: " + e);
          });
        }).catch((e) => {
          _this.$message.error(e);
        });
      } else {
        data.domain = _this.domain;
        _this.$api.kgManager.createNode(data).then((result) => {
          if (result.code === 200) {
            d3.select('.graphcontainer').style("cursor", "");
            var newnode = result.data;
            newnode.x = _this.txx;
            newnode.y = _this.tyy;
            newnode.fx = _this.txx;
            newnode.fy = _this.tyy;
            _this.graph.nodes.push(newnode);
            _this.updategraph();
          }
        }).catch((e) => {
          _this.$message.error("createSingleNode: " + e);
        });
      }
    },
    deletenode(out_buttongroup_id) {
      var _this = this;
      _this.$confirm('此操作将删除该节点及周边关系(不可恢复), 是否继续?', '三思而后行', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function () {
        var data = {
          fileID: _this.currentFile.id,
          domain: _this.domain,
          nodeid: _this.selectnodeid
        };
        _this.$api.kgManager.deleteNode(data).then((result) => {
          if (result.code === 200) {
            _this.svg.selectAll(out_buttongroup_id).remove();
            var rships = result.data;
            // 删除节点对应的关系
            for (var m = 0; m < rships.length; m++) {
              for (var i = 0; i < _this.graph.links.length; i++) {
                if (_this.graph.links[i].uuid === rships[m].uuid) {
                  _this.graph.links.splice(i, 1);
                  i = i - 1;
                }
              }
            }
            // 找到对应的节点索引
            var j = -1;
            for (var i = 0; i < _this.graph.nodes.length; i++) {
              if (_this.graph.nodes[i].uuid === _this.selectnodeid) {
                j = i;
                break;
              }
            }
            if (j >= 0) {
              _this.selectnodeid = 0;
              _this.graph.nodes.splice(i, 1);// 根据索引删除该节点
              _this.updategraph();
              _this.resetentity();
              _this.$message({
                type: 'success',
                message: '操作成功!'
              });
            }
          }
        }).catch((e) => {
          _this.$message.error("deletenode: " + e);
        });

      }).catch(function () {
        _this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },

    deletelink() {
      var _this = this;
      this.$confirm('此操作将删除该关系(不可恢复), 是否继续?', '三思而后行', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function () {
        var data = {
          fileID: _this.currentFile.id,
          domain: _this.domain,
          shipid: _this.selectnodeid
        };
        _this.$api.kgManager.deleteLink(data).then((result) => {
          if (result.code === 200) {
            var j = -1;
            for (var i = 0; i < _this.graph.links.length; i++) {
              if (_this.graph.links[i].uuid === _this.selectnodeid) {
                j = i;
                break;
              }
            }
            if (j >= 0) {
              _this.selectnodeid = 0;
              _this.graph.links.splice(i, 1);
              _this.updategraph();
              _this.isdeletelink = false;
            }
          }
        }).catch((e) => {
          _this.$message.error("deletelink: " + e);
        });

      }).catch(function () {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    createlink(sourceId, targetId, ship) {
      var _this = this;
      _this.$prompt('请输入关系类型', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: this.selectlinkname
      }).then(function (res) {
        var data = {
          fileID: _this.currentFile.id,
          domain: res.value,
          sourceid: sourceId,
          targetid: targetId,
          ship: ''
        };
        _this.$api.kgManager.createLink(data).then((result) => {
          if (result.code === 200) {
            var newship = result.data;
            _this.graph.links.push(newship);
            _this.updategraph();
            _this.isaddlink = false;
          }
        }).catch((e) => {
          _this.$message.error("createlink: " + e);
        });
      });
    },

    updatelinkName() {
      var _this = this;
      _this.$prompt('请输入关系名称', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: this.selectlinkname
      }).then(function (res) {
        value = res.value;
        var data = {
          fileID: _this.currentFile.id,
          domain: _this.domain,
          shipid: _this.selectnodeid,
          shipname: value
        };
        _this.$api.kgManager.updateLink(data).then((result) => {
          if (result.code === 200) {
            var newship = result.data;
            _this.graph.links.forEach(function (m) {
              if (m.uuid === newship.uuid) {
                m.name = newship.name;
              }
            });
            _this.selectnodeid = 0;
            _this.updategraph();
            _this.isaddlink = false;
            _this.selectlinkname = '';
          }
        }).catch((e) => {
          _this.$message.error("updatelink: " + e);
        });

      }).catch(function () {
        _this.$message({
          type: 'info',
          message: '取消输入'
        });
      });
    },
    updatenodename(d) {
      var _this = this;
      _this.$prompt('编辑节点名称', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: d.name
      }).then(function (res) {
        value = res.value;
        var data = {
          fileID: _this.currentFile.id,
          domain: d.type,
          nodeid: d.uuid,
          nodename: value
        };
        _this.$api.kgManager.getRelationNodeCount(data).then((result) => {
          if (result.code === 200) {
            if (d.uuid !== 0) {
              for (var i = 0; i < _this.graph.nodes.length; i++) {
                if (_this.graph.nodes[i].uuid === d.uuid) {
                  _this.graph.nodes[i].name = value;
                }
              }
            }
            _this.updategraph();
            _this.$message({
              message: '操作成功',
              type: 'success'
            });
          }
        }).catch((e) => {
          _this.$message.error("updatenodename: " + e);
        });
      }).catch(function () {
        _this.$message({
          type: 'info',
          message: '取消操作'
        });
      });
    },

    addmaker() {
      var arrowMarker = this.svg.append("marker")
        .attr("id", "arrow")
        .attr("markerUnits", "strokeWidth")
        .attr("markerWidth", "20")//
        .attr("markerHeight", "20")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", "22")// 13
        .attr("refY", "0")
        .attr("orient", "auto");
      var arrow_path = "M0,-5L10,0L0,5";// 定义箭头形状
      arrowMarker.append("path").attr("d", arrow_path).attr("fill", "#fce6d4");
    },

    drawnode(node) {
      var _this = this;
      var nodeEnter = node.enter().append("circle");
      nodeEnter.attr("r", function (d) {
        if (typeof (d.r) != "undefined" && d.r !== '') {
          return d.r
        }
        return 30;
      });
      nodeEnter.attr("fill", function (d) {
        if (typeof (d.color) != "undefined" && d.color !== '') {
          return d.color
        }
        return "#ff4500";
      });
      nodeEnter.style("opacity", 0.8);
      nodeEnter.style("stroke", function (d) {
        if (typeof (d.color) != "undefined" && d.color !== '') {
          return d.color
        }
        return "#ff4500";
      });
      nodeEnter.style("stroke-opacity", 0.6);
      nodeEnter.append("title")// 为每个节点设置title
        .text(function (d) {
          return d.name;
        })
      nodeEnter.on("mouseover", function (d, i) {
        _this.nodedetail = d;
      });
      nodeEnter.on("dblclick", function (d) {
        _this.updatenodename(d);// 双击更新节点名称
      });
      nodeEnter.on("mouseenter", function (d) {
        var aa = d3.select(this)._groups[0][0];
        if (aa.classList.contains("selected")) return;
        d3.select(this).style("stroke-width", "6");
      });
      nodeEnter.on("mouseleave", function (d) {
        var aa = d3.select(this)._groups[0][0];
        if (aa.classList.contains("selected")) return;
        d3.select(this).style("stroke-width", "2");
      });
      nodeEnter.on("click", function (d, i) {
        d3.select('#nodedetail').style('display', 'block');
        var out_buttongroup_id = '.out_buttongroup_' + i;
        _this.svg.selectAll(".buttongroup").classed("circle_opreate", true);
        _this.svg.selectAll(out_buttongroup_id).classed("circle_opreate", false);
        _this.graphEntity = d;
        _this.selectnodeid = d.uuid;
        _this.selectnodename = d.name;
        // 添加连线状态
        if (_this.isaddlink) {
          _this.selecttargetnodeid = d.uuid;
          if (_this.selectsourcenodeid === _this.selecttargetnodeid || _this.selectsourcenodeid === 0 || _this.selecttargetnodeid === 0) return;
          _this.createlink(_this.selectsourcenodeid, _this.selecttargetnodeid, "RE")
          _this.selectsourcenodeid = 0;
          _this.selecttargetnodeid = 0;
          d.fixed = false
          d3.event.stopPropagation();
        }
      });
      nodeEnter.call(d3.drag()
        .on("start", _this.dragstarted)
        .on("drag", _this.dragged)
        .on("end", _this.dragended));
      return nodeEnter;
    },

    drawnodetext(nodetext) {
      var _this = this;
      var nodetextenter = nodetext.enter().append("text")
        .style("fill", "#fff")
        .attr("dy", 4)
        .attr("font-family", "微软雅黑")
        .attr("text-anchor", "middle")
        .text(function (d) {
          if (typeof (d.name) == 'undefined') return '';
          if (d.name.length > 4) {
            var s = d.name.slice(0, 3) + "...";
            return s;
          }
          return d.name;
        });
      nodetextenter.on("dblclick", function (d) {
        _this.updatenodename(d);// 双击更新节点名称
      });
      nodetextenter.on("click", function (d) {
        document.getElementById("link_menubar").style.display = "none";
        _this.graphEntity = d;
        _this.selectnodeid = d.uuid;
        // 更新工具栏节点信息
        _this.getcurrentnodeinfo(d);
        // 添加连线状态
        if (_this.isaddlink) {
          _this.selecttargetnodeid = d.uuid;
          if (_this.selectsourcenodeid == _this.selecttargetnodeid || _this.selectsourcenodeid == 0 || _this.selecttargetnodeid == 0) return;
          _this.createlink(_this.selectsourcenodeid, _this.selecttargetnodeid, "RE")
          _this.selectsourcenodeid = 0;
          _this.selecttargetnodeid = 0;
          d.fixed = false
          d3.event.stopPropagation();
        }
      });

      return nodetextenter;
    },
    drawnodesymbol(nodesymbol) {
      var _this = this;
      var symnol_path = "M566.92736 550.580907c30.907733-34.655573 25.862827-82.445653 25.862827-104.239787 0-108.086613-87.620267-195.805867-195.577173-195.805867-49.015467 0-93.310293 18.752853-127.68256 48.564907l-0.518827-0.484693-4.980053 4.97664c-1.744213 1.64864-3.91168 2.942293-5.59104 4.72064l0.515413 0.484693-134.69696 133.727573L216.439467 534.8352l0 0 137.478827-136.31488c11.605333-10.410667 26.514773-17.298773 43.165013-17.298773 36.051627 0 65.184427 29.197653 65.184427 65.24928 0 14.032213-5.33504 26.125653-12.73856 36.829867l-131.754667 132.594347 0.515413 0.518827c-10.31168 11.578027-17.07008 26.381653-17.07008 43.066027 0 36.082347 29.16352 65.245867 65.184427 65.245867 16.684373 0 31.460693-6.724267 43.035307-17.07008l0.515413 0.512M1010.336427 343.49056c0-180.25472-145.882453-326.331733-325.911893-326.331733-80.704853 0-153.77408 30.22848-210.418347 79.0528l0.484693 0.64512c-12.352853 11.834027-20.241067 28.388693-20.241067 46.916267 0 36.051627 29.16352 65.245867 65.211733 65.245867 15.909547 0 29.876907-6.36928 41.192107-15.844693l0.38912 0.259413c33.624747-28.030293 76.301653-45.58848 123.511467-45.58848 107.99104 0 195.549867 87.6544 195.549867 195.744427 0 59.815253-27.357867 112.71168-69.51936 148.503893l0 0-319.25248 317.928107 0 0c-35.826347 42.2912-88.654507 69.710507-148.340053 69.710507-107.956907 0-195.549867-87.68512-195.549867-195.805867 0-59.753813 27.385173-112.646827 69.515947-148.43904l-92.18048-92.310187c-65.69984 59.559253-107.700907 144.913067-107.700907 240.749227 0 180.28544 145.885867 326.301013 325.915307 326.301013 95.218347 0 180.02944-41.642667 239.581867-106.827093l0.13312 0.129707 321.061547-319.962453-0.126293-0.13312C968.69376 523.615573 1010.336427 438.71232 1010.336427 343.49056L1010.336427 343.49056 1010.336427 343.49056zM1010.336427 343.49056";// 定义回形针形状
      var nodesymbolEnter = nodesymbol.enter().append("path").attr("d", symnol_path);
      nodesymbolEnter.call(d3.drag()
        .on("start", _this.dragstarted)
        .on("drag", _this.dragged)
        .on("end", _this.dragended));
      return nodesymbolEnter;
    },
    drawnodebutton(nodebutton) {
      var _this = this;
      var nodebuttonEnter = nodebutton.enter().append("g").append("use")//  为每个节点组添加一个 use 子元素
        .attr("r", function (d) {
          return d.r;
        })
        .attr("xlink:href", function (d) {
          return "#out_circle" + d.uuid;
        }) //  指定 use 引用的内容
        .attr('class', function (d, i) {
          return 'buttongroup out_buttongroup_' + i;
        })
        .classed("circle_opreate", true);

      return nodebuttonEnter;
    },
    drawlink(link) {
      var _this = this;
      var linkEnter = link.enter().append("path")
        .attr("stroke-width", 1)
        .attr("stroke", "#fce6d4")
        .attr("fill", "none")
        .attr("id", function (d) {
          return "invis_" + d.lk.sourceid + "-" + d.lk.name + "-" + d.lk.targetid;
        })
        .attr("marker-end", "url(#arrow)");// 箭头
      linkEnter.on("dblclick", function (d) {
        _this.selectnodeid = d.lk.uuid;
        if (_this.isdeletelink) {
          _this.deletelink();
        } else {
          _this.updatelinkName();
        }
      });
      linkEnter.on("contextmenu", function (d) {

        var top = (d.target.y + d.source.y) / 2 + document.getElementById("graphcontainer").getBoundingClientRect().top;
        var left = (d.target.x + d.source.x) / 2 + document.getElementById("graphcontainer").getBoundingClientRect().left;
        _this.selectnodeid = d.lk.uuid;
        _this.selectlinkname = d.lk.name;
        d3.select('#link_menubar')
          .style('position', 'absolute')
          .style('left', left + "px")
          .style('top', top + "px")
          .style('display', 'block');
        d3.event.preventDefault();// 禁止系统默认右键
        d3.event.stopPropagation();// 禁止空白处右键
      });
      linkEnter.on("mouseenter", function (d) {
        d3.select(this).style("stroke-width", "6").attr("stroke", "#ff9e9e").attr("marker-end", "url(#arrow)");
        _this.nodedetail = d.lk;
        d3.select('#nodedetail').style('display', 'block');
      });
      linkEnter.on("mouseleave", function (d) {
        d3.select(this).style("stroke-width", "1").attr("stroke", "#fce6d4").attr("marker-end", "url(#arrow)");
      });
      return linkEnter;
    },
    drawlinktext(link) {
      var _this = this;
      var linktextEnter = link.enter().append('text')
        .style('fill', '#e3af85')
        .append("textPath")
        .attr("startOffset", "50%")
        .attr("text-anchor", "middle")
        .attr("xlink:href", function (d) {
          return "#invis_" + d.lk.sourceid + "-" + d.lk.name + "-" + d.lk.targetid;
        })
        .style("font-size", 14)
        .text(function (d) {
          if (d.lk.name !== '') {
            return d.lk.name;
          }
        });

      linktextEnter.on("mouseover", function (d) {
        _this.selectnodeid = d.lk.uuid;
        _this.selectlinkname = d.lk.name;

        var top = (d.target.y + d.source.y) / 2 + document.getElementById("graphcontainer").getBoundingClientRect().top;
        var left = (d.target.x + d.source.x) / 2 + document.getElementById("graphcontainer").getBoundingClientRect().left;
        d3.select('#link_menubar')
          .style('position', 'absolute')
          .style('left', left + "px")
          .style('top', top + "px")
          .style('display', 'block');
      });

      return linktextEnter;
    },


    dragstarted(d) {
      if (!d3.event.active) this.simulation.alphaTarget(0.3).restart();
      d.fx = d.x;
      d.fy = d.y;
      d.fixed = true;
    },
    dragged(d) {
      d.fx = d3.event.x;
      d.fy = d3.event.y;
    },
    dragended(d) {
    },
  }
}
</script>

<style scoped>
.pl-20 {
  padding-left: 20px
}

#graphcontainerdiv {
  background: #fff;
}

.el-color-picker__panel {
  left: 812px !important;
}


.mind-fj-box {
  display: inline-block;
  width: 290px;
  padding: 5px;
  border: 1px solid #e6e6e6;
  box-shadow: 0 0 8px rgba(206, 205, 201, .38);
}

.node_detail {
  position: absolute;
  width: 100%;
  line-height: 35px;
  -webkit-border-radius: 10px;
  -moz-border-radius: 10px;
  border-radius: 10px;
  font-size: 12px;
  padding-bottom: 10px;
  background: rgba(198, 226, 255, 0.2);
  display: none;
}

.node_pd {
  padding: 4px;
  font-size: 13px;
  font-family: -webkit-body;
  font-weight: 600;
}

.operatetips {
  position: absolute;
  right: 10px;
  float: right;
  top: 0;
  width: 220px;
  padding: 30px;
  border: 2px #EE7942 solid;
  border-radius: 4px;
}

.jsoncontainer {
  position: absolute;
  right: 30%;
  float: right;
  top: 0;
  width: 60%;
  height: 60%;
  padding: 30px;
  border: 2px #EE7942 solid;
  border-radius: 4px;
  background: #fff;
}

.cypher_toolbar {
  line-height: 70px;
  height: 85px;
  padding: 0 22px;
  border-bottom: 1px solid #ededed;
}
</style>
