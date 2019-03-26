<script>
    import Component from 'vue-class-component';
    import Vue from 'vue';
    import I18n from '../i18n.js';
    import { mapState } from 'vuex';

    @Component({
        computed: {
            ...mapState([
                'version',
                'config',
                'currentUser',
            ]),
        },
    })
    export default class Base extends Vue {
        i18n(key, args) {
            return I18n.i18n(key, args);
        }

        setAttributes(context, data, keys) {
            keys.forEach((key) => {
                context[key] = data[key];
            });
        }

        exist(value) {
            return typeof value !== 'undefined';
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
