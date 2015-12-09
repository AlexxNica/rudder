/*
*************************************************************************************
* Copyright 2013 Normation SAS
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

package com.normation.rudder.web.rest.node

import com.normation.inventory.ldap.core.LDAPFullInventoryRepository
import com.normation.utils.StringUuidGenerator
import com.normation.rudder.web.rest.RestExtractorService
import com.normation.rudder.web.rest.RestDataSerializer
import com.normation.inventory.domain.NodeId
import com.normation.inventory.domain._
import net.liftweb.http.Req
import net.liftweb.common._
import com.normation.rudder.web.rest.RestUtils._
import net.liftweb.json.JArray
import net.liftweb.json.JsonDSL._




class NodeApiService4 (
    inventoryRepository : LDAPFullInventoryRepository
  , uuidGen             : StringUuidGenerator
  , restExtractor       : RestExtractorService
  , restSerializer      : RestDataSerializer
) extends Loggable {

  import restSerializer._

  def getNodeDetails(nodeId : NodeId, detailLevel : NodeDetailLevel, state: InventoryStatus) = {
    inventoryRepository.get(nodeId,state).map(serializeInventory(_, detailLevel))
  }

  def nodeDetailsWithStatus(nodeId : NodeId, detailLevel : NodeDetailLevel, state: InventoryStatus, req:Req) = {
    implicit val prettify = restExtractor.extractPrettify(req.params)
    implicit val action = s"${state.name}NodeDetails"
    getNodeDetails(nodeId,detailLevel,state) match {
        case Full(inventory) =>
          toJsonResponse(Some(nodeId.value), ( "nodes" -> JArray(List(inventory))))
        case eb: EmptyBox =>
          val message = (eb ?~ (s"Could not find Node ${nodeId.value} in state '${state.name}'")).msg
          toJsonError(Some(nodeId.value), message)
      }
  }


  def nodeDetailsGeneric(nodeId : NodeId, detailLevel : NodeDetailLevel, req:Req) = {
    implicit val prettify = restExtractor.extractPrettify(req.params)
    implicit val action = "nodeDetails"
    getNodeDetails(nodeId,detailLevel,AcceptedInventory) match {
        case Full(inventory) =>
          toJsonResponse(Some(nodeId.value), ( "nodes" -> JArray(List(inventory))))
        case eb: EmptyBox =>
          getNodeDetails(nodeId,detailLevel,PendingInventory) match {
            case Full(inventory) =>
              toJsonResponse(Some(nodeId.value), ( "nodes" -> JArray(List(inventory))))
            case eb: EmptyBox =>
              getNodeDetails(nodeId,detailLevel,RemovedInventory) match {
                case Full(inventory) =>
                  toJsonResponse(Some(nodeId.value), ( "nodes" -> JArray(List(inventory))))
                case eb: EmptyBox =>
                  val message = (eb ?~ (s"Could not find Node ${nodeId.value}")).msg
                  toJsonError(Some(nodeId.value), message)
              }
          }
    }
  }
}


