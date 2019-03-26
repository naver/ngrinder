<script>
    import Component from 'vue-class-component';
    import Vue from 'vue';
    import { mapState } from 'vuex';
    import GlobalAttributes from 'global-attributes';
    import I18n from '../i18n.js';

    @Component({
        computed: {
            ...mapState([
                'nGrinderVersion',
            ]),
        }
    })
    export default class Base extends Vue {
        mounted() {
            this.initGlobalAttribtues();
        }

        i18n(key, args) {
            return I18n.i18n(key, args);
        }

        initGlobalAttribtues() {
            this.$store.commit('nGrinderVersion', GlobalAttributes.get('nGrinderVersion'));
        }
    }
</script>

<style lang="less">
    #ngrinder-main-content-wrapper {
        .clickable {
            cursor: pointer;
        }

        .pagination {
            float: right;
            margin: 0;
        }

        .popover {
            width: auto;
            min-width: 200px;
            max-width: 600px;
            max-height: 500px;
        }

        .display-inline {
            display: inline;
        }

        .hide {
            display: none;
        }

        .popover {
            width: 300px;
        }
    }
</style>
