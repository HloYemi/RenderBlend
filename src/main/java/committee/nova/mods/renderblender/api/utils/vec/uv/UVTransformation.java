package committee.nova.mods.renderblender.api.utils.vec.uv;

import committee.nova.mods.renderblender.api.utils.vec.ITransformation;

/**
 * UV 变换抽象基类。
 * 已剥离 IVertexOperation 依赖（该依赖在完整渲染管线中才需要）。
 */
public abstract class UVTransformation extends ITransformation<UV, UVTransformation> {

    public UVTransformation at(UV point) {
        return new UVTransformationList(new UVTranslation(-point.u, -point.v), this, new UVTranslation(point.u, point.v));
    }

    public UVTransformationList with(UVTransformation t) {
        return new UVTransformationList(this, t);
    }
}