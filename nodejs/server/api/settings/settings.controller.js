/**
 * Using Rails-like standard naming convention for endpoints.
 * GET     /things              ->  index
 * POST    /things              ->  create
 * GET     /things/:id          ->  show
 * PUT     /things/:id          ->  update
 * DELETE  /things/:id          ->  destroy
 */

'use strict';

var _ = require('lodash');
var config = require('./../../config/environment');
var whitelist = ['doorName']; // only these properties are accessible

// Get list of things
exports.index = function (req, res) {
    // return only whitelisted properties
    var keys = _.intersection(whitelist, Object.keys(config));
    
    return res.json(_.zipObject(keys, _.at(config, keys))); 
};

// Updates an existing thing in the DB.
exports.update = function (req, res) {
    if (whitelist.indexOf(req.params.name)) {
        return res.send(404);
    }
    
    config[req.params.name] = req.body.value;
    config.save();
    
    var ret = {};
    ret[req.params.name] = req.body.value
    res.json(ret);
};

function handleError(res, err) {
    return res.send(500, err);
}