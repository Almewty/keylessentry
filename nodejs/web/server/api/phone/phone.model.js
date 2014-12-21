'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;
var config = require('./../../config/environment');
var url = require('url');
var qr = require('qr-image');

var PhoneSchema = new Schema({
    _id: String,
    name: String,
    secret: Buffer
});

PhoneSchema.virtual('phoneId').get(function () {
    return this._id;
});

PhoneSchema.methods.createUrl = function () {
    return url.format({
        protocol: 'http',
        host: 'w-hs.de',
        pathname: 'keylessentry',
        query: {
            name: config.doorName,
            ownid: new Buffer(this.phoneId).toString('base64'),
            remoteid: new Buffer(config.doorId).toString('base64'),
            secret: new Buffer(this.secret).toString('base64')
        }
    })
};

PhoneSchema.methods.pipeQr = function (res) {
    var url = this.createUrl();
    var code = qr.image(url, {
        type: 'svg',
        size: 5
    });
    code.pipe(res);
};

module.exports = mongoose.model('Phone', PhoneSchema);