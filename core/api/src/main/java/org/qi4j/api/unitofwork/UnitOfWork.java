/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2013, Paul Merlin. All Rights Reserved.
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
package org.qi4j.api.unitofwork;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.MetaInfoHolder;
import org.qi4j.api.usecase.Usecase;

/**
 * All operations on entities goes through an UnitOfWork.
 * <p>A UnitOfWork allows you to access
 * Entities and work with them. All modifications to Entities are recorded by the UnitOfWork,
 * and at the end they may be sent to the underlying EntityStore by calling complete(). If the
 * UoW was read-only you may instead simply discard() it.
 * </p>
 * <p>
 * A UoW differs from a traditional Transaction in the sense that it is not tied at all to the underlying
 * storage resource. Because of this there is no timeout on a UoW. It can be very short or very long.
 * Another difference is that if a call to complete() fails, and the cause is validation errors in the
 * Entities of the UoW, then these can be corrected and the UoW retried. By contrast, when a Transaction
 * commit fails, then the whole transaction has to be done from the beginning again.
 * </p>
 * <p>
 * A UoW can be associated with a Usecase. A Usecase describes the metainformation about the process
 * to be performed by the UoW.
 * </p>
 * <p>
 * If a code block that uses a UoW throws an exception you need to ensure that this is handled properly,
 * and that the UoW is closed before returning. Because discard() is a no-op if the UoW is closed, we therefore
 * recommend the following template to be used:
 * </p>
 * <pre>
 *     UnitOfWork uow = module.newUnitOfWork();
 *     try
 *     {
 *         ...
 *         uow.complete();
 *     }
 *     finally
 *     {
 *         uow.discard();
 *     }
 * </pre>
 * <p>
 * This ensures that in the happy case the UoW is completed, and if any exception is thrown the UoW is discarded. After
 * the UoW has completed the discard() method doesn't do anything, and so has no effect. You can choose to either add
 * catch blocks for any exceptions, including exceptions from complete(), or skip them.
 * </p>
 * <p>
 * Since 2.1 you can leverage Java 7 Automatic Resource Management (ie. Try With Resources) and use the following
 * template instead:
 * </p>
 * <pre>
 *     try( UnitOfWork uow = module.newUnitOfWork() )
 *     {
 *         ...
 *         uow.complete();
 *     }
 * </pre>
 * <p>It has the very same effect than the template above but is shorter.</p>
 */
public interface UnitOfWork extends MetaInfoHolder, AutoCloseable
{

    /**
     * Get the UnitOfWorkFactory that this UnitOfWork was created from.
     *
     * @return The UnitOfWorkFactory instance that was used to create this UnitOfWork.
     */
    UnitOfWorkFactory unitOfWorkFactory();

    long currentTime();

    /**
     * Get the Usecase for this UnitOfWork
     *
     * @return the Usecase
     */
    Usecase usecase();

    void setMetaInfo( Object metaInfo );

    <T> Query<T> newQuery( QueryBuilder<T> queryBuilder );

//    DataSet newDataSetBuilder(Specification<?>... constraints);

    /**
     * Create a new Entity which implements the given mixin type. An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * EntityComposites implement the type then an AmbiguousTypeException will be thrown.
     * <p/>
     * The identity of the Entity will be generated by the IdentityGenerator of the Module of the EntityComposite.
     *
     * @param type the mixin type that the EntityComposite must implement
     *
     * @return a new Entity
     *
     * @throws NoSuchEntityException       if no EntityComposite type of the given mixin type has been registered
     * @throws org.qi4j.api.entity.LifecycleException
     *                                     if the entity cannot be created
     * @throws EntityTypeNotFoundException
     */
    <T> T newEntity( Class<T> type )
        throws EntityTypeNotFoundException, LifecycleException;

    /**
     * Create a new Entity which implements the given mixin type. An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * EntityComposites implement the type then an AmbiguousTypeException will be thrown.
     *
     * @param type     the mixin type that the EntityComposite must implement
     * @param identity the identity of the new Entity
     *
     * @return a new Entity
     *
     * @throws NoSuchEntityException       if no EntityComposite type of the given mixin type has been registered
     * @throws LifecycleException          if the entity cannot be created
     * @throws EntityTypeNotFoundException
     */
    <T> T newEntity( Class<T> type, String identity )
        throws EntityTypeNotFoundException, LifecycleException;

    /**
     * Create a new EntityBuilder for an EntityComposite which implements the given mixin type. An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * EntityComposites implement the type then an AmbiguousTypeException will be thrown.
     *
     * @param type the mixin type that the EntityComposite must implement
     *
     * @return a new Entity
     *
     * @throws NoSuchEntityException       if no EntityComposite type of the given mixin type has been registered
     * @throws LifecycleException
     * @throws EntityTypeNotFoundException
     */
    <T> EntityBuilder<T> newEntityBuilder( Class<T> type )
        throws EntityTypeNotFoundException;

    /**
     * Create a new EntityBuilder for an EntityComposite which implements the given mixin type. An EntityComposite
     * will be chosen according to what has been registered and the visibility rules
     * for Modules and Layers will be considered. If several
     * mixins implement the type then an AmbiguousTypeException will be thrown.
     *
     * @param type     the mixin type that the EntityComposite must implement
     * @param identity the identity of the new Entity
     *
     * @return a new Entity
     *
     * @throws NoSuchEntityException       if no EntityComposite type of the given mixin type has been registered
     * @throws LifecycleException
     * @throws EntityTypeNotFoundException
     */
    <T> EntityBuilder<T> newEntityBuilder( Class<T> type, String identity )
        throws EntityTypeNotFoundException;

    /**
     * Find an Entity of the given mixin type with the give identity. This
     * method verifies that it exists by asking the underlying EntityStore.
     *
     * @param type     of the entity
     * @param identity of the entity
     *
     * @return the entity
     *
     * @throws EntityTypeNotFoundException if no entity type could be found
     * @throws NoSuchEntityException
     */
    <T> T get( Class<T> type, String identity )
        throws EntityTypeNotFoundException, NoSuchEntityException;

    /**
     * If you have a reference to an Entity from another
     * UnitOfWork and want to create a reference to it in this
     * UnitOfWork, then call this method.
     *
     * @param entity the Entity to be dereferenced
     *
     * @return an Entity from this UnitOfWork
     *
     * @throws EntityTypeNotFoundException if no entity type could be found
     */
    <T> T get( T entity )
        throws EntityTypeNotFoundException;

    /**
     * Remove the given Entity.
     *
     * @param entity the Entity to be removed.
     *
     * @throws LifecycleException if the entity could not be removed
     */
    void remove( Object entity )
        throws LifecycleException;

    /**
     * Complete this UnitOfWork. This will send all the changes down to the underlying
     * EntityStore's.
     *
     * @throws UnitOfWorkCompletionException if the UnitOfWork could not be completed
     * @throws ConcurrentEntityModificationException
     *                                       if entities have been modified by others
     */
    void complete()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException;

    /**
     * Discard this UnitOfWork. Use this if a failure occurs that you cannot handle,
     * or if the usecase was of a read-only character. This is a no-op of the UnitOfWork
     * is already closed.
     */
    void discard();

    /**
     * Discard this UnitOfWork. Use this if a failure occurs that you cannot handle,
     * or if the usecase was of a read-only character. This is a no-op of the UnitOfWork
     * is already closed. This simply call the {@link #discard()} method and is an
     * implementation of the {@link AutoCloseable} interface providing Try With Resources
     * support for UnitOfWork.
     */
    @Override
    public void close();
    
    /**
     * Check if the UnitOfWork is open. It is closed after either complete() or discard()
     * methods have been called successfully.
     *
     * @return true if the UnitOfWork is open.
     */
    boolean isOpen();

    /**
     * Check if the UnitOfWork is paused. It is not paused after it has been create through the
     * UnitOfWorkFactory, and it can be paused by calling {@link #pause()} and then resumed by calling
     * {@link #resume()}.
     *
     * @return true if this UnitOfWork has been paused.
     */
    boolean isPaused();

    /**
     * Pauses this UnitOfWork.
     * <p>
     * Calling this method will cause the underlying UnitOfWork to become the current UnitOfWork until the
     * the resume() method is called. It is the client's responsibility not to drop the reference to this
     * UnitOfWork while being paused.
     * </p>
     */
    void pause();

    /**
     * Resumes this UnitOfWork to again become the current UnitOfWork.
     */
    void resume();

    /**
     * Register a callback. Callbacks are invoked when the UnitOfWork
     * is completed or discarded.
     *
     * @param callback a callback to be registered with this UnitOfWork
     */
    void addUnitOfWorkCallback( UnitOfWorkCallback callback );

    /**
     * Unregister a callback. Callbacks are invoked when the UnitOfWork
     * is completed or discarded.
     *
     * @param callback a callback to be unregistered with this UnitOfWork
     */
    void removeUnitOfWorkCallback( UnitOfWorkCallback callback );
}
