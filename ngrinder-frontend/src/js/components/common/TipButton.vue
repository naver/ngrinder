<template>
    <div class="intro-button-container"
         :title="title"
         data-html="true"
         data-toggle="popover"
         data-trigger="hover"
         :class="{ 'pointer-cursor': clickable, 'default-cursor': !clickable }"
         :data-placement="dataPlacement"
         :data-content="dataContent ? dataContent : i18n('intro.public.button.show')" @click="startIntroJs">
        <code class="intro-button-title" v-text="title"></code>
    </div>
</template>

<script>
    import Component from 'vue-class-component';
    import intro from 'intro.js';
    import Base from '../Base.vue';

    @Component({
        name: 'tipButton',
        props: {
            title: {
                type: String,
                default: 'TIP',
            },
            dataPlacement: {
                type: String,
                default: 'bottom',
            },
            dataContent: {
                type: String,
                default: '',
            },
            clickable: {
                type: Boolean,
                default: true,
            }
        },
    })
    export default class TipButton extends Base {
        mounted() {
            $('[data-toggle="popover"]').popover();
        }

        startIntroJs() {
            if (!this.clickable) {
                return;
            }
            intro.introJs().start();
        }
    }
</script>

<style lang="less" scoped>
    @import '~intro.js/introjs.css';

    .intro-button-container {
        position: absolute;
        width: 56px;
        top: 57px;
        right: -39px;
        -webkit-transform: rotate(90deg);
        -moz-transform: rotate(90deg);
        -o-transform: rotate(90deg);
        -ms-transform: rotate(90deg);
        transform: rotate(90deg);

        code {
            width: 100%;
            height: 22px;
            padding: 2px 4px;
            text-align: center;
            display: inline-block;
            letter-spacing: 2px;
        }
    }
</style>
