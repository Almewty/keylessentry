'use strict';

angular.module('keylessEntryApp')
    .controller('MainCtrl', function ($scope, $http, socket, Modal) {
        $scope.phones = [];

        $http.get('/api/phones').success(function (phones) {
            $scope.phones = phones;
            socket.syncUpdates('phone', $scope.phones);
        });

        $scope.addPhone = function () {
            if (!$scope.newPhoneForm.$valid) {
                return;
            }
            $http.post('/api/phones', {
                name: $scope.phoneName
            }).success(function (phone) {
            $http.get('/api/phones/' + phone._id + '/link').success(function (data) {
                Modal.show.qr()('/api/phones/' + phone._id + '/qr', data);
            });
            });
            $scope.phoneName = '';
        };

        $scope.deletePhone = function (phone) {
            Modal.confirm.delete(function () {
                $http.delete('/api/phones/' + phone._id);
            })(phone.name);
        };

        $scope.updatePhone = function (phone) {
            if (phone.newName === undefined || phone.newName.length < 1 || phone.newName.length > 30) {
                return;
            }
            phone.name = phone.newName;
            $http.put('/api/phones/' + phone._id, phone);
        };

        $scope.showEdit = function (phone) {
            phone.newName = phone.name;
            phone.edit = true;
        };

        $scope.showQr = function (phone) {
            $http.get('/api/phones/' + phone._id + '/link').success(function (data) {
                Modal.show.qr()('/api/phones/' + phone._id + '/qr', data);
            });
        };

        $scope.$on('$destroy', function () {
            socket.unsyncUpdates('phone');
        });
    });