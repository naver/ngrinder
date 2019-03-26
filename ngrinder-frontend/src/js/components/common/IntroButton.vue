<template>
    <div class="pull-right intro-button-container clickable" rel="popover"
         :title="title" data-html="ture" data-placement="left"
         :data-content="i18n('intro.public.button.show')" id="introButton" @click="startIntroJs">
        <code class="intro-button-title" v-text="title"></code>
    </div>
</template>

<script>
    import Base from '../Base.vue';
    import Component from 'vue-class-component';
    import intro from "intro.js";

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
        startIntroJs() {
            intro.introJs().start();
        }
    }
</script>

<style lang="less" scoped>
    .intro-button-title {
        margin-right: -30px;
    }
</style>
