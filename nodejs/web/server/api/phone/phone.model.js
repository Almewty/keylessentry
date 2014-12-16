'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var PhoneSchema = new Schema({
    _id: String,
    name: String,
    secret: Buffer
});

PhoneSchema.virtual('phoneId').get(function () {
    return this._id; 
});

module.exports = mongoose.model('Phone', PhoneSchema);