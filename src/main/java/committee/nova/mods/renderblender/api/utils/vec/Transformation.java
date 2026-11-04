package committee.nova.mods.renderblender.api.utils.vec;

/**
 * 三维向量变换抽象基类。
 * 已剥离 IVertexOperation 依赖（该依赖在完整渲染管线中才需要）。
 */
public abstract class Transformation extends ITransformation<Vector3, Transformation> {

    /** 对法线应用此变换（不包含平移） */
    public abstract void applyN(Vector3 normal);

    /** 将本变换合并到矩阵右侧 */
    public abstract void apply(Matrix4 mat);

    public Transformation at(Vector3 point) {
        return new TransformationList(new Translation(-point.x, -point.y, -point.z), this, point.translation());
    }

    public TransformationList with(Transformation t) {
        return new TransformationList(this, t);
    }
}