package com.spicter.persistence.testdomain.model.squad;

import com.curtis.model.AssociationEnd;
import com.curtis.model.NamedBusinessObject;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;

public interface Player extends NamedBusinessObject<Player>
{

    @AssociationEnd
    Association<Player> boss();

    @AssociationEnd(role = "knows")
    ManyAssociation<Player> knows();

    @AssociationEnd(role = "seniorPlayers")
    Association<Team> club();

    @AssociationEnd
    Association<Team> nationalTeam();
}
