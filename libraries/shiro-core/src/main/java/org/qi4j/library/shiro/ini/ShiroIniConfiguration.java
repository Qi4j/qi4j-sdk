/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.shiro.ini;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

// START SNIPPET: config
public interface ShiroIniConfiguration
        extends ConfigurationComposite
{

    /**
     * Resource path of the ini configuration file.
     * "classpath:", "file": and "url:" prefixes are supported.
     * Defaulted to "classpath:shiro.ini".
     */
    @Optional
    Property<String> iniResourcePath();

}
// END SNIPPET: config
