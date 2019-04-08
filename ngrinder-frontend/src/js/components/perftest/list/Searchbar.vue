<template>
    <div class="well form-inline search-bar no-margin">
        <div class="left-float">
            <div data-step="3" data-position="top" :data-intro="i18n('intro.list.search')">
                <select2 v-model="selectedTag" :value="selectedTag" customStyle="width: 150px" type="select"
                         :option="{placeholder: i18n('perfTest.action.selectATag'), allowClear: true}" @change="$emit('change-tag')">
                    <option value=""></option>
                    <option v-for="tag in userTags" v-text="tag" :value="tag"></option>
                </select2>
                <input type="search" name="search" class="search-query search-query-without-radios span2"
                       placeholder="Keywords" v-model="searchText">
                <button class="btn btn-info" @click="$emit('search')" v-text="i18n('common.button.search')">
                    <i class="glyphicon glyphicon-search"></i>
                </button>
                <label class="checkbox">
                    <input type="checkbox" @click="$emit('filter-running', {enable: !running, token: 'R'})" v-model="running"><span v-text="i18n('perfTest.action.running')"></span>
                </label>
                <label class="checkbox">
                    <input type="checkbox" @click="$emit('filter-schduled', {enable: !scheduled, token: 'S'})" v-model="scheduled"><span v-text="i18n('perfTest.action.scheduled')"></span>
                </label>
            </div>
        </div>

        <div class="right-float">
            <a class="btn btn-primary" v-on:click="createTest" id="create_btn" data-position="left" data-step="1"
               :data-intro="i18n('intro.list.create')" v-text="i18n('perfTest.action.createTest')">
                <i class="glyphicon glyphicon-file icon-white"></i>
            </a>
            <a @click="$emit('delete-selected-tests')" class="pointer-cursor btn btn-danger" id="delete_btn" data-position="top"
               data-step="2" :data-intro="i18n('intro.list.delete')" v-text="i18n('perfTest.action.deleteSelectedTest')">
                <i class="glyphicon glyphicon-remove icon-white"></i>
            </a>
        </div>
    </div>
</template>
<script>
    import Base from '../../Base.vue';
    import Component from 'vue-class-component';
    import Select2 from '../../common/Select2.vue';

    @Component({
        name: 'searchBar',
        components: { Select2 }
    })
    export default class SearchBar extends Base {
        searchText = '';
        selectedTag = '';
        userTags = [];

        running = false;
        scheduled = false;

        created() {
            this.getUserTags();
        }

        getUserTags() {
            this.$http.get('/perftest/api/search_tag').then(res => {
                this.userTags = res.data;
            }).catch(error => console.error(error));
        }

        createTest() {
            // TODO Require perftest detail page
        }
    }
</script>
<style lang="less" scoped>
    .search-bar {
        height: 30px;
        border-radius: 0;
        margin: 0;

        .checkbox {
            position:relative;
            margin-left:5px;
        }
    }

</style>
