/*
 * Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.soasecurity.user.mgt.attribute.store.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Map;

/**
 *
 */
public class AttributeStoreExtension extends AbstractUserOperationEventListener {

    private static Log log = LogFactory.getLog(AttributeStoreExtension.class);

    /**
     * If there is user store domain name with the following post prefix, it would be identified as an attribute store
     */
    private static final String ATTRIBUTE_STORE_POST_PREFIX = "-ATTRIBUTE-STORE";

    @Override
    public int getExecutionOrderId() {
        return 986732;
    }


    @Override
    public boolean doPreAuthenticate(String userName, Object credential,
                                     UserStoreManager userStoreManager) throws UserStoreException {

        log.info("doPreAuthenticate method is called before authenticating with user store");


        // check for attribute user store domains.
        String currentDomainName = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());

        if (currentDomainName.endsWith(ATTRIBUTE_STORE_POST_PREFIX)) {
            throw new UserStoreException("This is Attribute User Store Domain,  Can not authenticate users");
        }

        return true;
    }


    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated, UserStoreManager storeManager) {
        log.info("doPostAuthenticate method is called");

        return true;

    }


    @Override
    public boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
                                            Map<String, String> claimMap, UserStoreManager storeManager) throws UserStoreException {

        Map<String, String> newClaimMap;

        log.info("doPostGetUserClaimValues method is called after retrieving claim using underline user store");


        String domainName = UserCoreUtil.getDomainName(storeManager.getRealmConfiguration());


        while (storeManager.getSecondaryUserStoreManager() != null) {


            try {

                storeManager = storeManager.getSecondaryUserStoreManager();

                String userDomainName = UserCoreUtil.getDomainName(storeManager.getRealmConfiguration());

                log.info("domain - " + userDomainName);

                if (!userDomainName.startsWith(domainName) || !userDomainName.endsWith(ATTRIBUTE_STORE_POST_PREFIX)) {
                    continue;
                }

                newClaimMap = storeManager.getUserClaimValues(userName, claims, profileName);

                log.info("User store mgr != null");

                claimMap.putAll(newClaimMap);


            } catch (Exception e) {

                log.info("Secondary user store not found", e);
            }

        }

        return true;
    }

}
