package org.qi4j.api.composite;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.structure.MetaInfoHolder;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

public interface MetaStateDescriptor extends MetaInfoHolder
{
    /**
     * Get the qualified name of the property which is equal to:
     * <pre><code>
     * &lt;interface name&gt;:&lt;method name&gt;
     * </code></pre>
     *
     * @return the qualified name of the property
     */
    QualifiedName qualifiedName();

    /**
     * Get the type of the property or association. If a property is declared
     * as Property&lt;X&gt; then X is returned.
     *
     * @return the property type
     */
    Type type();

    /**
     * Returns the accessor for this property or association.
     * This can either be a Field or a Method.
     */
    AccessibleObject accessor();

    /**
     * Whether the property or association is immutable.
     */
    boolean isImmutable();

    /**
     * Whether the property or association is queryable.
     */
    boolean queryable();
}
