/*
*************************************************************************************
* Copyright 2017 Normation SAS
*************************************************************************************
*
* This file is part of Rudder.
* 
* Rudder is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* In accordance with the terms of section 7 (7. Additional Terms.) of
* the GNU General Public License version 3, the copyright holders add
* the following Additional permissions:
* Notwithstanding to the terms of section 5 (5. Conveying Modified Source
* Versions) and 6 (6. Conveying Non-Source Forms.) of the GNU General
* Public License version 3, when you create a Related Module, this
* Related Module is not considered as a part of the work and may be
* distributed under the license agreement of your choice.
* A "Related Module" means a set of sources files including their
* documentation that, without modification of the Source Code, enables
* supplementary functions or services in addition to those offered by
* the Software.
* 
* Rudder is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Rudder.  If not, see <http://www.gnu.org/licenses/>.

*
*************************************************************************************
*/


var app = angular.module('tags', []);

app.controller('tagsController', function ($scope, $http, $location, $timeout, $rootScope) {
  $scope.filterScope;
  $scope.newTag = { "key" : "" , "value" : "" };
  $scope.tags = [];
  $scope.isEditForm = false
  $scope.showDelete = [];
  $scope.filterKeys = [];
  $scope.filterValues = [];

  $scope.init = function(tags, filterCtrl, isEditForm){
    $scope.tags = tags;
    $scope.isEditForm = isEditForm

    angular.element($('[ng-controller="'+filterCtrl+'"]')).scope().registerScope($rootScope)
  }
  
  $scope.toggleTag = function(tag){
    if( $scope.filterScope !== undefined) {
      $scope.filterScope.$emit("addTag", angular.copy(tag))
    }    
  }
  
  $scope.keyTagMatch = function(tag){
    return $.inArray(tag.key , $scope.filterKeys) > -1
  }
  $scope.valTagMatch = function(tag){
    return $.inArray(tag.value, $scope.filterValues) > -1 // tag.match.value && $scope.scopeFilter.tags.length>0;
  }

  $scope.$watch('tags', updateResult, true)
  
  $rootScope.$on("registerScope", function(event,filterScope) {
    $scope.filterScope = filterScope;
  })
    
  $rootScope.$on("updateFilter", function(event,filters) {
    $scope.filterKeys = [];
    $scope.filterValues = [];
    angular.forEach(filters.tags, function(filter) {
      $scope.filterKeys.push(filter.key);
      $scope.filterValues.push(filter.value);
    })
    $timeout(function() {},0);
  })
  
  function updateResult () {
    $scope.result = JSON.stringify($scope.tags);
  }
  
  $scope.addTag = function(){
    $scope.tags.push($scope.newTag);
    $scope.newTag = {};
  }
  
  $scope.removeTag = function(index){
    $scope.tags.splice(index, 1);
  }
  
});

app.config(function($locationProvider) {
  $locationProvider.html5Mode({
    enabled: true,
    requireBase: false
  });
});