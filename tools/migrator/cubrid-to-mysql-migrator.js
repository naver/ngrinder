var Mcm = require('./lib/migrate-cubrid-to-mysql');
var fs = require('fs');
var Q = require('q');

var htCubrid = {
    sHostname: 'sepecify_cubrid_ip',
    sUser: '',
    sPassword: '',
    nPort: 33000,
    sDatabase: 'ngrinder'
};

var htMysql = {
    sHostname: 'sepecify_mysql_ip',
    sUser: '',
    sPassword: '',
    nPort: 3306,
    sDatabase: 'ngrinder',
    bDebug: false
};

var oMcm = new Mcm(htCubrid, htMysql);

oMcm.once('connected', function () {
    var contents = fs.readFileSync('./create-tables.sql', 'utf8');
    var lines = contents.split(/#########################################################/);
    var result = Q();
    for (var each of lines) {
        console.log("creating db ", each);
        result = result.then(oMcm.createTables(each.trim()), (e) => {
            console.log(e)
        });
    }
    result.then(function () {
        console.log("succeeded");
        oMcm.emit("prepared");
    });
});

oMcm.once('prepared', function () {
    // first arg : cubrid query
    // second arg : mysql table name
    // third arg : truncate(delete data from mysql table)
    // forth arg : callback
    console.log("Starting table migration");

    oMcm.migrateByQuery("SELECT * FROM AGENT", 'AGENT', true, function (htResult) {
        console.log('AGENT table migration is done', htResult);
    });

    oMcm.migrateByQuery("SELECT * FROM NUSER", 'NUSER', true, function (htResult) {
        console.log('NUSER migration is done', htResult);
    });

    oMcm.migrateByQuery("SELECT * FROM PERF_TEST", 'PERF_TEST', true, function (htResult) {
        console.log('PERF_TEST migration is done', htResult);
    });

    oMcm.migrateByQuery("SELECT * FROM PERF_TEST_TAG", 'PERF_TEST_TAG', true, function (htResult) {
        console.log('PERF_TEST_TAG migration is done', htResult);
    });

    oMcm.migrateByQuery("SELECT * FROM TAG", 'TAG', true, function (htResult) {
        console.log('TAG migration is done', htResult);
    });

    oMcm.migrateByQuery("SELECT * FROM SHARED_USER", 'SHARED_USER', true, function (htResult) {
        console.log('SHARED_USER migration is done', htResult);
    });
}).once('done', function (htResult) {
    console.log('\nAll done\n', htResult);
    process.exit(0);
});
