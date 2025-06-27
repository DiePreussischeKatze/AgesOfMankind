package org.src.components;

import org.joml.Vector2f;
import org.src.components.province.Province;
import org.src.core.helper.Component;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.core.managers.TextureManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.ShaderStorage;
import org.src.rendering.wrapper.Texture;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.src.core.helper.Consts.RECT_INDICES;

public final class Map extends Component {
	private final Mesh overlayMapMesh;
	private final Mesh provinceMesh;
	private final Mesh boxMesh;

	private final ShaderStorage pointShaderStorage;

	private boolean drawProvinceFillings;
	private boolean drawProvincePoints;

	private float[] provincesPoints;

	private int indicesOffset;

	private final Texture texture;
	private final ArrayList<Province> provinces;

	private int lendProvince;

	public Map() {
		drawProvinceFillings = true;
		drawProvincePoints = false;
		indicesOffset = 0;
		provincesPoints = new float[0];

		lendProvince = 0;

		pointShaderStorage = new ShaderStorage(2);

		provinces = new ArrayList<>();
		texture = TextureManager.createTexture("res/images/map.png");

		this.provinceMesh = new Mesh(new byte[] {
				2, 3
		});

		this.boxMesh = Helper.createPlainBoxMesh(0.0008f, 0.0008f);

		float xSize = (float) texture.getWidth() / 1000;
		float ySize = (float) texture.getHeight() / 1000;

		overlayMapMesh = new Mesh(new float[]{
				 xSize,  ySize, 1.0f, 1.0F,
				 xSize, -ySize, 1.0f, 0.0f,
				-xSize, -ySize, 0.0f, 0.0f,
				-xSize,  ySize, 0.0f, 1.0f,
		}, RECT_INDICES
		,new byte[] {
				2, 2
		});
		
	}

	public Province createProvince() {
		// check if there aren't any empty provinces
		for (int i = 0; i < provinces.size(); i++) {
			if (provinces.get(i).getIndices().length == 0) {
				lendProvince = i;
				return provinces.get(i);
			}
		}

		final Province newProvince = new Province();
		provinces.add(newProvince);
		lendProvince = provinces.size() - 1;
		return newProvince;
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

	public void takeProvinceOut(final Province province) {

		for (int i = province.getIndicesIndex() + province.getIndices().length; i < provinceMesh.indices.length; i++) {
			provinceMesh.indices[i] -= province.getVertices().length / province.getMeshStride();
		}

		for (final Province changeOffset : provinces) {
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

	public Province findProvinceUnderPoint(final Vector2f point) {
		for (final Province province: provinces) {
			if (province.isInProvince(point)) { return province; }
		}
		return null;
	}

	public int findProvinceIndexUnderPoint(final Vector2f point) {
		for (int i = 0; i < provinces.size(); i++) {
			if (provinces.get(i).isInProvince(point)) { return i; }
		}
		return -1;
	}

	@Override
	public void draw() {
		texture.bind();
		ShaderManager.get(ShaderID.DEFAULT).bind();
		ShaderManager.get(ShaderID.DEFAULT).setInt("tex", texture.getSlot());
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
	}

	@Override
	public void update(double deltaTime) {
		provinces.forEach(Province::update);
	}

	@Override
	public void dispose() {
		texture.dispose();
		overlayMapMesh.dispose();
	}

	public boolean getDrawProvinceFillings() {
		return drawProvinceFillings;
	}

	public void setDrawProvinceFillings(boolean drawProvinceFillings) {
		this.drawProvinceFillings = drawProvinceFillings;
	}

	public boolean getDrawProvincePoints() {
		return drawProvincePoints;
	}

	public void setDrawProvincePoints(boolean drawProvincePoints) {
		this.drawProvincePoints = drawProvincePoints;
	}

	public void toggleDrawProvincePoints() {
		drawProvincePoints = !drawProvincePoints;
	}

	public void toggleDrawProvinceFillings() {
		drawProvinceFillings = !drawProvinceFillings;
	}

	public Province getProvince(final int index) {
		return provinces.get(index);
	}

	public int getAmountOfProvinces() {
		return provinces.size();
	}

	public int getLendProvinceId() {
		return lendProvince;
	}

	public void setLendProvince(int lendProvince) {
		this.lendProvince = lendProvince;
	}

	public ArrayList<Province> getProvinces() {
		return provinces;
	}

}
