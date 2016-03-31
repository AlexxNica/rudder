/*
*************************************************************************************
* Copyright 2014 Normation SAS
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

package com.normation.rudder.reports

import net.liftweb.common._
import net.liftweb.json._

/**
 * Define level of compliance:
 *
 * - compliance: full compliance, check success report (historical Rudder way)
 * - error_only: only report for repaired and error reports.
 */

sealed trait ComplianceMode {
  def name: String
}

final case object FullCompliance extends ComplianceMode {
  val name = "full-compliance"
}
final case class ChangesOnly (
  heartbeatPeriod : Int
)extends ComplianceMode {
  val name = ChangesOnly.name
}

object ChangesOnly {
  val name = "changes-only"
}

trait ComplianceModeService {
  def getComplianceMode : Box[ComplianceMode]
}

class ComplianceModeServiceImpl (
    readComplianceMode : () => Box[String]
  , readHeartbeatFreq  : () => Box[Int]
) extends ComplianceModeService {

  def getComplianceMode : Box[ComplianceMode] = {
     readComplianceMode() match {
       case Full(FullCompliance.name) =>  Full(FullCompliance)
       case Full(ChangesOnly.name) =>
         readHeartbeatFreq() match {
           case Full(freq) => Full(ChangesOnly(freq))
           case eb : EmptyBox =>
             val fail = eb ?~! "Could not get heartbeat period"
             fail
         }
       case Full(value) =>
         Failure(s"Unable to parse the compliance mode name. was expecting '${FullCompliance.name}' or '${ChangesOnly.name}' and got '${value}'")
       case eb : EmptyBox =>
         val fail = eb ?~! "Could not get compliance mode name"
         fail
     }
  }
}