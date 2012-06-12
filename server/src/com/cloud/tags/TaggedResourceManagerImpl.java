// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
// Automatically generated by addcopyright.py at 04/03/2012
package com.cloud.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.api.commands.ListTagsCmd;
import com.cloud.configuration.Resource;
import com.cloud.configuration.Resource.TaggedResourceType;
import com.cloud.domain.Domain;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.server.ResourceTag;
import com.cloud.server.TaggedResourceService;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import com.cloud.user.UserContext;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.Inject;
import com.cloud.utils.component.Manager;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.DbUtil;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.uuididentity.dao.IdentityDao;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.UserVmDao;

/**
 * @author Alena Prokharchyk
 */
@Local(value = { TaggedResourceService.class})
public class TaggedResourceManagerImpl implements TaggedResourceService, Manager{
    public static final Logger s_logger = Logger.getLogger(TaggedResourceManagerImpl.class);
    private String _name;
    
    private static Map<Resource.TaggedResourceType, String> _resourceMap= 
            new HashMap<Resource.TaggedResourceType, String>();
    
    private static Map<Resource.TaggedResourceType, GenericDao<?, Long>> _daoMap= 
            new HashMap<Resource.TaggedResourceType, GenericDao<?, Long>>();
    
    @Inject
    AccountManager _accountMgr;
    @Inject
    ResourceTagDao _resourceTagDao;
    @Inject
    IdentityDao _identityDao;
    @Inject
    DomainManager _domainMgr;
    @Inject
    UserVmDao _userVmDao;

    
    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
        _name = name; 
        _resourceMap.put(TaggedResourceType.UserVm, DbUtil.getTableName(VMInstanceVO.class));
        _daoMap.put(TaggedResourceType.UserVm, _userVmDao);
        
        
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    
    private Long getResourceId(String resourceId, Resource.TaggedResourceType resourceType) {
        String tableName = _resourceMap.get(resourceType);

        if (tableName == null) {
            throw new InvalidParameterValueException("Unable to find resource of type " + resourceType + " in the database");
        }
        
        return _identityDao.getIdentityId(tableName, resourceId);
    }
    
    private Pair<Long, Long> getAccountDomain(long resourceId, Resource.TaggedResourceType resourceType) {
        String tableName = _resourceMap.get(resourceType);

        if (tableName == null) {
            throw new InvalidParameterValueException("Unable to find resource of type " + resourceType + " in the database");
        }
        
        Pair<Long, Long> pair = _identityDao.getAccountDomainInfo(tableName, resourceId);
        Long accountId = pair.first();
        Long domainId = pair.second();
        
        if (accountId == null) {
            accountId = Account.ACCOUNT_ID_SYSTEM;
        }
        
        if (domainId == null) {
            domainId = Domain.ROOT_DOMAIN;
        }
        
        return new Pair<Long, Long>(accountId, domainId);
    }

    @Override
    public TaggedResourceType getResourceType(String resourceTypeStr) {
        Resource.TaggedResourceType resourceType = null;
        try {
            resourceType = Resource.TaggedResourceType.valueOf(resourceTypeStr);
         } catch (IllegalArgumentException ex) {
             throw new InvalidParameterValueException("Invalid resource type " + resourceType);
         }
        
        return resourceType;
    }

    @Override
    @DB
    public List<ResourceTag> createTags(List<String> resourceIds, TaggedResourceType resourceType, Map<String, String> tags) {
        Account caller = UserContext.current().getCaller();
        
        List<ResourceTag> resourceTags = new ArrayList<ResourceTag>(tags.size());
        
        Transaction txn = Transaction.currentTxn();
        txn.start();
        
        for (String tag : tags.keySet()) {
            for (String resourceId : resourceIds) {
                Long id = getResourceId(resourceId, resourceType);
                
                //check if object exists
                if (_daoMap.get(resourceType).findById(id) == null) {
                    throw new InvalidParameterValueException("Unable to find resource by id " + resourceId + " and type " + resourceType);
                }
                
                Pair<Long, Long> accountDomainPair = getAccountDomain(id, resourceType);
                Long domainId = accountDomainPair.second();
                Long accountId = accountDomainPair.first();
                if (accountId != null) {
                    _accountMgr.checkAccess(caller, null, false, _accountMgr.getAccount(accountId));
                } else if (domainId != null && caller.getType() != Account.ACCOUNT_TYPE_NORMAL) {
                    //check permissions;
                    _accountMgr.checkAccess(caller, _domainMgr.getDomain(domainId));
                } else {
                    throw new PermissionDeniedException("Account " + caller + " doesn't have permissions to create tags" +
                    		" for resource " + tag);
                }
               
                ResourceTagVO resourceTag = new ResourceTagVO(tag, tags.get(tag), accountDomainPair.first(),
                        accountDomainPair.second(), 
                        id, resourceType);
                resourceTag = _resourceTagDao.persist(resourceTag);
                resourceTags.add(resourceTag);

            }
        }
        
        txn.commit();
        
        return resourceTags;
    }
    
    @Override
    public String getUuid(String resourceId, TaggedResourceType resourceType) {
        return _identityDao.getIdentityUuid(_resourceMap.get(resourceType), resourceId);
    }

    @Override
    public List<? extends ResourceTag> listTags(ListTagsCmd cmd) {
        Account caller = UserContext.current().getCaller();
        List<Long> permittedAccounts = new ArrayList<Long>();
        String key = cmd.getKey();
        String value = cmd.getValue();
        String resourceId = cmd.getResourceId();
        String resourceType = cmd.getResourceType();
        boolean listAll = cmd.listAll();

        Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = 
                new Ternary<Long, Boolean, ListProjectResourcesCriteria>(cmd.getDomainId(), cmd.isRecursive(), null);
       _accountMgr.buildACLSearchParameters(caller, null, cmd.getAccountName(), 
               cmd.getProjectId(), permittedAccounts, domainIdRecursiveListProject, listAll, false);
           Long domainId = domainIdRecursiveListProject.first();
       Boolean isRecursive = domainIdRecursiveListProject.second();
       ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();
       Filter searchFilter = new Filter(ResourceTagVO.class, "resourceType", false, cmd.getStartIndex(), cmd.getPageSizeVal());
       
       SearchBuilder<ResourceTagVO> sb = _resourceTagDao.createSearchBuilder();
       _accountMgr.buildACLSearchBuilder(sb, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

       sb.and("key", sb.entity().getKey(), SearchCriteria.Op.EQ);
       sb.and("value", sb.entity().getValue(), SearchCriteria.Op.EQ);
       sb.and("resourceId", sb.entity().getResourceId(), SearchCriteria.Op.EQ);
       sb.and("resourceType", sb.entity().getResourceType(), SearchCriteria.Op.EQ);
       
       // now set the SC criteria...
       SearchCriteria<ResourceTagVO> sc = sb.create();
       _accountMgr.buildACLSearchCriteria(sc, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);
       
       if (key != null) {
           sc.setParameters("key", key);
       }
       
       if (value != null) {
           sc.setParameters("value", value);
       }
       
       if (resourceId != null) {
           sc.setParameters("resourceId", resourceId);
       }
       
       if (resourceType != null) {
           sc.setParameters("resourceType", resourceType);
       }
       
       return _resourceTagDao.search(sc, searchFilter);
    }

}