<template>
  <div>
    <el-dialog id="tag-editor" title="文件标签编辑" :visible.sync="tagEditorShow">
      <div style="padding-bottom: 10px">
        <el-select v-model="currentTagPrefix" placeholder="请选择" size="small">
          <el-option-group
            v-for="group in tagPrefix"
            :key="group.label"
            :label="group.label">
            <el-option
              v-for="item in group.options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-option-group>
        </el-select>
      </div>
      <div style="padding-bottom: 10px">
        <el-select
          allow-create
          size="small"
          v-model="currentTagSuffix"
          filterable
          remote
          reserve-keyword
          placeholder="请输入关键词"
          :remote-method="getRelatedTags"
          :loading="searchLoading">
          <el-option
            v-for="item in related_tags"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <el-button type="primary" class="button-new-tag" size="small" @click="handleInputConfirm">添加标签</el-button>
      </div>
      <el-tag
        :key="tag"
        v-for="tag in tags"
        closable
        :disable-transitions="false"
        @close="handleDelete(tag)"
        size="medium">
        {{ tag }}
      </el-tag>

    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "TagEditor.vue",
  data() {
    return {
      currentTagSuffix: '',
      currentTagPrefix: '默认',
      tagPrefix: [{
        label: '常用',
        options: [{
          value: '模板',
          label: '模板'
        }, {
          value: '历史文件',
          label: '历史文件'
        }]
      }, {
        label: '默认',
        options: [{
          value: '默认',
          label: '默认'
        }]
      }],
      tagEditorShow: this.tagEditorShow,
      fileID: this.fileID,
      tags: [],
      related_tags: [],
      inputVisible: false,
      inputValue: '',
      searchLoading: false,
    }
  },
  props: {
    tagEditorShow: {
      type: Boolean,
      required: true,
    },
    fileID: {
      type: String,
      required: true,
    },
  },
  created() {
    this.getAllTags()
  },
  mounted() {
    this.getFileTags()
  },
  methods: {
    getRelatedTags(query) {
      this.searchLoading = true;
      let _this = this;
      let data = {
        input: query,
        num: 10,
      }
      _this.$api.tagEditor.getRelatedLabels(data).then((result) => {
        if (result.code === 200) {
          let _tags = []
          for (const d of result.data) {
            let suffix
            if (d.indexOf(':') != -1) {
              suffix = d.split(":").slice(1)
            } else {
              suffix = d
            }
            let option = {
              value: suffix,
              label: d
            }
            _tags.push(option);
          }
          _this.related_tags = _tags
          _this.searchLoading = false
        }
      }).catch((e) => {
        _this.searchLoading = false;
        _this.$message.error("getRelatedTags: " + e);
      });
    },
    getAllTags() {
      let _this = this;
      _this.$api.tagEditor.getLabels().then((result) => {
        if (result.code === 200) {
          let _tags = []
          for (const d of result.data) {
            let option = {
              value: d.name,
              label: d.name
            }
            _tags.push(option);
          }
          _this.all_tags = _tags
        }
      }).catch((e) => {
        //_this.$message.error("getAllTags: "+e);
      });
    },
    getFileTags() {
      let _this = this;
      let data = {
        fileID: _this.fileID
      }
      _this.$api.tagEditor.getLabels(data).then((result) => {
        if (result.code === 200) {
          let _tags = []
          for (const d of result.data) {
            _tags.push(d.name);
          }
          _this.tags = _tags
        }
      }).catch((e) => {
        //_this.$message.error("getFileTags: "+e);
      });
    },

    handleDelete(tag) {
      let _this = this;
      let data = {
        fileID: _this.fileID,
        name: tag
      }
      _this.$api.tagEditor.deleteFileLabel(data).then((result) => {
        if (result.code === 200) {
          this.tags.splice(this.tags.indexOf(tag), 1);
        }
      }).catch((e) => {
        _this.$message.error("getFileTags: " + e);
      });
    },

    handleInputConfirm() {
      let tag
      if (this.currentTagPrefix === "默认") {
        tag = this.currentTagSuffix
      } else {
        tag = this.currentTagPrefix + ':' + this.currentTagSuffix
      }
      if (tag) {
        let _this = this;
        let data = {
          fileID: _this.fileID,
          name: tag
        }
        _this.$api.tagEditor.createLabel(data).then((result) => {
          if (result.code === 200) {
            _this.tags.push(tag);
          }
        }).catch((e) => {
          _this.$message.error("handleInputConfirm: " + e);
        });

      }
      this.inputVisible = false;
      this.inputValue = '';
    }
  },
  watch: {
    tagEditorShow: function (val) {
      this.currentTagPrefix = '默认'
      this.$emit('tagEditorShow', val)
    },
    fileID: function () {
      this.getFileTags();
    }
  }
}
</script>

<style scoped>
.el-tag {
  margin: 5px;
}

.button-new-tag {
  margin: 5px;
  height: 30px;
  line-height: 28px;
  padding-top: 0;
  padding-bottom: 0;
}


</style>
