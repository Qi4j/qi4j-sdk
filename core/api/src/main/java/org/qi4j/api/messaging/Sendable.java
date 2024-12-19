package org.qi4j.api.messaging;

import org.qi4j.api.common.HasMetadata;
import org.qi4j.api.common.HasTimestamp;
import org.qi4j.api.identity.HasIdentity;

public interface Sendable extends HasIdentity, HasTopic, HasTimestamp, HasMetadata
{
}
