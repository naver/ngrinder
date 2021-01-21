<template>
    <div class="search-bar clearfix">
        <div class="float-left">
            <div class="d-flex" data-step="3" data-position="top" :data-intro="i18n('intro.list.search')">
                <select2 v-model="selectedTag" :value="selectedTag" @change="$emit('change-tag')"
                         name="tagSelect" ref="tagSelect"
                         customStyle="margin-bottom: 1px;"
                         :option="{placeholder: i18n('perfTest.action.selectATag'), allowClear: true}">
                    <option value=""></option>
                    <option v-for="tag in userTags" v-text="tag" :value="tag"></option>
                </select2>
                <div class="d-inline-block ml-1">
                    <div class="input-group">
                        <input type="search" name="search" class="input-search-append form-control"
                               placeholder="Keywords" v-model="searchText" @keydown.enter="$emit('search')" v-focus>
                        <div class="input-group-append">
                            <button class="btn btn-info align-baseline" @click="$emit('search')">
                                <i class="fa fa-search mr-1"></i>
                                <span v-text="i18n('common.button.search')"></span>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="d-flex align-items-center">
                    <label class="ml-2 mb-0">
                        <input type="checkbox" class="align-middle" v-model="running">
                        <span v-text="i18n('perfTest.action.running')"></span>
                    </label>
                    <label class="ml-2 mb-0">
                        <input type="checkbox" class="align-middle" v-model="scheduled">
                        <span v-text="i18n('perfTest.action.scheduled')"></span>
                    </label>
                </div>
            </div>
        </div>
        <div class="float-right">
            <button class="btn btn-primary align-baseline" @click="$emit('create')" data-position="left" data-step="1"
               :data-intro="i18n('intro.list.create')">
                <i class="fa fa-file mr-1"></i>
                <span v-text="i18n('perfTest.action.createTest')"></span>
            </button>
            <button @click="$emit('delete-selected-tests')" class="btn btn-danger align-baseline" data-position="top"
               data-step="2" :data-intro="i18n('intro.list.delete')">
                <i class="fa fa-remove mr-1"></i>
                <span v-text="i18n('perfTest.action.deleteSelectedTest')"></span>
            </button>
        </div>
    </div>
</template>
<script>
    import { Mixins } from 'vue-mixin-decorator';
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../../Base.vue';
    import Select2 from '../../common/Select2.vue';
    import MessagesMixin from '../../common/mixin/MessagesMixin.vue';

    @Component({
        name: 'searchBar',
        components: { Select2 },
    })
    export default class SearchBar extends Mixins(Base, MessagesMixin) {
        searchText = '';
        selectedTag = '';
        userTags = [];

        running = false;
        scheduled = false;

        @Watch('running')
        filterRunning() {
            this.$emit('filter-running', { enable: this.running, token: 'R' });
        }

        @Watch('scheduled')
        filterScheduled() {
            this.$emit('filter-schduled', { enable: this.scheduled, token: 'S' });
        }

        created() {
            this.getUserTags();
        }

        getUserTags() {
            this.$http.get('/perftest/api/search_tag')
                .then(res => {
                    this.userTags = res.data;
                    this.$nextTick(() => {
                        this.$refs.tagSelect.selectValue(this.selectedTag);
                    });
                })
                .catch(() => this.showErrorMsg(this.i18n('common.message.loading.error',
                    { content: this.i18n('perfTest.list.tags') })));
        }
    }
</script>

<style lang="less">
    .search-bar {
        .select2-container {
            .select2-choice {
                width: 170px;
                height: 29px;
            }
        }
    }
</style>

<style lang="less" scoped>
    .search-bar {
        height: 53px;
        border-radius: 0;
        margin: 0;
        padding: 10px;
        border: 1px solid #e3e3e3;
        border-bottom: none;
        background-color: #f5f5f5;

        .input-search-append{
            display: inline-block;
            width: 250px;
            height: 30px;
        }
    }
</style>
