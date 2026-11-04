package committee.nova.mods.renderblender.api.iface.transform;


import committee.nova.mods.renderblender.api.client.model.PerspectiveModelState;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;

public interface IToolTransform {

    default PerspectiveModelState getToolTransform() {
        return TransformUtils.DEFAULT_TOOL;
    }

}
