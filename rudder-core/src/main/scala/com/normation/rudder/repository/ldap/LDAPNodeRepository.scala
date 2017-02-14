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
package com.normation.rudder.repository
package ldap

import com.normation.eventlog.EventActor
import com.normation.eventlog.EventLog
import com.normation.eventlog.ModificationId
import com.normation.inventory.domain.NodeId
import com.normation.ldap.sdk._
import com.normation.rudder.domain.NodeDit
import com.normation.rudder.domain.RudderLDAPConstants._
import com.normation.rudder.domain.nodes._
import com.normation.rudder.domain.policies.SimpleDiff
import com.normation.rudder.reports.AgentRunInterval
import com.normation.rudder.reports.HeartbeatConfiguration
import net.liftweb.common._
import com.normation.rudder.repository.EventLogRepository
import com.normation.rudder.domain.policies.PolicyMode
import com.normation.ldap.ldif.LDIFNoopChangeRecord

class WoLDAPNodeRepository(
    nodeDit             : NodeDit
  , mapper              : LDAPEntityMapper
  , ldap                : LDAPConnectionProvider[RwLDAPConnection]
  , actionLogger        : EventLogRepository
  ) extends WoNodeRepository with Loggable {
  repo =>

  def updateNode(node: Node, modId: ModificationId, actor:EventActor, reason:Option[String]) : Box[Node] = {
    import com.normation.rudder.services.nodes.NodeInfoService.{nodeInfoAttributes => attrs}
    repo.synchronized { for {
      con           <- ldap
      existingEntry <- con.get(nodeDit.NODES.NODE.dn(node.id.value), attrs:_*) ?~! s"Cannot update node with id ${node.id.value} : there is no node with that id"
      oldNode       <- mapper.entryToNode(existingEntry) ?~! s"Error when transforming LDAP entry into a node for id ${node.id.value} . Entry: ${existingEntry}"
      // here goes the check that we are not updating policy server
      nodeEntry     =  mapper.nodeToEntry(node)
      result        <- con.save(nodeEntry, true, Seq()) ?~! s"Error when saving node entry in repository: ${nodeEntry}"
      // only record an event log if there is an actual change
      _             <- result match {
                         case LDIFNoopChangeRecord(_) => Full("ok")
                         case _                       =>
                           val diff = ModifyNodeDiff(oldNode, node)
                           actionLogger.saveModifyNode(modId, actor, diff, reason)
                       }
    } yield {
      node
    } }
  }
}
