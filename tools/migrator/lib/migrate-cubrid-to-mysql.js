/**
 * Module dependencies.
 */
var Cubrid = require('node-cubrid'),
    Result2Array = Cubrid.Result2Array,
    GenericPool = require('generic-pool'),
    Mysql = require('mysql'),
    EventEmitter = require("events").EventEmitter;
var Q = require('q');

/**
 * Export the constructor.
 * @ignore
 */
exports = module.exports = Migrator;

/**
 * Migrator class.
 * @name Migrator
 * @constructor
 */
function Migrator() {
    this.init.apply(this, arguments);
}

Migrator.prototype = {
    /**
     * event.EventEmitter 상속
     * @ignore
     */
    __proto__: EventEmitter.prototype,

    _htCubrid: {
        sHostname: 'localhost',
        sUser: '',
        sPassword: '',
        nPort: 33000,
        sDatabase: ''
    },
    _htMysql: {
        sHostname: 'localhost',
        sUser: '',
        sPassword: '',
        nPort: 3306,
        sDatabase: '',
        bDebug: true
    },
    _oCubrid: null,
    _oMysql: null,

    _bIsCubridConnected: false,
    _bIsMysqlConnected: false,

    _nMigrationCount: 0,
    _nMigrationDoneCount: 0,
    _htMigrationResult: {},

    _oIconv: null,

    _aQueue: [],
    _bStarted: false,

    _aSuccessCount: [],
    _aFailureCount: [],
    _aTotalCount: [],

    init: function (htCubrid, htMysql) {
        this._htCubrid = htCubrid || this._htCubrid;
        this._htMysql = htMysql || this._htMysql;

        this._connectToCubrid();
        this._connectToMysql();
    },
    _connectToCubrid: function () {
        var self = this;
        this._bIsCubridConnected = false;

        this._oCubrid = GenericPool.Pool({
            name: 'CUBRID',
            // you can limit this pool to create maximum 10 connections
            max: 10,
            min: 1,
            // destroy the connection if it's idle for 30 seconds
            idleTimeoutMillis: 30000,
            log: self._htCubrid.bDebug,
            create: function (callback) {
                var oCubrid = Cubrid.createCUBRIDConnection(
                    self._htCubrid.sHostname,
                    self._htCubrid.nPort,
                    self._htCubrid.sUser,
                    self._htCubrid.sPassword,
                    self._htCubrid.sDatabase);
                oCubrid.connect(function (err) {
                    console.log('connected in _connectToCubrid, Migrator.js');
                    self._bIsCubridConnected = true;
                    self._checkConnection();
                    callback(err, oCubrid);
                });
            },
            destroy: function (oCubrid) {
                oCubrid.close();
            }
        });


    },
    _connectToMysql: function () {
        var self = this;

        this._oMysql = Mysql.createConnection({
            host: this._htMysql.sHostname,
            port: this._htMysql.nPort,
            database: this._htMysql.sDatabase,
            user: this._htMysql.sUser,
            password: this._htMysql.sPassword,
            debug: this._htMysql.bDebug
        });

        this._oMysql.connect(function (oErr) {
            //callback(oErr, oMysqlClient);
            console.log('connected in _connectToMysql, Migrator.js');
            self._bIsMysqlConnected = true;
            self._checkConnection();
        });

        this._oMysql.on('close', function (oErr) {
            console.error('close event in _connectToMysql, Migrator.js');
            //oMysqlClient.end();
            self._bIsMysqlConnected = false;
            self._oMysql.end();
            self._connectToMysql();
        });

        this._oMysql.on('error', function (oErr) {
            console.error('error event in _connectToMysql, Migrator.js');
            self.emit('error', err);
            self._bIsMysqlConnected = false;
            self._oMysql.end();
            self._connectToMysql();
        });
    },

    _checkConnection: function () {
        if (this._bIsCubridConnected && this._bIsMysqlConnected) {
            this.emit('connected');
        }
    },

    createTables: function (dml) {
        var self = this;
        var deferred = Q.defer();
        if (this._bIsMysqlConnected === false) {
            console.error('connection failed');
            return;
        }
        this._oMysql.query(dml, [], function (err, result) {
            if (err) {
                throw new Error('Error while creating tables', err);
            }
            deferred.resolve(true);
        });
        return deferred.promise;
    },

    migrateByQuery: function (sQuery, sToTablename, bTruncate, fCb) {
        var self = this;

        if (this._bIsCubridConnected === false || this._bIsMysqlConnected === false) {
            console.error('connection failed');
            return;
        }

        console.log('Migrate `%s` is just started\n', sQuery);

        this._nMigrationCount += 1;

        this._oCubrid.acquire(function (err, oCubrid) {
            if (err) {
                console.error('cubrid connection falsed', err);
                return;
            } else {
                oCubrid.query(sQuery, null);

                var aColumn;

                oCubrid.on(oCubrid.EVENT_QUERY_DATA_AVAILABLE, function (result, queryHandle) {
                    var aData = Result2Array.RowsArray(result);
                    aColumn = Result2Array.ColumnNamesArray(result);

                    self._aFailureCount[sQuery] = 0;
                    self._aSuccessCount[sQuery] = 0;
                    self._aTotalCount[sQuery] = Result2Array.TotalRowsCount(result);
                    console.log('Total query result rows count for ' + sToTablename + ' :', self._aTotalCount[sQuery]);
                    // console.log('First "batch" of data returned rows count:', aData.length);
                    if (bTruncate) {
                        self._truncateOnMysql(sToTablename, function () {
                            self._insertDataIntoMysql(sQuery, sToTablename, aColumn, aData, fCb);
                        });
                    } else {
                        self._insertDataIntoMysql(sQuery, sToTablename, aColumn, aData, fCb);
                    }
                    oCubrid.fetch(queryHandle, null);
                });

                oCubrid.on(oCubrid.EVENT_FETCH_DATA_AVAILABLE, function (result, queryHandle) {
                    var aData = Result2Array.RowsArray(result);
                    // console.log('Next fetch of data returned rows count:', aData.length);
                    self._insertDataIntoMysql(sQuery, sToTablename, aColumn, aData, fCb);
                    oCubrid.fetch(queryHandle, null);
                });

                oCubrid.on(oCubrid.EVENT_FETCH_NO_MORE_DATA_AVAILABLE, function (queryHandle) {
                    oCubrid.closeQuery(queryHandle, null);
                });
            }
        });
    },

    _truncateOnMysql: function (sToTablename, fCb) {
        this._oMysql.query('TRUNCATE ' + sToTablename, [], function (err, result) {
            if (err) {
                console.error('Error while truncate', err);
                return;
            }
            fCb();
        });
    },

    _insertDataIntoMysql: function (sQuery, sToTablename, aColumn, aData, fCb) {
        var self = this;
        if (!aData) {
            fCb(self._htMigrationResult[sQuery] = {
                nSuccessCount: 0,
                nFailureCount: 0
            });
            self._nMigrationDoneCount += 1;

            if (self._nMigrationCount === self._nMigrationDoneCount) {
                self.emit('done', self._htMigrationResult);
            } else {
                return;
            }
        }
        for (var i = 0, nLen = aData.length; i < nLen; i++) {
            var htData = this._makeHtData(aColumn, aData[i]);
            this._oMysql.query('INSERT INTO ' + sToTablename + ' SET ?', htData, function (err, aInnerResult) {
                if (err) {
                    console.log('Error while insert', err);
                    self._aFailureCount[sQuery] += 1;
                } else {
                    self._aSuccessCount[sQuery] += 1;
                }
                if ((self._aSuccessCount[sQuery] + self._aFailureCount[sQuery]) === self._aTotalCount[sQuery]) {
                    fCb(self._htMigrationResult[sQuery] = {
                        nSuccessCount: self._aSuccessCount[sQuery],
                        nFailureCount: self._aFailureCount[sQuery]
                    });
                    self._nMigrationDoneCount += 1;
                    if (self._nMigrationCount === self._nMigrationDoneCount) {
                        self.emit('done', self._htMigrationResult);
                    }
                }
            });
        }
    },

    _makeHtData: function (aColumn, aData) {
        var htData = {};
        for (var i = 0, nCnt = aColumn.length; i < nCnt; i++) {
            htData[aColumn[i]] = aData[i];
        }
        return htData;
    }
};
