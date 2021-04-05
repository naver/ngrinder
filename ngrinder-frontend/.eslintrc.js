module.exports = {
    root: true,
    env: {
        browser: true,
        node: true,
        es6: true,
    },
    globals: {
        '$': true,
        'window': true,
        'getProcessCount': true,
        'getThreadCount': true,
        'bootbox': true,
    },
    parser: 'vue-eslint-parser',
    parserOptions: {
        parser: '@babel/eslint-parser',
        ecmaVersion: 2017,
        sourceType: 'module'
    },
    plugins: ['vue'],
    extends: [
        'naver',
        'plugin:vue/essential'
    ],
    rules: {
        // 'vue/no-unused-vars': 'error',
        'linebreak-style': 0,
        'indent': 0,
        'class-methods-use-this': 0,
        'max-len': 0,
        'newline-per-chained-call': 0,
        'no-console': 0,
        'no-alert': 0,
        'radix': 0,
        'vue/require-v-for-key': 0,
        'prefer-rest-params': 0,
        'array-element-newline': 0,
        'array-bracket-newline': 0,
        'no-param-reassign': 0,
        'no-return-assign': 0,
        'padding-line-between-statements': 'off',
        'quotes': ['error', 'single'],
        'object-curly-spacing': ['error', 'always'],
    },
};
