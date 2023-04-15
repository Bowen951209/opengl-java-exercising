package utilities;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

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

    public int getNumOfVectors() {
        return numOfVectors;
    }

    private int numOfVectors;

    public ModelReader(String filepath) {

        AIScene scene = Assimp.aiImportFile(filepath, Assimp.aiProcess_Triangulate);

        assert scene != null;
        System.out.println("    Number of Meshes: " + scene.mNumMeshes());
        PointerBuffer buffer = scene.mMeshes();

        if (buffer != null) {
            for (int i = 0; i < buffer.limit(); i++) {
                AIMesh mesh = AIMesh.create(buffer.get(i));
                processMesh(mesh);
            }
        }else {
            throw new RuntimeException("scene.mMeshes is null");
        }


    }

    private void processMesh(AIMesh mesh) {
        AIVector3D.Buffer vectors = mesh.mVertices();

        numOfVectors = vectors.limit();
        pvalue = new float[numOfVectors * 3];
        for (int i = 0; i < numOfVectors; i++) {
            AIVector3D vector = vectors.get(i);

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
            System.err.println("The model has no texture coordinates.");
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
