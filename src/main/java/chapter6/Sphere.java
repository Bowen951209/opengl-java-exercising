package chapter6;

import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.joml.Math.*;

public class Sphere {
    private int numIndices;
    private final int prec; // prec = precision
    private int[ ] indices;
    private Vector3f[ ] vertices;
    private Vector2f[ ] texCoords;
    private Vector3f[ ] normals;
    public Sphere(int p)
    { prec = p;
        initSphere();
    }
    private void initSphere()
    {
        int numVertices = (prec + 1) * (prec + 1);
        numIndices = prec * prec * 6;
        indices = new int[numIndices];
        vertices = new Vector3f[numVertices];
        texCoords = new Vector2f[numVertices];
        normals = new Vector3f[numVertices];
        for (int i = 0; i< numVertices; i++)
        { vertices[i] = new Vector3f();
            texCoords[i] = new Vector2f();
            normals[i] = new Vector3f();
        }
        // calculate triangle vertices
        for (int i=0; i<=prec; i++)
        { for (int j=0; j<=prec; j++)
        {	 float y =cos(toRadians((float) (180-i*180/prec)));
            float x = -(float) cos(toRadians(j*360/(float)prec)) * abs(cos(asin(y)));
            float z =sin(toRadians(j*360/(float)prec)) * abs(cos(asin(y)));
            vertices[i*(prec+1)+j].set(x,y,z);
            texCoords[i*(prec+1)+j].set((float)j/prec, (float)i/prec);
            normals[i*(prec+1)+j].set(x,y,z);
        } }
        // calculate triangle indices
        for(int i=0; i<prec; i++)
        { for(int j=0; j<prec; j++)
        { indices[6 * (i * prec + j)] = i*(prec+1)+j;
            indices[6*(i*prec+j)+1] = i*(prec+1)+j+1;
            indices[6*(i*prec+j)+2] = (i+1)*(prec+1)+j;
            indices[6*(i*prec+j)+3] = i*(prec+1)+j+1;
            indices[6*(i*prec+j)+4] = (i+1)*(prec+1)+j+1;
            indices[6*(i*prec+j)+5] = (i+1)*(prec+1)+j;
        } } }
    public int getNumIndices() { return numIndices; }

    public int[ ] getIndices() { return indices; }
    public Vector3f[ ] getVertices() { return vertices; }
    public Vector2f[ ] getTexCoords() { return texCoords; }
    public Vector3f[ ] getNormals() { return normals; }
}

