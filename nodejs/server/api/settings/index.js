'use strict';

var express = require('express');
var controller = require('./settings.controller');

var router = express.Router();

router.get('/', controller.index);
router.put('/:name', controller.update);
router.patch('/:name', controller.update);

module.exports = router;