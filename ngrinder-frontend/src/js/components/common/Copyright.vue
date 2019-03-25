<template>
    <div id="foot_div">
        <div class="credit">
            <a href="https://naver.github.io/ngrinder" target="_blank" v-text="`nGrinder v${nGrinderVersion}`"></a>
        </div>
    </div>
</template>

<script>
    import Component from 'vue-class-component';

    @Component({
        name: 'copyright',
        props: {
            nGrinderVersion: {
                type: String,
                required: true,
            },
        },
    })
    export default class Copyright {}
</script>

<style lang="less" scoped>
    #foot_div {
        position: fixed;
        left: 0;
        bottom: 0;
        width: 100%;

        .credit {
            height: 100%;
            margin: 0;
            display: flex;
            align-items: center;
            justify-content: center;
        }
    }
</style>
