<template>
    <div>
        <div class="row form-horizontal">
            <div class="option-title span1"><h5 v-text="i18n('script.option.header.title')"></h5></div>
            <div id="headers" class="option-data span9">
                <div :class="{hide: !showContentTypeHeader}">
                    <input type="text" class="input name span2" placeholder="name" disabled="disabled"
                           v-model="contentTypeHeader.name"> =
                    <autocomplete class="input value span2-3"
                                  placeholder="value"
                                  v-model="contentTypeHeader.value"
                                  :source="contentTypeHeaderValues"></autocomplete>
                    <i class="icon-minus pointer-cursor" title="Delete" @click="showContentTypeHeader = false"></i>
                </div>
                <div v-for="(header, index) in headers">
                    <autocomplete class="input name span2"
                                  placeholder="name"
                                  v-model="header.name"
                                  :source="headerNames"></autocomplete> =
                    <autocomplete class="input value span2-3"
                                  placeholder="value"
                                  v-model="header.value"
                                  :source="headerValues[header.name]">
                    </autocomplete>
                    <i class="icon-minus pointer-cursor" title="Delete" @click="removeHeader(index)"></i>
                </div>
                <i title="Add header" id="addHeaderBtn" class="icon-plus pointer-cursor" @click="addHeader"></i>
            </div>
        </div>
        <hr class="small">
        <div class="row form-horizontal">
            <div class="option-title span1"><h5 v-text="i18n('script.option.cookie.title')"></h5></div>
            <div id="cookies" class="option-data span9">
                <div v-for="(cookie, index) in cookies">
                    <input type="text" class="input name span2" placeholder="name" v-model="cookie.name"> =
                    <input type="text" class="input value sapn2-3" placeholder="value" v-model="cookie.value">

                    <input type="text" class="input domain span2 form-control" placeholder="domain"
                           v-model="cookie.domain">
                    <input type="text" class="input path span2 form-control" placeholder="path" v-model="cookie.path">
                    <i class="icon-minus pointer-cursor" title="Delete" @click="removeCookie(index)"></i>
                </div>
                <i title="Add cookie" id="addCookieBtn" class="icon-plus pointer-cursor" @click="addCookie"></i>
            </div>
        </div>
        <hr class="small">
        <div id="paramRow" class="row form-horizontal" :class="{hide: showRequestBody}">
            <div class="option-title span1"><h5 v-text="i18n('script.option.param.title')"></h5></div>
            <div id="params" class="option-data span9">
                <div v-for="(param, index) in parameters">
                    <input type="text" class="input name span2" placeholder="name" v-model="param.name"> =
                    <input type="text" class="input value sapn2-3" placeholder="value" v-model="param.value">
                    <i class="icon-minus pointer-cursor" title="Delete" @click="removeParameter(index)"></i>
                </div>
                <i title="Add param" id="addParamBtn" class="icon-plus pointer-cursor" @click="addParameter"></i>
            </div>
        </div>
        <div id="body-row" class="row" :class="{hide: !showRequestBody}">
            <div class="option-title span1"><h5 v-text="i18n('script.option.reqBody.title')"></h5></div>
            <div class="span9">
                <textarea id="reqBodyText" class="span9" rows="6" cols="95" v-model="requestBody"></textarea>
            </div>
        </div>
    </div>
</template>

<script>
    import { Component, Watch } from 'vue-property-decorator';
    import Base from '../../Base.vue';
    import Autocomplete from "../../common/Autocomplete.vue";

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

    @Component({
        name: 'scriptOption',
        components: {Autocomplete},
        props: {
            method: {
                type: String,
                required: true,
            }
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

        @Watch('contentTypeHeader', {deep: true})
        contentTypeHeaderChanged(newContentTypeHeader) {
            if (newContentTypeHeader.value === 'application/json') {
                this.showRequestBody = true;
            } else {
                this.showRequestBody = false;
            }
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

    const objectToJson = obj => JSON.parse(JSON.stringify(obj));
</script>

<style lang="less" scoped>
    .option-data {
        padding-top: 10px;
    }

    input[type="text"] {
        height: 30px;
    }

    .icon-minus {
        margin-left: 10px;
    }

    .domain {
        margin-left: 15px;
    }
</style>
