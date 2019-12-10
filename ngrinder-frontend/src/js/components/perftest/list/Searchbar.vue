<template>
    <div class="search-bar clearfix">
        <div class="float-left">
            <div data-step="3" data-position="top" :data-intro="i18n('intro.list.search')">
                <select2 v-model="selectedTag" :value="selectedTag" @change="$emit('change-tag')"
                         name="tagSelect" ref="tagSelect"
                         :option="{placeholder: i18n('perfTest.action.selectATag'), allowClear: true}">
                    <option value=""></option>
                    <option v-for="tag in userTags" v-text="tag" :value="tag"></option>
                </select2>
                <input type="search" name="search" class="search-query-without-radios form-control"
                       placeholder="Keywords" v-model="searchText" @keydown.enter="$emit('search')">
                <button class="btn btn-info align-baseline" @click="$emit('search')">
                    <i class="fa fa-search mr-1"></i>
                    <span v-text="i18n('common.button.search')"></span>
                </button>
                <label class="checkbox-label">
                    <input type="checkbox" class="align-middle" @click="$emit('filter-running', {enable: !running, token: 'R'})" v-model="running">
                    <span v-text="i18n('perfTest.action.running')"></span>
                </label>
                <label class="checkbox-label">
                    <input type="checkbox" class="align-middle" @click="$emit('filter-schduled', {enable: !scheduled, token: 'S'})" v-model="scheduled">
                    <span v-text="i18n('perfTest.action.scheduled')"></span>
                </label>
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
    import Component from 'vue-class-component';
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

        .form-control {
            display: inline-block;
        }

        .search-query-without-radios {
            width: 170px;
        }

        .checkbox-label {
            position: relative;
            margin-left: 5px;
        }

        * {
            font-size: 12px;
        }
    }
</style>
