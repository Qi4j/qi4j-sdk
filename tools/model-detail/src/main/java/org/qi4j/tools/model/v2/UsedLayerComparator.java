package org.qi4j.tools.model.v2;

import org.qi4j.api.structure.LayerDescriptor;

import java.util.Comparator;

class UsedLayerComparator
    implements Comparator<LayerDescriptor>
{
    @Override
    public int compare(LayerDescriptor d1, LayerDescriptor d2)
    {
        if(d1.equals(d2))
        {
            return 0;
        }
        if(uses(d1, d2))
        {
            return -1;
        }
        if(uses(d2, d1))
        {
            return 1;
        }
        return 0;
    }

    private boolean uses(LayerDescriptor user, LayerDescriptor used)
    {
        if(user.equals(used))
        {
            return true;
        }
        for(LayerDescriptor usedLayer : user.usedLayers().layers().toList())
        {
            if(uses(usedLayer, used))
            {
                return true;
            }
        }
        return false;
    }
}
