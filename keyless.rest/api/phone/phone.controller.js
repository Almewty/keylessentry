/**
 * Using Rails-like standard naming convention for endpoints.
 * GET     /things              ->  index
 * POST    /things              ->  create
 * GET     /things/:id          ->  show
 * PUT     /things/:id          ->  update
 * DELETE  /things/:id          ->  destroy
 */

'use strict';

var dba = require('/nodejs/dbaccess');
var _ = require('lodash');



// Read all
app.get('/phones', function (req, res) {
  res.send(dba.getNameList());
});


// Create
app.post('/phones/:uuid/:name/:secret', function (req, res) {
  res.send(dba.insertUUID(req.params.uuid,req.params.name,req.params.sercret));
});


// Delete
app.del('/todo/:uuid', function (req, res) {
  dba.removeUUID(req.params.uuid);
  res.send(200);
});