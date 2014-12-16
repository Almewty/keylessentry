'use strict';

angular.module('keylessEntryApp')
  .controller('MainCtrl', function ($scope, $http, socket) {
    $scope.phones = [];

    $http.get('/api/phones').success(function(phones) {
      $scope.phones = phones;
      socket.syncUpdates('phone', $scope.phones);
    });

    $scope.addPhone = function() {
      if($scope.phoneName === '') {
        return;
      }
      $http.post('/api/phones', { name: $scope.phoneName });
      $scope.phoneName = '';
    };

    $scope.deletePhone = function(phone) {
      $http.delete('/api/phones/' + phone._id);
    };
    
    $scope.showEdit = function (phone) {
        phone.edit = true;  
    };

    $scope.$on('$destroy', function () {
      socket.unsyncUpdates('phone');
    });
  });
