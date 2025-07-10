package org.src.components.map;

import org.lwjgl.glfw.GLFW;
import org.src.components.province.Province;
import org.src.core.callbacks.KeyPressCallback;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;
import org.src.core.helper.ShaderID;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.ShaderStorage;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

public final class MapRenderer {

	// TODO: Divide the large textures into a bunch of small ones that can be unloaded (for the game consumes fucking 4GB)
	// TODO: readd later when needed
	//private final Texture[] mapOverlays;
//
	//private final Mesh overlayMapMesh;
	private final Mesh provinceMesh;
	private final Mesh boxMesh;

	private final ShaderStorage pointShaderStorage;

	private float[] provincesPoints;

	private int indicesOffset;
	private int activeOverlay;

	private boolean drawProvincePoints;
	private boolean drawProvinceFillings;

	private final KeyPressCallback pressCallback = (win, key, action, mods) -> {
		switch (key) {
			case GLFW_KEY_1:
				activeOverlay = 0;
				break;
			case GLFW_KEY_2:
				activeOverlay = 1;
				break;
		}
	};

	private final Map map;
	public MapRenderer(final Map map) {
		this.map = map;

		this.drawProvinceFillings = true;
		this.drawProvincePoints = false;

		this.indicesOffset = 0;

		this.provincesPoints = new float[0];

		//this.mapOverlays = new Texture[2];

		//this.mapOverlays[0] = TextureManager.createTexture("res/images/map0.png");
		//this.mapOverlays[1] = TextureManager.createTexture("res/images/map1.jpg");

		this.pointShaderStorage = new ShaderStorage(2);

		this.provinceMesh = new Mesh(new byte[] {
				2, 3
		});

		this.boxMesh = Helper.createPlainBoxMesh(0.0008f, 0.0008f);

		//final float xSize = (float) mapOverlays[0].getWidth() / 1000;
		//final float ySize = (float) mapOverlays[0].getHeight() / 1000;

		//this.overlayMapMesh = new Mesh(new float[]{
		//		xSize,  ySize, 1.0f, 1.0F,
		//		xSize, -ySize, 1.0f, 0.0f,
		//		-xSize, -ySize, 0.0f, 0.0f,
		//		-xSize,  ySize, 0.0f, 1.0f,
		//}, RECT_INDICES
		//		,new byte[] {
		//		2, 2
		//});

		InputManager.addKeyPressCallback(pressCallback);
	}

	public void draw() {
	//	mapOverlays[activeOverlay].bind();
		ShaderManager.get(ShaderID.DEFAULT).bind();
	//	ShaderManager.get(ShaderID.DEFAULT).setInt("tex", mapOverlays[activeOverlay].getSlot());
	//	overlayMapMesh.draw();

		if (drawProvincePoints) {
			ShaderManager.get(ShaderID.MAP_PIVOT).bind();
			pointShaderStorage.bind();
			boxMesh.bind();
			glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, provincesPoints.length / Consts.POINT_POS_STRIDE);
		}

		if (drawProvinceFillings) {
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
			ShaderManager.get(ShaderID.POLYGON).bind();
			provinceMesh.draw();
			glDisable(GL_BLEND);
		}
	}

	public void addProvinceToMesh(final Province province) {
		province.setVertexIndex(provinceMesh.vertices.length);
		province.setIndicesIndex(provinceMesh.indices.length);

		provinceMesh.addVertices(province.getVertices());
		provincesPoints = Helper.addElementsToFloatArray(provincesPoints, province.getPointsPoses());

		final int[] newIndices = new int[province.getIndices().length];
		for (int i = 0; i < newIndices.length; i++) {
			newIndices[i] = province.getIndices()[i] + indicesOffset;
		}
		provinceMesh.addIndices(newIndices);

		indicesOffset += province.getVertices().length / province.getMeshStride();

		pointShaderStorage.regenerate(provincesPoints);
		provinceMesh.regenerate();
	}

	public void takeProvinceFromMesh(final Province province) {

		for (int i = province.getIndicesIndex() + province.getIndices().length; i < provinceMesh.indices.length; i++) {
			provinceMesh.indices[i] -= province.getVertices().length / province.getMeshStride();
		}

		for (final Province changeOffset : map.getProvinces()) {
			// the province lays further in the array
			if (changeOffset.getIndicesIndex() <= province.getIndicesIndex()) { continue; } // we don't want to do that to our province that we are taking out

			changeOffset.setIndicesIndex(changeOffset.getIndicesIndex() - province.getIndices().length);
			changeOffset.setVertexIndex(changeOffset.getVertexIndex() - province.getVertices().length);
		}

		indicesOffset -= province.getVertices().length / province.getMeshStride();

		provinceMesh.indices = Helper.deleteElementsFromIntArray(provinceMesh.indices, province.getIndicesIndex(), province.getIndices().length);
		provinceMesh.vertices = Helper.deleteElementsFromFloatArray(provinceMesh.vertices, province.getVertexIndex(), province.getVertices().length);
		provinceMesh.regenerate();

		provincesPoints = Helper.deleteElementsFromFloatArray(provincesPoints, province.getVertexIndex() * Consts.POINT_POS_STRIDE / province.getMeshStride(), province.getVertices().length * Consts.POINT_POS_STRIDE / province.getMeshStride());
		pointShaderStorage.regenerate(provincesPoints);
	}

	public void dispose() {
		pointShaderStorage.dispose();
		boxMesh.dispose();
		//overlayMapMesh.dispose();
		provinceMesh.dispose();
	//	for (final Texture map: mapOverlays) {
	//		map.dispose();
	//	}
	}

	public boolean getDrawProvincePoints() {
		return drawProvincePoints;
	}

	public boolean getDrawProvinceFillings() {
		return drawProvinceFillings;
	}

	public void setDrawProvincePoints(boolean drawProvincePoints) {
		this.drawProvincePoints = drawProvincePoints;
	}

	public void setDrawProvinceFillings(boolean drawProvinceFillings) {
		this.drawProvinceFillings = drawProvinceFillings;
	}

}
