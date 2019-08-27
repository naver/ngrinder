var path = require('path');
var CopyWebpackPlugin = require('copy-webpack-plugin');
var MiniCssExtractPlugin = require('mini-css-extract-plugin');
var VueLoaderPlugin = require('vue-loader/lib/plugin');
var webpack = require("webpack");

var outputDir = path.resolve("../ngrinder-controller/build/classes/main/static");

var argv = require('yargs').argv;
var productionBuild = argv.p || false;
var devMode = process.env.NODE_ENV !== 'production';

if (productionBuild) {
    console.log("### production build is enabled. ga is included and javascript is optmized\r");
} else {
    console.log("### production build is disabled.\r");
}

if (argv.w || argv.watch) {
    console.log("### watch is enabled");
}
// If we omit the following line, the env var for module.exports will be undefined. It's weired.
console.log("### passed env is " + JSON.stringify(argv.env));

module.exports = function (env) {
    var ngrinderVersion = "3.5.0-SNAPSHOT";
    if (env !== undefined && env.ngrinderVersion !== undefined) {
        ngrinderVersion = env.ngrinderVersion;
    }
    console.log("### frontend version is " + ngrinderVersion + "\r");

    var webpackConfig = {
        mode: 'production',
        performance: {
            hints: false,
        },
        entry: {
            'app': 'entries/app.js',
        },
        output: {
            path: outputDir,
            publicPath: "/",
            filename: "./js/[name].js",
            chunkFilename: './js/[name].bundle.js',
        },
        resolve: {
            modules: [
                path.join(__dirname, "src/js"),
                "./src/js/components",
                "./src/js/modules",
                "node_modules",
                "./src/less",
                path.join(__dirname, "./../ngrinder-controller/src/main/resources"),
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
                    test: /\.vue$/,
                    loader: 'vue-loader',
                },
                {
                    test: /.properties$/,
                    loader: "java-properties-flat-loader",
                },
                {
                    test: /\.js$/,
                    exclude: [/node_modules/, /3rd-party/],
                    loader: 'babel-loader?cacheDirectory=true',
                    query: {
                        presets: ['es2015', 'es2017', "stage-0"],
                        plugins: ['transform-decorators-legacy'],
                    },
                },
                {
                    test: /\.css$/,
                    use: [{
                        loader: MiniCssExtractPlugin.loader,
                        options: {
                            publicPath: '../',
                        },
                    }, {
                        loader: 'css-loader',
                        options: {
                            sourceMap: devMode,
                        },
                    }],
                },
                {
                    test: /\.less$/,
                    use: [{
                        loader: MiniCssExtractPlugin.loader,
                        options: {
                            publicPath: '../',
                        },
                    }, {
                        loader: 'css-loader',
                        options: {
                            sourceMap: devMode,
                        },
                    }, {
                        loader: 'less-loader',
                        options: {
                            sourceMap: devMode,
                        },
                    }],
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
            new VueLoaderPlugin(),
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
                debug: !productionBuild,
                options: {
                    context: __dirname,
                    htmlLoader: {
                        ignoreCustomFragments: "[/\{\{.*?}}/]",
                    },
                },
            }),
            new webpack.optimize.LimitChunkCountPlugin({
                maxChunks: 10,
            }),
        ],
    };

    if (!productionBuild) {
        console.log("### sourcemap is enabled.\r");
        webpackConfig.devtool = "#inline-source-map";
    } else {
        console.log("### sourcemap is disabled.\r");
    }

    return webpackConfig;
};
