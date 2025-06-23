package org.src.components;

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

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.src.core.helper.Consts.RECTANGLE_INDICES;

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

	public Map() {
		drawProvinceFillings = true;
		drawProvincePoints = false;
		indicesOffset = 0;
		provincesPoints = new float[0];

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
		}, RECTANGLE_INDICES
		,new byte[] {
				2, 2
		});
		
	}

	public Province createProvince() {
		final Province newProvince = new Province();
		provinces.add(newProvince);
	//	System.out.println(provinces.size());
		return newProvince;
	}

	public void addProvinceToMesh(final Province province) {
		provinceMesh.addVertices(province.getVertices());
		provincesPoints = Helper.addElementsToFloatArray(provincesPoints, province.getPointsPositions());

		final int[] newIndices = new int[province.getIndices().length];
		for (int i = 0; i < newIndices.length; i++) {
			newIndices[i] = province.getIndices()[i] + indicesOffset;
		}
		provinceMesh.addIndices(newIndices);

		indicesOffset += province.getVertices().length / province.getMeshStride();

		pointShaderStorage.regenerate(provincesPoints);
		provinceMesh.regenerate();
	}

	public void removeProvinceFromMesh(final Province province) {
		int j = 0;
		int startIndex = -1;
		// for vertices
		for (int i = 0; i < provinceMesh.getVertices().length; i += provinceMesh.getStrideSum()) {
			// if any of the positions of the vertices are the same
			if (provinceMesh.getVertices()[i] == province.getVertices()[0] &&
					provinceMesh.getVertices()[i + 1] == province.getVertices()[1]) {
				startIndex = i;

				int matchedTimes = 0;
				// we still need to check if this isn't a duplicate vertex of another province
				for (int k = i; k < province.getVertices().length; k += provinceMesh.getStrideSum()) {
					// any of the vertices doesn't match
					if (provinceMesh.getVertices()[k] != province.getVertices()[j] &&
							provinceMesh.getVertices()[k + 1] != province.getVertices()[j + 1]) {
						break;
					}
					matchedTimes++;
					j += province.getMeshStride();
				}

				// if the province matches, i.e. the province's vertex count is the same as the times it got a match
				if (matchedTimes == province.getVertices().length / province.getMeshStride()) {
					System.out.println("Before deletion: " + Arrays.toString(provinceMesh.getVertices()));
					provinceMesh.setVertices(Helper.deleteElementsFromFloatArray(provinceMesh.getVertices(), startIndex, j - startIndex));
					System.out.println("After deletion: " + Arrays.toString(provinceMesh.getVertices()));
					provinceMesh.regenerate();
					return;
				}
			}
		}
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
			glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, provincesPoints.length / Consts.POINT_POSITION_STRIDE);
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

	public Province getProvince(final int index) {
		return provinces.get(index);
	}

}
