package engine.readers;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;
import engine.exceptions.NoMeshesException;

import java.nio.file.Path;

public class ModelReader {


    public float[] getPvalue() {
        return pvalue;
    }

    public float[] getTvalue() {
        return tvalue;
    }

    public float[] getNvalue() {
        return nvalue;
    }

    private float[] pvalue;
    private float[] tvalue;
    private float[] nvalue;

    public int getNumOfVertices() {
        return numOfVertices;
    }

    private int numOfVertices;
    private final String FILE_NAME;

    public ModelReader(String filepath) {
        Path filePath = Path.of(filepath);
        FILE_NAME = filePath.getFileName().toString();

        AIScene scene = Assimp.aiImportFile(filepath, Assimp.aiProcess_Triangulate);

        assert scene != null;
//        System.out.println(FILE_NAME + " has " + scene.mNumMeshes() + " meshes");
        PointerBuffer buffer = scene.mMeshes();

        if (buffer != null) {
            for (int i = 0; i < buffer.limit(); i++) {
                AIMesh mesh = AIMesh.create(buffer.get(i));
                processMesh(mesh);
            }
        } else {
            throw new NoMeshesException("scene.mMeshes is null");
        }


    }

    private void processMesh(AIMesh mesh) {
        AIVector3D.Buffer vertices = mesh.mVertices();

        numOfVertices = vertices.limit();
        pvalue = new float[numOfVertices * 3];
        for (int i = 0; i < numOfVertices; i++) {
            AIVector3D vector = vertices.get(i);

            pvalue[i * 3] = vector.x();
            pvalue[i * 3 + 1] = vector.y();
            pvalue[i * 3 + 2] = vector.z();
        }

        AIVector3D.Buffer texCoords = mesh.mTextureCoords(0);

        if (texCoords != null) {
            tvalue = new float[texCoords.limit() * 2];
            for (int i = 0; i < texCoords.limit(); i++) {
                AIVector3D tc = texCoords.get(i);

                tvalue[i * 2] = tc.x();
                tvalue[i * 2 + 1] = tc.y();
            }
        } else {
            System.err.println(FILE_NAME + " has no texture coordinates.");
        }


        AIVector3D.Buffer norms = mesh.mNormals();

        if (norms != null) {
            nvalue = new float[norms.limit() * 3];
            for (int i = 0; i < norms.limit(); i++) {
                AIVector3D norm = norms.get(i);

                nvalue[i * 3] = norm.x();
                nvalue[i * 3 + 1] = norm.y();
                nvalue[i * 3 + 2] = norm.z();
            }
        } else {
            throw new RuntimeException("No normal values");
        }
    }
}
