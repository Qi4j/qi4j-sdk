package com.curtis.model;

import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.api.usecase.UsecaseBuilder;

@Mixins( Repository.RepositoryMixin.class )
public interface Repository
{
    <T> T unitOfWork( String usecase, Function<UnitOfWork,T> work );

    class RepositoryMixin
        implements Repository
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public <T> T unitOfWork( String usecaseName, Function<UnitOfWork,T> work )
        {
            UsecaseBuilder usecaseBuilder = UsecaseBuilder.buildUsecase( usecaseName );
            Usecase usecase = usecaseBuilder.newUsecase();
            try( UnitOfWork uow = uowf.newUnitOfWork( usecase ) )
            {
                T result = work.apply( uow );
                uow.complete();
                return result;
            }
        }
    }
}
