package org.src.core.managers;

import org.src.core.helper.ShaderID;
import org.src.rendering.wrapper.Shader;

import java.io.File;
import java.util.HashMap;

public final class ShaderManager {
	public static final HashMap<ShaderID, Shader> shaders;

	static {
		shaders = new HashMap<>();
	}

	private static void loadShader(final String vertexPath, final String fragmentPath, final ShaderID shaderID) {
		shaders.put(shaderID, new Shader(vertexPath, fragmentPath));
	}

	public static void loadShaders(final String loadingDirectory) {
		final File shaderFolder = new File(loadingDirectory);
		final File[] shaderFiles = shaderFolder.listFiles();
		assert shaderFiles != null;

		int i = 0;
		for (final ShaderID shaderID: ShaderID.values()) {
			// The paths get loaded alphabetically so we can assume it will go like: *.frag -> *.vert
			loadShader(
					shaderFiles[i + 1].getPath(),
					shaderFiles[i].getPath(),
					shaderID
			);

			i += 2;
		}
	}


	public static Shader get(final ShaderID shaderID) {
		return shaders.get(shaderID);
	}

}
