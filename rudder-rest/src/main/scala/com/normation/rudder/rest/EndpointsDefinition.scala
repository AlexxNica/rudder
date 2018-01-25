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
* In accordance with OneParam with the terms of section 7 (7. Additional Terms.) of
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
* along with OneParam with Rudder.  If not, see <http://www.gnu.org/licenses/>.

*
*************************************************************************************
*/

package com.normation.rudder.rest

import com.normation.rudder.rest.EndpointSchema.syntax._
import com.normation.rudder.api.HttpAction._
import sourcecode.Line


/*
 * This file contains the definution of all endpoint schema
 * in Rudder base.
 *
 * Any module wanting to contribute an API
 * More preciselly, it defines the data format of
 * an endpoint descriptor, an endpoint descriptor container,
 * and prefil all the known endpoints into the corner.
 *
 * It also defined interpretor for endpoint descriptor toward
 * Lift RestAPI objects.
 *
 */

// we need a marker trait to get endpoint in a sorted way. Bad, but nothing better
// safe rewriting sealerate
trait SortIndex {
  protected[rest] def z: Int
  protected[rest] def zz = implicitly[Line].value
}

sealed trait ComplianceApi extends EndpointSchema with GeneralApi with SortIndex
object ComplianceApi {

  final case object GetRulesCompliance extends ComplianceApi with ZeroParam with StartsAtVersion7 with SortIndex { val z = zz
    val description = "Get compliance information for all rules"
    val (action, path)  = GET / "compliance" / "rules"
  }
  final case object GetRulesComplianceId extends ComplianceApi with OneParam with StartsAtVersion7 with SortIndex { val z = zz
    val description = "Get compliance information for the given rule"
    val (action, path)  = GET / "compliance" / "rules" / "{id}"
  }
  final case object GetNodesCompliance extends ComplianceApi with ZeroParam with StartsAtVersion7 with SortIndex { val z = zz
    val description = "Get compliance information for all nodes"
    val (action, path)  = GET / "compliance" / "nodes"
  }
  final case object GetNodeComplianceId extends ComplianceApi with OneParam with StartsAtVersion7 with SortIndex { val z = zz
    val description = "Get compliance information for the given node"
    val (action, path)  = GET / "compliance" / "nodes" / "{id}"
  }

  def endpoints = ca.mrvisser.sealerate.values[ComplianceApi].toList.sortBy( _.z )
}

sealed trait GroupApi extends EndpointSchema with GeneralApi with SortIndex
object GroupApi {
  // API v2
  final case object ListGroups extends GroupApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "List all groups with their information"
    val (action, path)  = GET / "groups"
  }
  final case object CreateGroup extends GroupApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Create a new group"
    val (action, path)  = PUT / "groups"
  }
  final case object GroupDetails extends GroupApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Get information about the given group"
    val (action, path)  = GET / "groups" / "{id}"
  }
  final case object DeleteGroup extends GroupApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Delete given group"
    val (action, path)  = DELETE / "groups" / "{id}"
  }
  final case object UpdateGroup extends GroupApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Update given group"
    val (action, path)  = POST / "groups" / "{id}"
  }
  final case object ReloadGroup extends GroupApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Update given dynamic group node list"
    val (action, path)  = GET / "groups" / "{id}" / "reload"
  }
  // API v5 updates 'Create' methods but no new endpoints
  // API v6
  final case object GetGroupTree extends GroupApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "List all group categories and group in a tree format"
    val (action, path)  = GET / "groups" / "tree"
  }
  final case object GetGroupCategoryDetails extends GroupApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Get information about the given group category"
    val (action, path)  = GET / "groups" / "categories" / "{id}"
  }
  final case object DeleteGroupCategory extends GroupApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Delete given group category"
    val (action, path)  = DELETE / "groups" / "categories" / "{id}"
  }
  final case object UpdateGroupCategory extends GroupApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Update information for given group category"
    val (action, path)  = POST / "groups" / "categories" / "{id}"
  }
  final case object CreateGroupCategory extends GroupApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Create a new group category"
    val (action, path)  = PUT / "groups" / "categories"
  }

  def endpoints = ca.mrvisser.sealerate.values[GroupApi].toList.sortBy( _.z )
}

sealed trait ChangeRequestApi extends EndpointSchema with GeneralApi with SortIndex
object ChangeRequestApi {

  final case object ListChangeRequests extends ChangeRequestApi with ZeroParam with StartsAtVersion3 with SortIndex { val z = zz
    val description = "List all change requests"
    val (action, path)  = GET / "changeRequests"
  }
  final case object ChangeRequestsDetails extends ChangeRequestApi with OneParam with StartsAtVersion3 with SortIndex { val z = zz
    val description = "Get information about given change request"
    val (action, path)  = GET / "changeRequests" / "{id}"
  }
  final case object DeclineRequestsDetails extends ChangeRequestApi with OneParam with StartsAtVersion3 with SortIndex { val z = zz
    val description = "Decline given change request"
    val (action, path)  = DELETE / "changeRequests" / "{id}"
  }
  final case object AcceptRequestsDetails extends ChangeRequestApi with OneParam with StartsAtVersion3 with SortIndex { val z = zz
    val description = "Accept given change request"
    val (action, path)  = POST / "changeRequests" / "{id}" / "accept"
  }
  final case object UpdateRequestsDetails extends ChangeRequestApi with OneParam with StartsAtVersion3 with SortIndex { val z = zz
    val description = "Update information about given change request"
    val (action, path)  = POST / "changeRequests" / "{id}"
  }

  def endpoints = ca.mrvisser.sealerate.values[ChangeRequestApi].toList.sortBy( _.z )
}

sealed trait DirectiveApi extends EndpointSchema with GeneralApi with SortIndex
object DirectiveApi {

  final case object ListDirectives extends DirectiveApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "List all directives"
    val (action, path)  = GET / "directives"
  }
  final case object DirectiveDetails extends DirectiveApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Get information about given directive"
    val (action, path)  = GET / "directives" / "{id}"
  }
  final case object CreateDirective extends DirectiveApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Create a new directive"
    val (action, path)  = PUT / "directives"
  }
  final case object DeleteDirective extends DirectiveApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Delete given directive"
    val (action, path)  = DELETE / "directives" / "{id}"
  }
  final case object CheckDirective extends DirectiveApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Check if the given directive can be migrated to target technique version"
    val (action, path)  = POST / "directives" / "{id}" / "check"
  }
  final case object UpdateDirective extends DirectiveApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Update given directive information"
    val (action, path)  = POST / "directives" / "{id}"
  }

  def endpoints = ca.mrvisser.sealerate.values[DirectiveApi].toList.sortBy( _.z )
}

sealed trait NcfApi extends EndpointSchema with GeneralApi with SortIndex
object NcfApi {

  final case object UpdateTechnique extends NcfApi with ZeroParam with StartsAtVersion9 with SortIndex { val z = zz
    val description = "Update technique created with technique editor"
    val (action, path)  = POST / "ncf"
  }
  final case object CreateTechnique extends NcfApi with ZeroParam with StartsAtVersion9 with SortIndex { val z = zz
    val description = "Create a new technique in Rudder from a technique in the technique editor"
    val (action, path)  = PUT / "ncf"
  }

  def endpoints = ca.mrvisser.sealerate.values[NcfApi].toList.sortBy( _.z )
}

sealed trait NodeApi extends EndpointSchema with GeneralApi with SortIndex
object NodeApi {

  final case object ListAcceptedNodes extends NodeApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "List all accepted nodes with configurable details level"
    val (action, path)  = GET / "nodes"
  }
  final case object ListPendingNodes extends NodeApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "List all pending nodes with configurable details level"
    val (action, path)  = GET / "nodes" / "pending"
  }
  final case object PendingNodeDetails extends NodeApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Get information about the given pending node"
    val (action, path)  = GET / "nodes" / "pending" / "{id}"
  }
  final case object NodeDetails extends NodeApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Get information about the given accepted node"
    val (action, path)  = GET / "nodes" / "{id}"
  }
  final case object UpdateNode extends NodeApi with OneParam with StartsAtVersion5 with SortIndex { val z = zz
    val description = "Update given node information (node properties, policy mode...)"
    val (action, path)  = POST / "nodes" / "{id}"
  }
  final case object DeleteNode extends NodeApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Delete given node"
    val (action, path)  = DELETE / "nodes" / "{id}"
  }
  final case object ChangePendingNodeStatus extends NodeApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Accept or refuse pending nodes"
    val (action, path)  = POST / "nodes" / "pending"
  }
  final case object ChangePendingNodeStatus2 extends NodeApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    override val name = "ChangePendingNodeStatus"
    val description = "Accept or refuse given pending node"
    val (action, path)  = POST / "nodes" / "pending" / "{id}"
  }
  final case object ApplyPocicyAllNodes extends NodeApi with ZeroParam with StartsAtVersion8 with SortIndex { val z = zz
    val description = "Ask all nodes to start a run with the given policy"
    val (action, path)  = POST / "nodes" / "applyPolicy"
  }
  final case object ApplyPolicy extends NodeApi with OneParam with StartsAtVersion8 with SortIndex { val z = zz
    val description = "Ask given node to start a run with the given policy"
    val (action, path)  = POST / "nodes" / "{id}" / "applyPolicy"
  }

  def endpoints = ca.mrvisser.sealerate.values[NodeApi].toList.sortBy( _.z )
}
sealed trait ParameterApi extends EndpointSchema with GeneralApi with SortIndex
object ParameterApi {

  final case object ListParameters extends ParameterApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "List all global parameters"
    val (action, path)  = GET / "parameters"
  }
  final case object CreateParameter extends ParameterApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Create a new parameter"
    val (action, path)  = PUT / "parameters"
  }
  final case object ParameterDetails extends ParameterApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Get information about the given parameter"
    val (action, path)  = GET / "parameters" / "{id}"
  }
  final case object DeleteParameter extends ParameterApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Delete given parameter"
    val (action, path)  = DELETE / "parameters" / "{id}"
  }
  final case object UpdateParameter extends ParameterApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Update information about given parameter"
    val (action, path)  = POST / "parameters" / "{id}"
  }

  def endpoints = ca.mrvisser.sealerate.values[ParameterApi].toList.sortBy( _.z )
}

sealed trait SettingsApi extends EndpointSchema with GeneralApi with SortIndex
object SettingsApi {
  final case object GetAllSettings extends SettingsApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Get information about all Rudder settings"
    val (action, path)  = GET / "settings"
  }
  final case object GetSetting extends SettingsApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Get information about given Rudder setting"
    val (action, path)  = GET / "settings" / "{key}"
  }
  final case object ModifySettings extends SettingsApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Update Rudder settings"
    val (action, path)  = POST / "settings"
  }
  final case object ModifySetting extends SettingsApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Update given Rudder setting"
    val (action, path)  = POST / "settings" / "{key}"
  }

  def endpoints = ca.mrvisser.sealerate.values[SettingsApi].toList.sortBy( _.z )
}

sealed trait UserApi extends EndpointSchema with InternalApi with SortIndex
object UserApi {
  final case object GetApiToken extends UserApi with ZeroParam with StartsAtVersion10 with SortIndex { val z = zz
    val description = "Get information about user personal API token"
    val (action, path)  = GET / "user" / "api" / "token"
  }
  final case object CreateApiToken extends UserApi with ZeroParam with StartsAtVersion10 with SortIndex { val z = zz
    val description = "Create user personal API token"
    val (action, path)  = PUT / "user" / "api" / "token"
  }
  final case object DeleteApiToken extends UserApi with ZeroParam with StartsAtVersion10 with SortIndex { val z = zz
    val description = "Delete user personal API token"
    val (action, path)  = DELETE / "user" / "api" / "token"
  }

  def endpoints = ca.mrvisser.sealerate.values[UserApi].toList.sortBy( _.z )
}

sealed trait TechniqueApi extends EndpointSchema with GeneralApi with SortIndex
object TechniqueApi {

  final case object ListTechniques extends TechniqueApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "List all techniques"
    val (action, path)  = GET / "techniques"
  }
  final case object ListTechniquesDirectives extends TechniqueApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "List directives derived from given technique"
    val (action, path)  = GET / "techniques" / "{name}" / "directives"
  }
  final case object ListTechniqueDirectives extends TechniqueApi with TwoParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "List directives derived from given technique for given version"
    val (action, path)  = GET / "techniques" / "{name}" / "{version}" / "directives"
  }

  def endpoints = ca.mrvisser.sealerate.values[TechniqueApi].toList.sortBy( _.z )
}

sealed trait RuleApi extends EndpointSchema with GeneralApi with SortIndex
object RuleApi {

  final case object ListRules extends RuleApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "List all rules with their information"
    val (action, path)  = GET / "rules"
  }
  final case object CreateRule extends RuleApi with ZeroParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Create a new rule"
    val (action, path)  = PUT / "rules"
  }
  final case object RuleDetails extends RuleApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Get information about given rule"
    val (action, path)  = GET / "rules" / "{id}"
  }
  final case object DeleteRule extends RuleApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Delete given rule"
    val (action, path)  = DELETE / "rules" / "{id}"
  }
  final case object UpdateRule extends RuleApi with OneParam with StartsAtVersion2 with SortIndex { val z = zz
    val description = "Update information about given rule"
    val (action, path)  = POST / "rules" / "{id}"
  }
  final case object GetRuleTree extends RuleApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Get rule categories and rule structured in a tree format"
    val (action, path)  = GET / "rules" / "tree"
  }
  final case object GetRuleCategoryDetails extends RuleApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Get information about given rule category"
    val (action, path)  = GET / "rules" / "categories" / "{id}"
  }
  final case object DeleteRuleCategory extends RuleApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Delete given category"
    val (action, path)  = DELETE / "rules" / "categories" / "{id}"
  }
  final case object UpdateRuleCategory extends RuleApi with OneParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Update information about given rule category"
    val (action, path)  = POST / "rules" / "categories" / "{id}"
  }
  final case object CreateRuleCategory extends RuleApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Create a new rule category"
    val (action, path)  = PUT / "rules" / "categories"
  }

  def endpoints = ca.mrvisser.sealerate.values[RuleApi].toList.sortBy( _.z )
}

sealed trait InfoApi extends EndpointSchema with GeneralApi with SortIndex
object InfoApi {

  final case object ApiGeneralInformations extends InfoApi with ZeroParam with StartsAtVersion6 with SortIndex { val z = zz
    val description = "Get information about Rudder public API"
    val (action, path)  = GET / "info"
  }

  final case object ApiInformations extends InfoApi with OneParam with StartsAtVersion10 with SortIndex { val z = zz
    val description = "Get detailed information about Rudder public API with the given name"
    val (action, path)  = GET / "info" / "details" / "{id}"
  }

  final case object ApiSubInformations extends InfoApi with OneParam with StartsAtVersion10 with SortIndex { val z = zz
    val description = "Get information about Rudder public API starting with given path"
    val (action, path)  = GET / "info" / "{id}"
  }

  def endpoints = ca.mrvisser.sealerate.values[InfoApi].toList.sortBy( _.z )
}

/*
 * All API.
 */
object AllApi {
  val api: List[EndpointSchema] =
    ComplianceApi.endpoints :::
    GroupApi.endpoints :::
    ChangeRequestApi.endpoints :::
    DirectiveApi.endpoints :::
    NcfApi.endpoints :::
    NodeApi.endpoints :::
    ParameterApi.endpoints :::
    SettingsApi.endpoints :::
    TechniqueApi.endpoints :::
    RuleApi.endpoints :::
    InfoApi.endpoints :::
    Nil
}

