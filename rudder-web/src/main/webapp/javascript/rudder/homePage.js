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

function homePage (
    globalCompliance
  , globalGauge
  , nodeCompliance
  , nodeComplianceColors
  , nodeCount
) {
  $("#globalCompliance").append(buildComplianceBar(globalCompliance,50));
  createTooltip();


  var allNodes = nodeCount.active;
  var activeNodes ="<span class='highlight'>" + nodeCount.active + "</span> Nodes."
  if (nodeCount.active === 1) {
    activeNodes = "<span class='highlight'>" + nodeCount.active + "</span> Node."
  }
  var stats = "Compliance based on "+ activeNodes
  if (nodeCount.pending !== null) {
    allNodes += nodeCount.pending.nodes;
    var pendingNodes = nodeCount.pending.nodes + " Nodes"
    var verb = "are"
    if (nodeCount.active === 1) {
      pendingNodes = nodeCount.pending.nodes + " Node"
      verb = "is"
    }
    stats += " There "+ verb +" also " + pendingNodes + " for which we are still waiting for data (" + nodeCount.pending.percent + "%)."
  }
  $("#globalComplianceStats").html(stats);
  
  var opts = {
      lines: 12, // The number of lines to draw
      angle: 0, // The length of each line
      lineWidth: 0.44, // The line thickness
      pointer: {
        length: 0.9, // The radius of the inner circle
        strokeWidth: 0.035, // The rotation offset
        color: '#000000' // Fill color
      },
      limitMax: 'false',   // If true, the pointer will not go past the end of the gauge
      colorStart: '#6FADCF',   // Colors
      colorStop: '#8FC0DA',    // just experiment with them
      strokeColor: '#E0E0E0',   // to see which ones work best for you
      percentColors : [[0.0, "#c9302c" ], [0.30, "#f0ad4e"], [0.50, "#5bc0de"], [1.0, "#9bc832"]],
      generateGradient: true
    };
    var target = document.getElementById('complianceGauge'); // your canvas element
    var gauge = new Gauge(target).setOptions(opts); // create sexy gauge!
    gauge.maxValue = 100; // set max gauge value
    gauge.animationSpeed = 25; // set animation speed (32 is default value)
    // set actual value - there is a bug for value = 0, so let's pretend it's 0.1
    gauge.set(function() {
      if(globalGauge == 0) return 0.1;
      else return globalGauge;
    }()); 
    $("#gauge-value").text(globalGauge+"%");


  doughnutChart('nodeCompliance', nodeCompliance, allNodes, nodeCompliance.colors)
    
}

var inventoryColors = 
  [ 'rgb(54, 148, 209)'
  , 'rgb(23, 190, 207)'
  , 'rgb(255,113,37)'
  , 'rgb(255, 224, 14)'
  , 'rgb(227, 119, 194)'
  , 'rgb(44, 160, 44)'
  , 'rgb(255, 104, 105)'
  , 'rgb(148, 103, 189)'
  , 'rgb(140, 86, 75)'
  , 'rgb(160, 160, 160)'
  , 'rgb(155,200,50)'
  , '#ffd203'
  , 'rgb(132, 63, 152)'
  ];

function doughnutChart (id,data,count, colors) {
  
  var context = $("#"+id)

  var chartData = {
    labels  :  data.labels,
    datasets:
      [ { data           : data.values
        , backgroundColor: colors
      } ]
  };

  function highlight(chart, index) {
    chart.segments
  }
  var chartOptions = {
      type: 'doughnut'
    , data: chartData
    , options: {
        legend: {
          display:false

        }
      , legendCallback: function(chart) {
        var text = [];
        text.push('<ul>');
        for (var i=0; i<chart.data.datasets[0].data.length; i++) {
            text.push('<li class="legend">');
            text.push('<span class="legend-square" style="background-color:' + chart.data.datasets[0].backgroundColor[i] + '"></span>');
            if (chart.data.labels[i]) {
                text.push(chart.data.labels[i]);
            }
            text.push('</li>');
        }
        text.push('</ul>');
        return text.join("");
      }
      , tooltips: {
          callbacks: {
            label: function(tooltipItem, data) {
              var label = data.labels[tooltipItem.index];
              var content = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
              var unit = "node";
              if (content > 1) {
                unit += "s";
              } 
              return " " + label + ": " + content + " " + unit +" ("+ (content/count*100).toFixed(0) + "%)"
            }
          }
        }
      }
  }
  
  var chart = new Chart(context, chartOptions);
  context.after(chart.generateLegend())
}

function homePageInventory (
    nodeMachines
  , nodeOses
  , count
) {
  doughnutChart('nodeMachine',nodeMachines, count, inventoryColors)
  doughnutChart('nodeOs', nodeOses, count, inventoryColors)
}

function homePageSoftware (
    nodeAgents
  , count
) {
  doughnutChart('nodeAgents', nodeAgents, count, inventoryColors)
}

function userAgentIsIE() {
    var msie = window.navigator.userAgent.indexOf("MSIE ");
    if (msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./)){
        return true;
    }
    return false;
}