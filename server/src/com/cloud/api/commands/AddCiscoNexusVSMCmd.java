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
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.IdentityMapper;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.PlugService;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.CiscoNexusVSMResponse;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.CiscoNexusVSMDeviceVO;
import com.cloud.network.element.CiscoNexusVSMElementService;
import com.cloud.user.UserContext;
import com.cloud.utils.exception.CloudRuntimeException;

@Implementation(responseObject=CiscoNexusVSMResponse.class, description="Adds a Cisco Nexus 1000v Virtual Switch Manager device")
public class AddCiscoNexusVSMCmd extends BaseAsyncCmd {

    public static final Logger s_logger = Logger.getLogger(AddCiscoNexusVSMCmd.class.getName());
    private static final String s_name = "addciscon1kvvsmresponse";
    @PlugService CiscoNexusVSMElementService _ciscoNexusVSMService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @IdentityMapper(entityTableName="external_virtual_switch_management_devices")
    @Parameter(name=ApiConstants.URL, type=CommandType.STRING, required = true, description="URL of the Cisco Nexus 1000v VSM appliance.")
    private String url;

    @Parameter(name=ApiConstants.USERNAME, type=CommandType.STRING, required = true, description="Credentials to reach the Cisco Nexus 1000v VSM device")
    private String username;
    
    @Parameter(name=ApiConstants.PASSWORD, type=CommandType.STRING, required = true, description="Credentials to reach the Cisco Nexus 1000v VSM device")
    private String password;

    // We may not need this at all. Or we may need a new device type for Switching management devices like the Cisco N1KV VSM.
    @Parameter(name = ApiConstants.NETWORK_DEVICE_TYPE, type = CommandType.STRING, required = true, description = "Cisco Nexus 1000v VSM supports physical and virtual types")
    private String deviceType;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDeviceType() {
        return deviceType;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException, ResourceAllocationException {
        try {
            CiscoNexusVSMDeviceVO vsmDeviceVO = _ciscoNexusVSMService.addCiscoNexusVSM(this);
            if (vsmDeviceVO != null) {
                CiscoNexusVSMResponse response = _ciscoNexusVSMService.createCiscoNexusVSMResponse(vsmDeviceVO);
                response.setObjectName("cisconexusvsm");
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(BaseAsyncCmd.INTERNAL_ERROR, "Failed to add Cisco Nexus Virtual Switch Manager due to internal error.");
            }
        }  catch (InvalidParameterValueException invalidParamExcp) {
            throw new ServerApiException(BaseCmd.PARAM_ERROR, invalidParamExcp.getMessage());
        } catch (CloudRuntimeException runtimeExcp) {
            throw new ServerApiException(BaseCmd.INTERNAL_ERROR, runtimeExcp.getMessage());
        }
    }

    @Override
    public String getEventDescription() {
        return "Adding a Cisco Nexus VSM device";
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_EXTERNAL_SWITCH_MGMT_DEVICE_ADD;
    }
 
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return UserContext.current().getCaller().getId();
    }
}
