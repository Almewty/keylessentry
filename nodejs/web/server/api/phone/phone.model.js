'use strict';

var mongoose = require('mongoose'),
    Schema = mongoose.Schema;

var PhoneSchema = new Schema({
  name: String,
  phoneId: String,
  secret: Buffer
});

module.exports = mongoose.model('Phone', PhoneSchema);