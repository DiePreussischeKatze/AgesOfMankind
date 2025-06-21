package org.src.components.province;

import earcut4j.Earcut;
import org.src.core.helper.Helper;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.ShaderStorage;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.src.core.helper.Consts.RECTANGLE_INDICES;

public final class Province {
	private final Mesh boxMesh;
	private final ShaderStorage shaderStorage;

	private final Mesh mesh;

	private float[] color;
	private float[] pointsPositions;

	private boolean drawFill;
	private boolean drawPoints;

	public Province() {
		drawFill = drawPoints = true;

		shaderStorage = new ShaderStorage(new float[] {}, 1);

		mesh = new Mesh(new float[]{}, new int[]{}, new byte[]{2, 3});

		boxMesh = new Mesh(new float[]{
				0.0008f,  0.0008f,
				0.0008f, -0.0008f,
				-0.0008f, -0.0008f,
				-0.0008f,  0.0008f,
		}, RECTANGLE_INDICES, new byte[] {
				2
		});

		pointsPositions = new float[0];

		color = new float[3];
		color[0] = 0.8f;
		color[1] = 0.2f;
		color[2] = 0.2f;
	}

	public void addPoint(final float x, final float y) {
		pointsPositions = Helper.insertElementsToFloatArray(pointsPositions, new float[] {x, y});
		mesh.setVertices(Helper.insertElementsToFloatArray(mesh.getVertices(), new float[] {x, y, color[0], color[1], color[2]}));
		refreshMesh();
	}

	public void deleteLastPoint() {
		pointsPositions = Helper.truncateFloatArray(pointsPositions, pointsPositions.length - 2);
		mesh.setVertices(Helper.truncateFloatArray(mesh.getVertices(), mesh.getVertices().length - 5));
		refreshMesh();
	}

	private void refreshMesh() {
		final List<Integer> newIndices = Earcut.earcut(Helper.toDoubleArray(pointsPositions));
		mesh.setIndices(newIndices.stream().mapToInt(Integer::intValue).toArray());
		shaderStorage.regenerate(pointsPositions);
		mesh.regenerate();
	}

	public void clearProvincePoints() {
		pointsPositions = new float[0];
		mesh.clear();
		mesh.regenerate();
	}

	/**
	 * This method will draw a single province in 1 draw call.
	 * For performance reasons, don't use it for anything else than debugging
	 */
	public void drawAlone() {
		if (drawPoints) {
			ShaderManager.get(ShaderID.PIVOT).bind();
			shaderStorage.bind();
			boxMesh.bind();
			glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, getPivotAmount());
		}

		if (drawFill) {
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
			ShaderManager.get(ShaderID.POLYGON).bind();
			mesh.draw();
			glDisable(GL_BLEND);
		}
	}

	public void updateColor() {
		for (int i = 0; i < mesh.getVertices().length; i += 5) {
			mesh.getVertices()[i + 2] = color[0];
			mesh.getVertices()[i + 3] = color[1];
			mesh.getVertices()[i + 4] = color[2];
		}
		mesh.regenerate();
	}

	public void update() {

	}

	public void dispose() {
		boxMesh.dispose();
		shaderStorage.dispose();
	}

	public int getPivotAmount() {
		return pointsPositions.length / 2;
	}

	public int[] getIndices() {
		return mesh.getIndices();
	}

	public int getMeshOffsetSum() {
		return mesh.getOffsetSum();
	}

	public float[] getVertices() {
		return mesh.getVertices();
	}

	public float[] getColor() {
		return color;
	}

	public float[] getPointsPositions() {
		return pointsPositions;
	}

	public boolean getDrawFill() {
		return drawFill;
	}

	public void setDrawFill(boolean drawFill) {
		this.drawFill = drawFill;
	}

	public boolean getDrawPoints() {
		return drawPoints;
	}

	public void setDrawPoints(boolean drawPoints) {
		this.drawPoints = drawPoints;
	}

}
