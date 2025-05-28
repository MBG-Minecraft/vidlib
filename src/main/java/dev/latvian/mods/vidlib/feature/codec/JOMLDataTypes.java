package dev.latvian.mods.vidlib.feature.codec;

import dev.latvian.mods.kmath.codec.JOMLCodecs;
import dev.latvian.mods.kmath.codec.JOMLStreamCodecs;
import org.joml.Matrix2d;
import org.joml.Matrix2f;
import org.joml.Matrix3d;
import org.joml.Matrix3f;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

public interface JOMLDataTypes {
	DataType<Vector2f> VEC_2 = DataType.of(JOMLCodecs.VEC_2, JOMLStreamCodecs.VEC_2, Vector2f.class);
	DataType<Vector3f> VEC_3 = DataType.of(JOMLCodecs.VEC_3, JOMLStreamCodecs.VEC_3, Vector3f.class);
	DataType<Vector4f> VEC_4 = DataType.of(JOMLCodecs.VEC_4, JOMLStreamCodecs.VEC_4, Vector4f.class);

	DataType<Quaternionf> QUATERNION = DataType.of(JOMLCodecs.QUATERNION, JOMLStreamCodecs.QUATERNION, Quaternionf.class);

	DataType<Vector2f> VEC_2S = DataType.of(JOMLCodecs.VEC_2S, JOMLStreamCodecs.VEC_2S, Vector2f.class);
	DataType<Vector3f> VEC_3S = DataType.of(JOMLCodecs.VEC_3S, JOMLStreamCodecs.VEC_3S, Vector3f.class);
	DataType<Vector4f> VEC_4S = DataType.of(JOMLCodecs.VEC_4S, JOMLStreamCodecs.VEC_4S, Vector4f.class);

	DataType<Matrix2f> MAT_2 = DataType.of(JOMLCodecs.MAT_2, JOMLStreamCodecs.MAT_2, Matrix2f.class);
	DataType<Matrix3f> MAT_3 = DataType.of(JOMLCodecs.MAT_3, JOMLStreamCodecs.MAT_3, Matrix3f.class);
	DataType<Matrix4f> MAT_4 = DataType.of(JOMLCodecs.MAT_4, JOMLStreamCodecs.MAT_4, Matrix4f.class);

	DataType<Vector2d> DVEC_2 = DataType.of(JOMLCodecs.DVEC_2, JOMLStreamCodecs.DVEC_2, Vector2d.class);
	DataType<Vector3d> DVEC_3 = DataType.of(JOMLCodecs.DVEC_3, JOMLStreamCodecs.DVEC_3, Vector3d.class);
	DataType<Vector4d> DVEC_4 = DataType.of(JOMLCodecs.DVEC_4, JOMLStreamCodecs.DVEC_4, Vector4d.class);

	DataType<Quaterniond> DQUATERNION = DataType.of(JOMLCodecs.DQUATERNION, JOMLStreamCodecs.DQUATERNION, Quaterniond.class);

	DataType<Vector2d> DVEC_2S = DataType.of(JOMLCodecs.DVEC_2S, JOMLStreamCodecs.DVEC_2S, Vector2d.class);
	DataType<Vector3d> DVEC_3S = DataType.of(JOMLCodecs.DVEC_3S, JOMLStreamCodecs.DVEC_3S, Vector3d.class);
	DataType<Vector4d> DVEC_4S = DataType.of(JOMLCodecs.DVEC_4S, JOMLStreamCodecs.DVEC_4S, Vector4d.class);

	DataType<Matrix2d> DMAT_2 = DataType.of(JOMLCodecs.DMAT_2, JOMLStreamCodecs.DMAT_2, Matrix2d.class);
	DataType<Matrix3d> DMAT_3 = DataType.of(JOMLCodecs.DMAT_3, JOMLStreamCodecs.DMAT_3, Matrix3d.class);
	DataType<Matrix4d> DMAT_4 = DataType.of(JOMLCodecs.DMAT_4, JOMLStreamCodecs.DMAT_4, Matrix4d.class);
}
