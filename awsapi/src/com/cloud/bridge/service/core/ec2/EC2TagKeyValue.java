/*
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
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
package com.cloud.bridge.service.core.ec2;

public class EC2TagKeyValue {
    private String key;
    private String value;

    public EC2TagKeyValue() {
        key      = null;
        value    = null;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}