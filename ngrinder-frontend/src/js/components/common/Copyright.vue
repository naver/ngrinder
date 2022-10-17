<template>
    <footer class="w-100">
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
        height: 40px;
        margin-top: 7px;
        background-color: #f5f4f2;

        section {
            height: inherit;
            margin: 0;
            align-items: center;
            justify-content: center;

            a:hover {
                text-decoration: none;
            }
        }
    }
</style>
