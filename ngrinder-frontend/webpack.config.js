var path = require('path');
var CopyWebpackPlugin = require('copy-webpack-plugin');
var MiniCssExtractPlugin = require('mini-css-extract-plugin');
var webpack = require('webpack');

var outputDir = path.resolve('../ngrinder-controller/build/classes/main/static');

var argv = require('yargs').argv;
var developmentBuild = argv.d || argv.dev || argv.development || false;

if (developmentBuild) {
    console.log('### production build is disabled.\r');
} else {
    console.log('### production build is enabled. ga is included and javascript is optimized\r');
}

if (argv.w || argv.watch) {
    console.log('### watch is enabled');
}
// If we omit the following line, the env var for module.exports will be undefined. It's weired.
console.log('### passed env is ' + JSON.stringify(argv.env));

var cssLoader = {
    loader: 'css-loader',
    options: {
        sourceMap: developmentBuild,
    },
};

var lessLoader = {
    loader: 'less-loader',
    options: {
        sourceMap: developmentBuild,
    },
};

module.exports = function (env) {
    var ngrinderVersion = '3.5.5-p2';
    if (env !== undefined && env.ngrinderVersion !== undefined) {
        ngrinderVersion = env.ngrinderVersion;
    }
    console.log('### frontend version is ' + ngrinderVersion + '\r');

    var webpackConfig = {
        mode: developmentBuild ? 'development' : 'production',
        node: {
            fs: 'empty'
        },
        performance: {
            hints: false,
        },
        entry: { 'app': ['@babel/polyfill', 'entries/app.js']},
        output: {
            path: outputDir,
            publicPath: '/',
            filename: './js/[name].js',
            chunkFilename: './js/[name].bundle.js',
        },
        resolve: {
            modules: [
                path.join(__dirname, './../ngrinder-controller/src/main/resources'),
                path.join(__dirname, 'src/js'),
                './src/js/components',
                './src/js/modules',
                'node_modules',
            ],
            alias: {
                'vue$': 'vue/dist/vue.esm.js',
            },
        },
        resolveLoader: {
            alias: {
                'scss-loader': 'sass-loader',
            },
        },
        module: {
            rules: [
                {
                    test: /\.js$/,
                    exclude: {
                        test: /node_modules/,
                        not: [
                            /(d3-.*)$/,
                            /(strip-.*)$/,
                            /(.*json.*)$/,
                            /(vuejs-datepicker.*)$/,
                        ],
                    },
                    use: [
                        { loader: 'babel-loader', options: { 'presets': ['@babel/preset-env'] } },
                    ],
                },
                {
                    test: /\.vue$/,
                    loader: 'vue-loader',
                    options: {
                        loaders: {
                            js: {
                                loader: 'babel-loader',
                                options: { 'presets': ['@babel/preset-env'] },
                            },
                            css: [ MiniCssExtractPlugin.loader, cssLoader ],
                            less: [ MiniCssExtractPlugin.loader, cssLoader, lessLoader ],
                        },
                    },
                },
                {
                    test: /.properties$/,
                    loader: 'java-properties-flat-loader',
                },
                {
                    test: /\.css$/,
                    use: [{
                        loader: MiniCssExtractPlugin.loader,
                        options: {
                            publicPath: '../',
                        },
                    }, cssLoader ],
                },
                {
                    test: /\.less$/,
                    use: [{
                        loader: MiniCssExtractPlugin.loader,
                        options: {
                            publicPath: '../',
                        },
                    }, cssLoader, lessLoader ],
                },
                {
                    test: /\.(png|jpg|jpeg|gif|eot|ttf|woff|woff2|svg|svgz)(\?.+)?$/,
                    use: [{
                        loader: 'url-loader',
                        options: {
                            limit: 20000,
                        },
                    }],
                },
            ],
        },
        plugins: [
            new webpack.ProvidePlugin({
                $: 'jquery',
                jQuery: 'jquery',
                'window.jQuery': 'jquery',
            }),
            new MiniCssExtractPlugin({
                filename: 'css/[name].css',
            }),
            new CopyWebpackPlugin([
                {
                    context: 'src/html',
                    from: '**/*',
                    to: '../templates',
                },
                {
                    context: 'src/js/library',
                    from: '**/*',
                    to: 'js/library',
                },
                {
                    context: 'src/js',
                    from: '*',
                    to: 'js',
                },
                {
                    context: 'src/css',
                    from: '**/*',
                    to: 'css',
                },
                {
                    context: 'src/img',
                    from: '**/*',
                    to: 'img',
                },
            ]),
            new webpack.LoaderOptionsPlugin({
                debug: developmentBuild,
                options: {
                    context: __dirname,
                    htmlLoader: {
                        ignoreCustomFragments: '[/\{\{.*?}}/]',
                    },
                },
            }),
            new webpack.optimize.LimitChunkCountPlugin({
                maxChunks: 10,
            }),
        ],
    };

    if (developmentBuild) {
        console.log('### sourcemap is enabled.\r');
        webpackConfig.devtool = "#inline-source-map";
    } else {
        console.log('### sourcemap is disabled.\r');
    }

    return webpackConfig;
};
