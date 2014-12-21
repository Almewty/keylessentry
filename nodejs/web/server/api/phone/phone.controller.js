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
var Phone = require('./phone.model');
var crypto = require('crypto');
var uuid = require('node-uuid');
var config = require('./../../config/environment');

// Get list of things
exports.index = function (req, res) {
    Phone.find(function (err, things) {
        if (err) {
            return handleError(res, err);
        }
        return res.json(200, things);
    });
};

// Get a single thing
exports.show = function (req, res) {
    Phone.findById(req.params.id, function (err, phone) {
        if (err) {
            return handleError(res, err);
        }
        if (!phone) {
            return res.send(404);
        }
        return res.json(phone);
    });
};

// Creates a new thing in the DB.
exports.create = function (req, res) {
    var uid = uuid.v1();
    crypto.randomBytes(256, function (ex, bytes) {
        Phone.create({
            _id: uid,
            name: req.body.name,
            secret: bytes
        }, function (err, phone) {
            if (err) {
                return handleError(res, err);
            }
            return res.json(201, phone);
        });
    });
};

// Updates an existing thing in the DB.
exports.update = function (req, res) {
    if (req.body._id) {
        delete req.body._id;
    }
    Phone.findById(req.params.id, function (err, phone) {
        if (err) {
            return handleError(res, err);
        }
        if (!phone) {
            return res.send(404);
        }
        var updated = _.merge(phone, {
            name: req.body.name
        });
        updated.save(function (err) {
            if (err) {
                return handleError(res, err);
            }
            return res.json(200, phone);
        });
    });
};

// Deletes a thing from the DB.
exports.destroy = function (req, res) {
    Phone.findById(req.params.id, function (err, phone) {
        if (err) {
            return handleError(res, err);
        }
        if (!phone) {
            return res.send(404);
        }
        phone.remove(function (err) {
            if (err) {
                return handleError(res, err);
            }
            return res.send(204);
        });
    });
};

exports.createLink = function (req, res) {
    Phone.findById(req.params.id, function (err, phone) {
        if (err) {
            return handleError(res, err);
        }
        if (!phone) {
            return res.send(404);
        }
        res.json(phone.createUrl());
    });
};

exports.createQr = function (req, res) {
    Phone.findById(req.params.id, function (err, phone) {
        if (err) {
            return handleError(res, err);
        }
        if (!phone) {
            return res.send(404);
        }
        res.type('svg');
        phone.pipeQr(res);
    });
};

function handleError(res, err) {
    return res.send(500, err);
}