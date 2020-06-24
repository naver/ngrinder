<template>
    <div class="container position-relative">
        <tip-button v-show="isActiveIntroJSTip" data-placement="bottom"/>
        <tip-button v-show="isActiveEditorShortcutTip"
                      :data-content="getShortcutGuides()"
                      :clickable="false">
        </tip-button>
    </div>
</template>

<script>
    import { Mixins } from 'vue-mixin-decorator';
    import Component from 'vue-class-component';
    import Base from '../Base.vue';
    import GuideMixin from '../script/mixin/Guide.vue';
    import TipButton from './TipButton.vue';
    import { mapState } from 'vuex';
    import { TipType } from "../../constants";

    @Component({
        name: 'tip',
        components: { TipButton },
        computed: {
            ...mapState(
                ['activeTip'],
            ),
        },
    })
    export default class Tip extends Mixins(Base, GuideMixin) {
        getShortcutGuides() {
            return this.shortcutConfigs.reduce((guides, shortcutConfig) =>
                guides += `${shortcutConfig.key} : ${this.i18n(shortcutConfig.desc)}<br>`);
        }

        get isActiveIntroJSTip() {
            return this.activeTip === TipType.INTROJS;
        }

        get isActiveEditorShortcutTip() {
            return this.activeTip === TipType.EDITOR_SHORTCUT;
        }
    }
</script>
