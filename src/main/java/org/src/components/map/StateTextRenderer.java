package org.src.components.map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.src.core.helper.Helper.FLOAT;

import java.util.HashMap;

import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.freetype.*;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Shader;

public final class StateTextRenderer {
    
    private class Char {
        int textureID;
        Vector2i size;
        Vector2i bearing;
        int advance;

        public Char(int textureID, Vector2i size, Vector2i bearing, int advance) {
            this.textureID = textureID;
            this.size = size;
            this.bearing = bearing;
            this.advance = advance;
        }
    }
    HashMap<Character, Char> characters;

    private final long freetype;
    private final FT_Face face;
    int VAO, VBO;


    public StateTextRenderer() {
        characters = new HashMap<>();

        final PointerBuffer libraryBuffer = BufferUtils.createPointerBuffer(1);
        if (FreeType.FT_Init_FreeType(libraryBuffer) != 0) {
            System.err.println("Could not initialize freetype!");
            System.exit(1);
        }

        freetype = libraryBuffer.get(0);

        final PointerBuffer faceBuffer = BufferUtils.createPointerBuffer(1);
        
        if (FreeType.FT_New_Face(freetype, "res/fonts/times.ttf", 0, faceBuffer) != 0) {
            System.err.println("Could not load font: " + "res/fonts/times.ttf");
            System.exit(1);
        }

        face = FT_Face.create(faceBuffer.get(0));

        // this is no good idea but it's the only thing that works.
        if (FreeType.FT_Set_Pixel_Sizes(face, 0, 48) != 0) {
            System.err.println("Failed to set the pixel size of the font");
            System.exit(1);
        }

        createFontTexture();

        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * 6 * 4, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);   
    }
    
    // TODO: Replace with a FontTexture class
    private void createFontTexture() {
        
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);  

        for (char i = 0; i < 128; i++) {
            if (FreeType.FT_Load_Char(face, (long)i, FreeType.FT_LOAD_RENDER) != 0) {
                System.out.println("Couldn't load glyph");
                continue;
            }

            // TODO: Replace with a texture array
            int texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RED,
                face.glyph().bitmap().width(),
                face.glyph().bitmap().rows(),
                0,
                GL_RED,
                GL_UNSIGNED_BYTE,
                face.glyph().bitmap().buffer(1)
            );

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            
            Char character = new Char(
                texture,
                new Vector2i(face.glyph().bitmap().width(), face.glyph().bitmap().rows()),
                new Vector2i(face.glyph().bitmap_left(), face.glyph().bitmap_top()),
               (int) face.glyph().advance().x()
            );

            characters.put(i, character);

        }

        FreeType.FT_Done_Face(face);
        FreeType.FT_Done_FreeType(freetype);
    }

    public void draw() {
        float x = 0;
        float y = 0;
        float scale = 0.001f;

        ShaderManager.get(ShaderID.TEXT).bind();
        ShaderManager.get(ShaderID.TEXT).setFloat3("textColor", 1.0f, 0.0f, 0.0f);
        glActiveTexture(GL_TEXTURE0);
        glBindVertexArray(VAO);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        float sin30 = (float) Math.sin(Math.toRadians(glfwGetTime() * 10));
        float cos30 = (float) Math.cos(Math.toRadians(glfwGetTime() * 10));

        char[] chars = {'I','f',' ','u','r',' ','r','e','a','d','i','n','\'',',',' ','h','a','v','e',' ','a',' ','n','i','c','e',' ','d','a','y'};
        // iterate through all characters
        for (final char c: chars) {
            Char ch = characters.get(c);

            float xpos = x + ch.bearing.x * scale;
            float ypos = y - (ch.size.y - ch.bearing.y) * scale;
        
            float width = ch.size.x * scale;
            float height = ch.size.y * scale;

            float sin = (float) Math.sin(glfwGetTime());
            float cos = (float) Math.cos(glfwGetTime());
            // update VBO for each character
            float[] vertices = new float[]{
                // this is kinda complicated but it's only a 2d rotation matrix
                 xpos          * cos - (ypos + height) * sin,  (ypos + height) * cos + (xpos)         * sin, 0.0f, 0.0f,
                 xpos          * cos -  ypos           * sin,   ypos           * cos +  xpos          * sin, 0.0f, 1.0f,
                (xpos + width) * cos -  ypos           * sin,   ypos           * cos + (xpos + width) * sin, 1.0f, 1.0f,
                 xpos          * cos - (ypos + height) * sin,  (ypos + height) * cos +  xpos          * sin,   0.0f, 0.0f,
                (xpos + width) * cos -  ypos           * sin,   ypos           * cos + (xpos + width) * sin,       1.0f, 1.0f,
                (xpos + width) * cos - (ypos + height) * sin , (ypos + height) * cos + (xpos + width) * sin,   1.0f, 0.0f           
            };

            glBindTexture(GL_TEXTURE_2D, ch.textureID);
            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            glBufferSubData(GL_ARRAY_BUFFER, 0, BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip()); 
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glDrawArrays(GL_TRIANGLES, 0, 6);

            x += (ch.advance >> 6) * scale; 
        }
        glBindVertexArray(0);
        glDisable(GL_BLEND);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void dispose() {

    }

}
