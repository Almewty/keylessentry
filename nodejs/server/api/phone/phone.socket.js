/**
 * Broadcast updates to client when the model changes
 */
'use strict';
var phone = require('./phone.model');
exports.register = function (socket) {
    phone.schema.post('save', function (doc) {
        onSave(socket, doc);
    });
    phone.schema.post('remove', function (doc) {
        onRemove(socket, doc);
    });
}

function onSave(socket, doc, cb) {
    socket.emit('phone:save', doc);
}

function onRemove(socket, doc, cb) {
    socket.emit('phone:remove', doc);
}