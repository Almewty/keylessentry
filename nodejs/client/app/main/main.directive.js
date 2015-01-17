'use strict';

angular.module('keylessEntryApp')
    .directive('focusMe', function ($timeout, $parse) {
        return {
            link: function (scope, element, attrs) {
                var model = $parse(attrs.focusMe);
                scope.$watch(model, function (value) {
                    if (value === true) {
                        $timeout(function () {
                            element[0].focus();
                        });
                    }
                });
                element.bind('blur', function () {
                    scope.$apply(model.assign(scope, false));
                });
            }
        };
    })
    .directive('focusMeNoBlur', function ($timeout, $parse, $log) {
        return {
            link: function (scope, element, attrs) {
                var model = $parse(attrs.focusMeNoBlur);
                scope.$watch(model, function (value) {
                    if (value === true) {
                        $timeout(function () {
                            element[0].focus();
                        });
                    }
                });
            }
        };
    })
    .directive('stopEvent', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attr) {
                element.bind(attr.stopEvent, function (e) {
                    e.stopPropagation();
                });
            }
        };
    });