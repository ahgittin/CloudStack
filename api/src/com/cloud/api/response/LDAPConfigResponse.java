package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class LDAPConfigResponse  extends BaseResponse {

    @SerializedName(ApiConstants.HOST_NAME) @Param(description="Hostname or ip address of the ldap server eg: my.ldap.com")
    private String hostname;

    @SerializedName(ApiConstants.PORT) @Param(description="Specify the LDAP port if required, default is 389")
    private String port;

    @SerializedName(ApiConstants.PORT) @Param(description="Check Use SSL if the external LDAP server is configured for LDAP over SSL")
    private String useSSL;

    @SerializedName(ApiConstants.SEARCH_BASE) @Param(description="The search base defines the starting point for the search in the directory tree Example:  dc=cloud,dc=com")
    private String searchBase;

    @SerializedName(ApiConstants.QUERY_FILTER) @Param(description="You specify a query filter here, which narrows down the users, who can be part of this domain")
    private String queryFilter;

    @SerializedName(ApiConstants.BIND_DN) @Param(description="Specify the distinguished name of a user with the search permission on the directory")
    private String bindDN;

    @SerializedName(ApiConstants.BIND_PASSWORD) @Param(description="DN password")
    private String bindPassword;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(String useSSL) {
        this.useSSL = useSSL;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public String getQueryFilter() {
        return queryFilter;
    }

    public void setQueryFilter(String queryFilter) {
        this.queryFilter = queryFilter;
    }

    public String getBindDN() {
        return bindDN;
    }

    public void setBindDN(String bindDN) {
        this.bindDN = bindDN;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }
    

}