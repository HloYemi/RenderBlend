package committee.nova.mods.renderblender.api.iface.transform;

import committee.nova.mods.renderblender.api.client.model.PerspectiveModelState;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;

public interface IBowTransform {
    default PerspectiveModelState getBowTransform() {
        return TransformUtils.DEFAULT_BOW;
    }
}
