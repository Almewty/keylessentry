'use strict';

angular.module('keylessEntryApp')
    .controller('NavbarCtrl', function ($scope, $location, $http) {
        $scope.menu = [{
            'title': 'Home',
            'link': '/'
        }];

        $scope.isCollapsed = true;

        $scope.isActive = function (route) {
            return route === $location.path();
        };
    
        $http.get('api/settings').success(function (data) {
            $scope.name = data.doorName;
        });
    
        $scope.showEdit = function () {
            $scope.toggleEdit = !$scope.toggleEdit;
            $scope.newName = $scope.name;   
        };
        
        $scope.updateName = function () {
            $http.put('api/settings/doorName', {value: $scope.newName}).success(function (data) {
                $scope.name = data.doorName;
                $scope.toggleEdit = false;
            }).error(function () {
                var l = 20;
                for (var i = 0; i < 10; i++) {
                    $('#change-name-form').animate( { 'margin-left': '+=' + ( l = -l ) + 'px' }, 50);
				}
			});
        };
    
        $scope.newName = $scope.name;
    });