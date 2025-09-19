package org.src.components.map;

import org.joml.Vector2f;
import org.src.components.province.Province;
import org.src.components.province.ProvinceType;
import org.src.core.callbacks.KeyPressCallback;
import org.src.core.helper.Config;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;
import org.src.core.helper.ShaderID;
import org.src.core.main.PerfTimer;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;
import org.src.core.managers.TextureManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.ShaderStorage;
import org.src.rendering.wrapper.Texture;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.src.core.helper.Consts.RECT_INDICES;
import static org.src.core.helper.Helper.FLOAT;
import static org.src.core.helper.Helper.FLOAT_ARR;
import static org.src.core.helper.Helper.INT_ARR;

public final class MapRenderer {

	// TODO: Divide the large textures into a bunch of small ones that can be unloaded (for the game consumes fucking 4GB)
	private final Texture[] mapOverlays;

	private final Mesh overlayMapMesh;
	private final Mesh provinceMesh;
	private final Mesh boxMesh;
	private final Mesh borderMesh;

	private final ShaderStorage pointShaderStorage;

	private DisplayMode displayMode;

	private float[] provincesPoints;

	private final float BORDER_WIDTH = FLOAT(Config.get("borderWidth")) / 10_000.0f;

	private int indicesOffset;
	private int activeOverlay;

	private boolean drawProvincePoints;
	private boolean drawProvinceFillings;

	private final KeyPressCallback pressCallback = (win, key, action, mods) -> {
		switch (key) {
			case GLFW_KEY_1 -> activeOverlay = 0;
			case GLFW_KEY_Y -> bakeBorders();
			//case GLFW_KEY_2:
			//	activeOverlay = 1;
			//	break;
		}
	};

	private final Map map;
	public MapRenderer(final Map map) {
		this.map = map;

		this.drawProvinceFillings = true;
		this.drawProvincePoints = false;

		this.displayMode = DisplayMode.POLITICAL;

		this.indicesOffset = 0;
		map.setLendProvinceID(-1);

		this.provincesPoints = new float[0];

		this.mapOverlays = new Texture[1];

		this.mapOverlays[0] = TextureManager.createTexture("res/images/map1.jpg");
		//this.mapOverlays[1] = TextureManager.createTexture("res/images/map1.jpg");

		this.pointShaderStorage = new ShaderStorage(2);

		this.provinceMesh = new Mesh(new byte[] {
				2, 3
		}, GL_DYNAMIC_DRAW);

		this.borderMesh = new Mesh(new byte[] { 2 });

		this.boxMesh = Helper.createPlainBoxMesh(0.0008f, 0.0008f);

		final float xSize = (float) mapOverlays[0].getWidth() / 1000;
		final float ySize = (float) mapOverlays[0].getHeight() / 1000;

		this.overlayMapMesh = new Mesh(new float[]{
				 xSize,  ySize, 1.0f, 1.0F,
				 xSize, -ySize, 1.0f, 0.0f,
				-xSize, -ySize, 0.0f, 0.0f,
				-xSize,  ySize, 0.0f, 1.0f,
		}, RECT_INDICES
				,new byte[] {
				2, 2
		});

		InputManager.addKeyPressCallback(pressCallback);
	}

	public void draw() {
		mapOverlays[activeOverlay].bind();
		ShaderManager.get(ShaderID.DEFAULT).bind();
		ShaderManager.get(ShaderID.DEFAULT).setInt("tex", mapOverlays[activeOverlay].getSlot());
		overlayMapMesh.draw();

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

		ShaderManager.get(ShaderID.BORDER).bind();
		borderMesh.draw();
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

		indicesOffset += province.getVertices().length / province.getVertexStride();

		pointShaderStorage.regenerate(provincesPoints);
		provinceMesh.regenerate();
	}

	// this does not change the display mode of the editor's currently held province
	public void setDisplayMode(final DisplayMode mode) {
		this.displayMode = mode;
		for (int i = 0; i < map.getProvinces().size(); i++) {
			if (i == map.getLendProvinceId()) { continue; } // because the province is not in the mesh

			float[] color = new float[3];
			Arrays.fill(color, 0.0f);

			switch (mode) {
				case POPULATION -> {
					color[0] = Math.max((float) Math.pow(map.getProvince(i).populationCount / (float) map.getMaxPopulation(), 1.0/2.2), 0.1f);
					color[1] = 0.1f;
					color[2] = 0.1f;
				}
				case TERRAIN -> {
					map.getProvince(i).setColorToType();
					color = map.getProvince(i).getColor();
				}
				case ELEVATION -> {
					color[0] = 0.1f;
					color[1] = 0.1f;
					color[2] = Math.max(map.getProvince(i).elevation / (float) map.getMaxElevation(), 0.1f);
				}
				case POLITICAL -> {
					if (map.getProvince(i).getOwner() != null && !Province.isSeaType(map.getProvince(i))) {
						color = map.getProvince(i).getOwner().getColor();
					} else {
						map.getProvince(i).setColorToType();
						color = map.getProvince(i).getColor();
					}
				}
				case ETHNICITY -> {
					if (!Province.isSeaType(map.getProvince(i))) {
						// Actual ethnicity color

					} else {
						map.getProvince(i).setColorToType();
						color = map.getProvince(i).getColor();
					}
				}
			}

			setProvinceColor(map.getProvince(i), color);
		}

		provinceMesh.regenerateGeometry();
	}

	public void updateMesh() {
		provinceMesh.regenerateGeometry();
	}

	public void setProvinceColor(final Province province, final float[] color) {
		province.setColor(color);

		for (int i = province.getVertexIndex(); i < province.getVertexIndex() + province.getVertices().length; i += province.getVertexStride()) {
			provinceMesh.vertices[i + 2] = color[0];
			provinceMesh.vertices[i + 3] = color[1];
			provinceMesh.vertices[i + 4] = color[2];
		}
	}

	public void takeProvinceFromMesh(final Province province) {

		for (int i = province.getIndicesIndex() + province.getIndices().length; i < provinceMesh.indices.length; i++) {
			provinceMesh.indices[i] -= province.getVertices().length / province.getVertexStride();
		}

		for (final Province changeOffset : map.getProvinces()) {
			// the province lays further in the array
			if (changeOffset.getIndicesIndex() <= province.getIndicesIndex()) { continue; } // we don't want to do that to our province that we are taking out

			changeOffset.setIndicesIndex(changeOffset.getIndicesIndex() - province.getIndices().length);
			changeOffset.setVertexIndex(changeOffset.getVertexIndex() - province.getVertices().length);
		}

		indicesOffset -= province.getVertices().length / province.getVertexStride();

		provinceMesh.indices = Helper.deleteElementsFromIntArray(provinceMesh.indices, province.getIndicesIndex(), province.getIndices().length);
		provinceMesh.vertices = Helper.deleteElementsFromFloatArray(provinceMesh.vertices, province.getVertexIndex(), province.getVertices().length);
		provinceMesh.regenerate();

		provincesPoints = Helper.deleteElementsFromFloatArray(provincesPoints, province.getVertexIndex() * Consts.POINT_POS_STRIDE / province.getVertexStride(), province.getVertices().length * Consts.POINT_POS_STRIDE / province.getVertexStride());
		pointShaderStorage.regenerate(provincesPoints);
	}

	public void dispose() {
		pointShaderStorage.dispose();
		boxMesh.dispose();
		overlayMapMesh.dispose();
		provinceMesh.dispose();

		for (final Texture map: mapOverlays) {
			map.dispose();
		}
	}

	public void bakeBorders() {
		this.borderMesh.clear();

		final ArrayList<Float> vertices = new ArrayList<>();
		final ArrayList<Integer> indices = new ArrayList<>();

		int indicesIndex = 0;
		for (final Province province: map.getProvinces()) {
		//final Province province = map.getProvince(74);
			//if (Province.isSeaType(province)) { continue; }

			boolean bordersAnyStates = false;
//
			for (final Province neighbor: province.getNeighbors()) {
				if (neighbor.getOwner() != province.getOwner()) {
					bordersAnyStates = true;
				}
			}
//x
			//if (!bordersAnyStates) { continue; }
			boolean shouldRemoveIndices = false;
			boolean removedIndices = false;
			int startIndicesIndex = indicesIndex;
			for (int i = 0; i < province.getPointsPoses().length - Consts.POINT_POS_STRIDE; i += Consts.POINT_POS_STRIDE) {

				// find the slope of the line connecting the two points
				final float angle = (float) Math.atan2(
					province.getPointsPoses()[i + Consts.POINT_POS_STRIDE] - province.getPointsPoses()[i],
					province.getPointsPoses()[i + Consts.POINT_POS_STRIDE + 1] - province.getPointsPoses()[i + 1]
				);

				final Vector2f offset = new Vector2f(
					(float) Math.sin(angle + (Math.PI / 2)) * BORDER_WIDTH,
					(float) Math.cos(angle + (Math.PI / 2)) * BORDER_WIDTH
				);

				final Province provinceUnderPoint = map.findProvinceUnderPoint(new Vector2f(province.getPointsPoses()[i] + offset.x / 1000, province.getPointsPoses()[i + 1] + offset.y / 1000));
				final Province provinceUnderPoint2 = map.findProvinceUnderPoint(new Vector2f(province.getPointsPoses()[i + 2] + offset.x / 1000, province.getPointsPoses()[i + 3] + offset.y / 1000));
				final Province provinceUnderPoint3 = map.findProvinceUnderPoint(new Vector2f(province.getPointsPoses()[i] - offset.x / 1000, province.getPointsPoses()[i + 1] - offset.y / 1000));
				final Province provinceUnderPoint4 = map.findProvinceUnderPoint(new Vector2f(province.getPointsPoses()[i + 2] - offset.x / 1000, province.getPointsPoses()[i + 3] - offset.y / 1000));
				
				if ((provinceUnderPoint != null && provinceUnderPoint.getOwner() != province.getOwner()) ||
					(provinceUnderPoint2 != null && provinceUnderPoint2.getOwner() != province.getOwner()) ||
					(provinceUnderPoint3 != null && provinceUnderPoint3.getOwner() != province.getOwner()) ||
					(provinceUnderPoint4 != null && provinceUnderPoint4.getOwner() != province.getOwner())
				) {
					
					// DO NOT CHANGE THE ORDER!!!!!
					vertices.add(province.getPointsPoses()[i] + offset.x);
					vertices.add(province.getPointsPoses()[i + 1] + offset.y);
					// DO NOT CHANGE THE ORDER!!!!!
					vertices.add(province.getPointsPoses()[i] - offset.x);
					vertices.add(province.getPointsPoses()[i + 1] - offset.y);

					vertices.add(province.getPointsPoses()[i + 2] + offset.x);
					vertices.add(province.getPointsPoses()[i + 3] + offset.y);

					vertices.add(province.getPointsPoses()[i + 2] - offset.x);
					vertices.add(province.getPointsPoses()[i + 3] - offset.y);
					//if (i - previousI >= 2) {
					//if (i < province.getPointsPoses().length - Consts.POINT_POS_STRIDE * 2) {
					if (shouldRemoveIndices) {
						indices.removeLast();
						indices.removeLast();
						indices.removeLast();
						
						indices.removeLast();
						indices.removeLast();
						indices.removeLast();

						indices.removeLast();
						indices.removeLast();
						indices.removeLast();

						indices.removeLast();
						indices.removeLast();
						indices.removeLast();

						//indicesIndex -= 2;
						shouldRemoveIndices = false;
						removedIndices = true;
					}
						indices.add(indicesIndex + 2);
						indices.add(indicesIndex + 1);
						indices.add(indicesIndex + 3);

						// first triangle
						indices.add(indicesIndex);
						indices.add(indicesIndex + 1);
						indices.add(indicesIndex + 2);

						indicesIndex += 2;

						indices.add(indicesIndex + 2);
						indices.add(indicesIndex + 1);
						indices.add(indicesIndex + 3);

						// first triangle
						indices.add(indicesIndex);
						indices.add(indicesIndex + 1);
						indices.add(indicesIndex + 2);

						indicesIndex += 2;
				//	}
					//}
					//previousI = i;
					//startedDrawingBorder = true;

				} else {
					if (indices.size() > 0) {
						shouldRemoveIndices = true;
					}
					//indices.add(indicesIndex + 2);
					//indices.add(indicesIndex + 1);
					//indices.add(indicesIndex + 3);
	////
					//indices.add(indicesIndex);
					//indices.add(indicesIndex + 1);
					//indices.add(indicesIndex + 2);
					//indicesIndex += 2;indices.removeLast();
					
					//indicesIndex += 4;
				}
			}
			try {
				//if (removedIndices) {
					indices.removeLast();
					indices.removeLast();
					indices.removeLast();
					indices.removeLast();
					indices.removeLast();
					indices.removeLast();
				//}
			} catch (Exception e) { }
			// Connect the first point with the last. Though this still needs to be improved			
			//indices.add(indicesIndex + 2);
			//indices.add(startIndicesIndex);
			//indices.add(indicesIndex + 3);
			//
			//indices.add(startIndicesIndex);
			//indices.add(startIndicesIndex + 1);
			//indices.add(indicesIndex + 3);
			
			// Stop the "spaghetti"
			//indices.add(indicesIndex + 2);
			//indices.add(indicesIndex + 1);
			//indices.add(indicesIndex + 3);
////
			//indices.add(indicesIndex);
			//indices.add(indicesIndex + 1);
			//indices.add(indicesIndex + 2);
			//indicesIndex += 4;
			//
			//for (int i = 0; i < province.getPointsPoses().length - Consts.POINT_POS_STRIDE * 2; i += Consts.POINT_POS_STRIDE) {
			//	
			//}

			

		}

		this.borderMesh.vertices = FLOAT_ARR(vertices);
		this.borderMesh.indices = INT_ARR(indices);

		this.borderMesh.regenerate();
	}

	public boolean getDrawProvincePoints() {
		return drawProvincePoints;
	}

	public boolean getDrawProvinceFillings() {
		return drawProvinceFillings;
	}

	public DisplayMode getDisplayMode() {
		return this.displayMode;
	}

	public void setDrawProvincePoints(boolean drawProvincePoints) {
		this.drawProvincePoints = drawProvincePoints;
	}

	public void setDrawProvinceFillings(boolean drawProvinceFillings) {
		this.drawProvinceFillings = drawProvinceFillings;
	}

}
