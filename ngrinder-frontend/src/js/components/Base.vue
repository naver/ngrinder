<script>
    import Component from 'vue-class-component';
    import Vue from 'vue';
    import { mapState } from 'vuex';
    import GlobalAttributes from 'global-attributes';

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

        initGlobalAttribtues() {
            this.$store.commit('nGrinderVersion', GlobalAttributes.get('nGrinderVersion'));
        }
    }
</script>

<style lang="less">

</style>
