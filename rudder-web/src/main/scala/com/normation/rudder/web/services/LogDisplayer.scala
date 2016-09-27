/*
*************************************************************************************
* Copyright 2011 Normation SAS
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

package com.normation.rudder.web.services


import com.normation.rudder.services.reports.ReportingService
import com.normation.rudder.domain.nodes.NodeInfo
import com.normation.rudder.domain.policies.RuleId
import com.normation.rudder.domain.policies.RuleVal
import com.normation.rudder.services.servers.NodeSummaryService
import com.normation.rudder.web.model._
import com.normation.inventory.domain.NodeId
import com.normation.rudder.domain.reports.Reports
import com.normation.rudder.web.components.DateFormaterService
import scala.xml._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.util._
import Helpers._
import net.liftweb.http.js._
import JsCmds._
import JE._
import net.liftweb.http.SHtml._
import bootstrap.liftweb.LiftSpringApplicationContext.inject
import scala.collection._
import com.normation.rudder.domain.policies.DirectiveId
import com.normation.exceptions.TechnicalException
import net.liftweb.http.Templates
import com.normation.rudder.repository.ReportsRepository
import com.normation.rudder.repository.RoDirectiveRepository
import com.normation.rudder.repository.RoRuleRepository
import org.joda.time.DateTime
import com.normation.cfclerk.xmlparsers.CfclerkXmlConstants.DEFAULT_COMPONENT_KEY
import net.liftweb.json.JsonAST.JString
import org.joda.time.format.DateTimeFormat

/**
 * Show the reports from cfengine (raw data)
 */
class LogDisplayer(
    reportRepository   : ReportsRepository
  , directiveRepository: RoDirectiveRepository
  , ruleRepository     : RoRuleRepository
) extends Loggable {

  private val templatePath = List("templates-hidden", "node_logs_tabs")
  private def template() =  Templates(templatePath) match {
    case Empty | Failure(_,_,_) =>
      throw new TechnicalException("Template for server details not found. I was looking for %s.html".format(templatePath.mkString("/")))
    case Full(n) => n
  }

  private def content = chooseTemplate("logs","content",template)

  private val gridName = "logsGrid"


  def asyncDisplay(nodeId : NodeId) : NodeSeq = {
    val id = JsNodeId(nodeId)
    val ajaxRefresh =  SHtml.ajaxInvoke( () => refreshData(nodeId, reportRepository.findReportsByNode(nodeId)))

    def getEventsInterval(jsonInterval: String): JsCmd = {
      import net.liftweb.util.Helpers.tryo
      import net.liftweb.json.parse

      val format = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")

      println("**** got string: " + jsonInterval)

      (for {
        parsed   <- tryo(parse(jsonInterval)) ?~! s"Error when trying to parse '${jsonInterval}' as a JSON datastructure with fields 'start' and 'end'"
        startStr <- parsed \ "start" match {
                      case JString(startStr) => tryo(DateTime.parse(startStr, format)) ?~! s"Bad format for start date, was execpting '${format.toString}' and got '${startStr}'"
                      case x => Failure("Error: missing start date and time")
                    }
        endStr   <- parsed \ "end" match {
                      case JString(endStr) => tryo(DateTime.parse(endStr, format)) ?~! s"Bad format for end date, was execpting '${format.toString}' and got '${endStr}'"
                      case x => Failure("Error: missing end date and time")
                    }
      } yield {
        if(startStr.isBefore(endStr)) {
          reportRepository.findReportsByNodeOnInterval(nodeId, startStr, endStr)
        } else {
          reportRepository.findReportsByNodeOnInterval(nodeId, endStr, startStr)
        }
      }) match {
        case Full(reports) =>
          refreshData(nodeId, reports)
        case eb : EmptyBox =>
          val fail = eb ?~! "Could not get latest event logs"
          logger.error(fail.messageChain)
          val xml = <div class="error">Error when trying to get last event logs. Error message was: {fail.messageChain}</div>
          SetHtml("eventLogsError",xml)
      }
    }

    Script(
      OnLoad(
        // set static content
        SetHtml("logsDetails",content) &
        // Create empty table
        JsRaw(s"""
         var pickEventLogsInInterval = ${AnonFunc(SHtml.ajaxCall(JsRaw(
           """'{"start":"'+$(".pickStartInput").val()+'", "end":"'+$(".pickEndInput").val()+'"}'"""
         ), getEventsInterval)._2).toJsCmd}

          createTechnicalLogsTable("${gridName}",[], "${S.contextPath}",function() {${ajaxRefresh.toJsCmd}}, pickEventLogsInInterval);""") &
        // Load data asynchronously
        JsRaw(
      s"""
        $$("#details_${id}").bind( "show", function(event, ui) {
          if(ui.panel.id== 'node_logs') { ${ajaxRefresh.toJsCmd} }
        });
       """
    )))
  }

  /**
   * find all reports for node passed as parameter and transform them into table data
   */
  def getReportsLineForNode (nodeId : NodeId, reports: Seq[Reports]) = {
    val directiveMap = mutable.Map[DirectiveId, String]()
    val ruleMap = mutable.Map[RuleId, String]()

    def getDirectiveName(directiveId : DirectiveId) : String = {
      directiveMap.get(directiveId).getOrElse({val result = directiveRepository.getDirective(directiveId).map(_.name).openOr(directiveId.value); directiveMap += ( directiveId -> result); result } )
    }

    def getRuleName(ruleId : RuleId) : String = {
      ruleMap.get(ruleId).getOrElse({val result = ruleRepository.get(ruleId).map(x => x.name).openOr(ruleId.value); ruleMap += ( ruleId -> result); result } )
    }

    val lines = {
      for {
        report <- reports
      } yield {

        val ruleName = getRuleName(report.ruleId)

        val directiveName = getDirectiveName(report.directiveId)

        val value = if (DEFAULT_COMPONENT_KEY == report.keyValue) "-" else report.keyValue

        ReportLine (
            report.executionDate
          , report.severity
          , ruleName
          , directiveName
          , report.component
          , value
          , report.message
        )
      }
    }

    JsTableData(lines.toList)

  }

  def refreshData(nodeId : NodeId, reports: => Seq[Reports]) : JsCmd = {

    val data = getReportsLineForNode(nodeId, reports).json.toJsCmd

    OnLoad(JsRaw(s"""refreshTable("${gridName}",${data});""")
    )
  }
}

/*
 *   Javascript object containing all data to create a line in the DataTable
 *   { "executionDate" : Date report was executed [DateTime]
 *   , "severity" : Report severity [String]
 *   , "ruleName" : Rule name [String]
 *   , "directiveName": Directive name [String]
 *   , "component" : Report component [String]
 *   , "value" : Report value [String]
 *   , "message" : Report message [String]
 *   }
 */
case class ReportLine (
    executionDate  : DateTime
  , severity       : String
  , ruleName       : String
  , directiveName  : String
  , component      : String
  , value          : String
  , message        : String
) extends JsTableLine {

    override val json  = {

      JsObj(
          ( "executionDate", executionDate.toString("yyyy-MM-dd HH:mm:ss") )
        , ( "severity", severity )
        , ( "ruleName", ruleName )
        , ( "directiveName",  directiveName )
        , ( "component", component )
        , ( "value", value )
        , ( "message", message )
      )

    }
}
