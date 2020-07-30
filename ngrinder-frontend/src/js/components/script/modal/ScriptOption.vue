<template>
    <div class="script-option">
        <div class="row form-horizontal py-3">
            <div class="option-title">
                <h5 class="my-0" v-text="i18n('script.option.header.title')"></h5>
            </div>
            <div>
                <div v-show="showContentTypeHeader" class="mb-2">
                    <input type="text" class="form-control" placeholder="name" disabled="disabled"
                           v-model="contentTypeHeader.name"> =
                    <autocomplete placeholder="value"
                                  v-model="contentTypeHeader.value"
                                  :source="contentTypeHeaderValues">
                    </autocomplete>
                    <i class="fa fa-minus ml-2 pointer-cursor" title="Delete" @click="showContentTypeHeader = false"></i>
                </div>
                <div v-for="(header, index) in headers" class="mb-2">
                    <autocomplete placeholder="name"
                                  v-model="header.name"
                                  :source="headerNames">
                    </autocomplete> =
                    <autocomplete placeholder="value"
                                  v-model="header.value"
                                  :source="headerValues[header.name]">
                    </autocomplete>
                    <i class="fa fa-minus ml-2 pointer-cursor" title="Delete" @click="removeHeader(index)"></i>
                </div>
                <i title="Add header" class="fa fa-plus pointer-cursor" @click="addHeader"></i>
            </div>
        </div>
        <hr class="small">
        <div class="row form-horizontal py-3">
            <div class="option-title">
                <h5 class="my-0" v-text="i18n('script.option.cookie.title')"></h5>
            </div>
            <div>
                <div v-for="(cookie, index) in cookies" class="mb-2">
                    <input type="text" class="form-control" placeholder="name" v-model="cookie.name"> =
                    <input type="text" class="form-control" placeholder="value" v-model="cookie.value">

                    <input type="text" class="form-control" placeholder="domain" v-model="cookie.domain">
                    <input type="text" class="form-control" placeholder="path" v-model="cookie.path">
                    <i class="fa fa-minus ml-2 pointer-cursor" title="Delete" @click="removeCookie(index)"></i>
                </div>
                <i title="Add cookie" class="fa fa-plus pointer-cursor" @click="addCookie"></i>
            </div>
        </div>
        <hr class="small">
        <div class="row form-horizontal py-3" v-show="!showRequestBody">
            <div class="option-title">
                <h5 class="my-0" v-text="i18n('script.option.param.title')"></h5>
            </div>
            <div>
                <div v-for="(param, index) in parameters" class="mb-2">
                    <input type="text" class="form-control" placeholder="name" v-model="param.name"> =
                    <input type="text" class="form-control" placeholder="value" v-model="param.value">
                    <i class="fa fa-minus ml-2 pointer-cursor" title="Delete" @click="removeParameter(index)"></i>
                </div>
                <i title="Add param" class="fa fa-plus pointer-cursor" @click="addParameter"></i>
            </div>
        </div>
        <div class="row" v-show="showRequestBody">
            <div class="option-title">
                <h5 class="my-0" v-text="i18n('script.option.reqBody.title')"></h5>
            </div>
            <div>
                <textarea rows="6" cols="95" v-model="requestBody"></textarea>
            </div>
        </div>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../../Base.vue';
    import Autocomplete from '../../common/Autocomplete.vue';

    class Pair {
        constructor(name, value) {
            this.name = name;
            this.value = value;
        }
    }

    class Cookie {
        constructor(name, value, domain, path) {
            this.name = name;
            this.value = value;
            this.domain = domain;
            this.path = path;
        }
    }

    const objectToJson = obj => JSON.parse(JSON.stringify(obj));

    @Component({
        name: 'scriptOption',
        components: { Autocomplete },
        props: {
            method: {
                type: String,
                required: true,
            },
        },
    })
    export default class ScriptOption extends Base {
        contentTypeHeader = new Pair('Content-Type', 'application/x-www-form-urlencoded');
        contentTypeHeaderValues = ['application/x-www-form-urlencoded', 'application/json'];

        headerNames = ['Connection', 'User-Agent'];
        headerValues = {
            'Connection': ['keep-alive'],
            'User-Agent': ['Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20100101 Firefox/21.0'],
        };

        headers = [];
        cookies = [];
        parameters = [];
        requestBody = '';

        showContentTypeHeader = false;
        showRequestBody = false;

        @Watch('method')
        methodChanged(newMethod) {
            // eslint-disable-next-line default-case
            switch (newMethod) {
            case 'GET':
                this.showContentTypeHeader = false;
                this.showRequestBody = false;
                break;
            case 'POST':
                this.showContentTypeHeader = true;
                if (this.contentTypeHeader.value === 'application/json') {
                    this.showRequestBody = true;
                }
                break;
            }
        }

        @Watch('contentTypeHeader', { deep: true })
        contentTypeHeaderChanged(newContentTypeHeader) {
            this.showRequestBody = newContentTypeHeader.value === 'application/json';
        }

        addHeader() {
            this.headers.push(new Pair());
        }

        removeHeader(index) {
            this.headers.splice(index, 1);
        }

        addCookie() {
            this.cookies.push(new Cookie());
        }

        removeCookie(index) {
            this.cookies.splice(index, 1);
        }

        addParameter() {
            this.parameters.push(new Pair());
        }

        removeParameter(index) {
            this.parameters.splice(index, 1);
        }

        reset() {
            this.headers = [];
            this.cookies = [];
            this.parameters = [];
            this.requestBody = '';
            this.showContentTypeHeader = false;
            this.showRequestBody = false;
        }

        get toJson() {
            const json = {
                method: this.method,
                headers: objectToJson(this.headers.filter(h => h.name && h.value)),
                cookies: objectToJson(this.cookies.filter(c => c.name && c.value && c.domain && c.path)),
                params: objectToJson(this.parameters.filter(p => p.name && p.value)),
                body: this.requestBody,
            };

            if (this.showRequestBody) {
                delete json.params;
            } else {
                delete json.body;
            }

            if (this.showContentTypeHeader) {
                json.headers = [objectToJson(this.contentTypeHeader), ...json.headers];
            }

            return json;
        }
    }
</script>

<style lang="less" scoped>
    .script-option {
        .option-title {
            width: 60px;
            margin-left: 50px;
            margin-right: 15px;
        }

        input[type="text"] {
            height: 30px;
            width: 140px;
        }

        .domain {
            margin-left: 15px;
        }
    }
</style>
