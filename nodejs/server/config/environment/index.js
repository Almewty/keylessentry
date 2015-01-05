'use strict';

var path = require('path');
var _ = require('lodash');
var nconf = require('nconf');
var uuid = require('node-uuid');

nconf.argv().env();

function requiredProcessEnv(name) {
    if (!process.env[name]) {
        throw new Error('You must set the ' + name + ' environment variable');
    }
    return process.env[name];
}

// All configurations will extend these options
// ============================================
var all = {
    env: process.env.NODE_ENV,

    //nconf json file
    nconfPath: './config.json',

    //nconf dependent variables
    nconfVars: ['doorId', 'doorName'],

    // Root path of server
    root: path.normalize(__dirname + '/../../..'),

    // Server port
    port: process.env.PORT || 9000,

    // Should we populate the DB with sample data?
    seedDB: false,

    // Secret for session, you will want to change this and make it an environment variable
    secrets: {
        session: 'keyless-entry-secret'
    },

    // MongoDB connection options
    mongo: {
        options: {
            db: {
                safe: true
            }
        }
    }

};

// Create the config object based on the NODE_ENV
// ==============================================
all = _.merge(
    all,
    require('./' + process.env.NODE_ENV + '.js') || {});

// add a save function wrapper for nconf
all.save = function () {
    for (var i = 0; i < all.nconfVars.length; i++) {
        nconf.set(all.nconfVars[i], all[all.nconfVars[i]]);
    }
    nconf.save();
};

nconf.file({
    file: all.nconfPath
});

nconf.defaults({
    doorName: 'KeylessEntry'
});

// add the nconfVars to the exported object
for (var i = 0; i < all.nconfVars.length; i++) {
    all[all.nconfVars[i]] = nconf.get(all.nconfVars[i]);
}

// if no doorId is set create one
if (!all.doorId) {
    all.doorId = uuid.v1();
    all.save();
}

module.exports = all;