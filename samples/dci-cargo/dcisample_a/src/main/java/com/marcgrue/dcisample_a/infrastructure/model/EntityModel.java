package com.marcgrue.dcisample_a.infrastructure.model;

import com.marcgrue.dcisample_a.infrastructure.conversion.DTO;
import org.apache.wicket.model.IModel;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.NoSuchEntityException;

/**
 * Javadoc
 */
public class EntityModel<T extends DTO, U extends EntityComposite>
      extends ReadOnlyModel<T>
{
    private Class<U> entityClass;
    private String identity;
    private Class<T> dtoClass;

    private transient T dtoComposite;

    public EntityModel( Class<U> entityClass, String identity, Class<T> dtoClass )
    {
        this.entityClass = entityClass;
        this.identity = identity;
        this.dtoClass = dtoClass;
    }

    public static <T extends DTO, U extends EntityComposite> IModel<T> of(
          Class<U> entityClass, String identity, Class<T> dtoClass )
    {
        return new EntityModel<T, U>( entityClass, identity, dtoClass );
    }

    public T getObject()
    {
        if (dtoComposite == null && identity != null)
        {
            dtoComposite = valueConverter.convert( dtoClass, loadEntity() );
        }
        return dtoComposite;
    }

    public void detach()
    {
        dtoComposite = null;
    }

    private U loadEntity()
    {
        U entity = uowf.currentUnitOfWork().get( entityClass, identity );
        if (entity == null)
        {
            throw new NoSuchEntityException( EntityReference.parseEntityReference( identity ) );
        }
        return entity;
    }
}
