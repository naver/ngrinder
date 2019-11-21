<template>
    <footer>
        <section class="d-flex">
            <a href="https://naver.github.io/ngrinder" target="_blank" v-text="`nGrinder v${version}`"></a>
        </section>
    </footer>
</template>

<script>
    import Component from 'vue-class-component';

    @Component({
        name: 'copyright',
        props: {
            version: {
                type: String,
                required: true,
            },
        },
    })
    export default class Copyright {}
</script>

<style lang="less" scoped>
    footer {
        min-height: 40px;
        margin-top: 10px;
        width: 100%;
        background-color: #f5f4f2;

        section {
            height: 100%;
            margin: 0;
            align-items: center;
            justify-content: center;

            a:hover {
                text-decoration: none;
            }
        }
    }
</style>
