package org.src.components;

import org.src.components.province.Province;
import org.src.core.helper.Component;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.core.managers.TextureManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.Texture;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.src.core.helper.Consts.RECTANGLE_INDICES;

public final class Map extends Component {
	private final Mesh overlayMapMesh;
	private final Mesh provinceMesh;

	private int indicesOffset;

	private final Texture texture;
	private final ArrayList<Province> provinces;

	public Map() {

		indicesOffset = 0;

		this.provinceMesh = new Mesh(new float[] {}, new int[] {}, new byte[] {
				2, 3
		});

		provinces = new ArrayList<>();
		texture = TextureManager.createTexture("res/images/map.png");

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

		return newProvince;
	}

	public void addProvinceToMesh(final Province province) {
		provinceMesh.addVertices(province.getVertices());
		final int[] newIndices = new int[province.getIndices().length];
		for (int i = 0; i < newIndices.length; i++) {
			newIndices[i] = province.getIndices()[i] + indicesOffset;
		}
		provinceMesh.addIndices(newIndices);
		provinceMesh.regenerate();
		indicesOffset += province.getVertices().length / 5;
	}

	@Override
	public void draw() {
		texture.bind();
		ShaderManager.get(ShaderID.DEFAULT).bind();
		ShaderManager.get(ShaderID.DEFAULT).setInt("tex", texture.getSlot());
		overlayMapMesh.draw();

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
		ShaderManager.get(ShaderID.POLYGON).bind();
		provinceMesh.draw();
		glDisable(GL_BLEND);

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

}
