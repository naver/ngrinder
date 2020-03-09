<template>
    <div class="intro-button-container float-right pointer-cursor"
         :title="title"
         data-html="true"
         data-toggle="popover"
         data-placement="left"
         data-trigger="hover"
         :data-content="i18n('intro.public.button.show')" @click="startIntroJs">
        <code class="intro-button-title" v-text="title"></code>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import intro from 'intro.js';
    import Base from '../Base.vue';

    @Component({
        name: 'introButton',
        props: {
            title: {
                type: String,
                default: 'Tip',
            },
        },
    })
    export default class IntroButton extends Base {
        mounted() {
            $('[data-toggle="popover"]').popover();
        }

        startIntroJs() {
            intro.introJs().start();
        }
    }
</script>

<style lang="less" scoped>
    @import '~intro.js/introjs.css';

    .intro-button-container {
        .intro-button-title {
            margin-right: -30px;
        }

        code {
            position: absolute;
            padding: 0 4px !important;
        }
    }
</style>
