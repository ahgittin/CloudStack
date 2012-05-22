/**
 *  Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.cloud.api.commands;

import org.apache.log4j.Logger;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.PlugService;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.CiscoNexusVSMResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.CiscoNexusVSMDevice;
import com.cloud.network.element.CiscoNexusVSMElementService;
import com.cloud.user.Account;
import com.cloud.user.UserContext;

@Implementation(responseObject=CiscoNexusVSMResponse.class, description="Retrieves a Cisco Nexus 1000v Virtual Switch Manager device associated with a Cluster")
public class ListCiscoVSMDetailsCmd extends BaseCmd {

    public static final Logger s_logger = Logger.getLogger(ListCiscoVSMDetailsCmd.class.getName());
    private static final String s_name = "listciscovsmdetailscmdresponse";
    @PlugService CiscoNexusVSMElementService _ciscoNexusVSMService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="cluster")
    @Parameter(name=ApiConstants.ID, type=CommandType.LONG, required = true, description="Id of the CloudStack cluster in which the Cisco Nexus 1000v VSM appliance.")
    private long clusterId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    
    public long getClusterId() {
    	return clusterId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    // NOTE- The uuid that is sent in during the invocation of the API AddCiscoNexusVSM()
    // automagically gets translated to the corresponding db id before this execute() method
    // is invoked. That's the reason why we don't have any uuid-dbid translation code here.
    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException, ResourceAllocationException {
    	CiscoNexusVSMDevice vsmDevice = _ciscoNexusVSMService.getCiscoNexusVSMByClusId(this);
        if (vsmDevice != null) {
        	CiscoNexusVSMResponse response = _ciscoNexusVSMService.createCiscoNexusVSMDetailedResponse(vsmDevice);
        	response.setObjectName("cisconexusvsm");
        	response.setResponseName(getCommandName());
        	this.setResponseObject(response);
        } else {
        	throw new ServerApiException(BaseCmd.INTERNAL_ERROR, "Failed to retrieve Cisco Nexus Virtual Switch Manager for the specified cluster due to an internal error.");
        }
    }
 
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}