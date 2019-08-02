package com.spicter.persistence.testdomain.model.squad;

import com.curtis.model.AssociationEnd;
import com.curtis.model.NamedBusinessObject;
import org.apache.polygene.api.association.ManyAssociation;

public interface Team extends NamedBusinessObject<Team>
{

    @AssociationEnd
    ManyAssociation<Player> youthPlayers();

    @AssociationEnd(role ="club")
    ManyAssociation<Player> seniorPlayers();
}
