package brain;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;

//import javax.media.opengl.GL2;
//import javax.media.opengl.GLProfile;
//import javax.media.opengl.fixedfunc.GLLightingFunc;
//import javax.media.opengl.glu.GLU;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;




import perspectives.base.Property;
import perspectives.base.PropertyManager;
import perspectives.base.Task;
import perspectives.three_d.Vector3D;
import perspectives.three_d.Viewer3D;
import perspectives.properties.PBoolean;
import perspectives.properties.PColor;
import perspectives.properties.PInteger;
import perspectives.properties.POptions;
import perspectives.properties.PPercent;
import perspectives.base.PropertyType;
import perspectives.properties.PString;


public class BrainViewer extends Viewer3D{
	public static final String PROPERTY_SELECTED_TUBES="SelectedTubes";
	public static final String PROPERTY_TUBE_WIDTH="Appearance.TubeWidth";
	public static final String PROPERTY_TUBE_FACES="Appearance.TubeFaces";
        public static final String PROPERTY_TUBE_COLOR="Appearance.TubeColor";
	public static final String PROPERTY_SELECTION_MODE="Selection Mode";
	public static final String PROPERTY_ONLY_SELECTED="Only Selected";
	
	public static final double TUBE_WIDTH_COEFFICIENT = 0.01;
	public static final Color COLOR_SELECTION = Color.red;
	
	Tube[] tubes;

	
	int[] totalNumVerts;
	int[] vbo;

	int vertexStride;
	int colorPointer;
	int vertexPointer;
	int normalPointer;
	   
	Vector3D[][] projectedSegments;
	Rectangle2D[] projectedRects;
	
	int oldWidth = 0;
	
	BrainData data;
	
	float[] model = new float[16];
	float[] proj = new float[16];
	int[] viewport = new int[4];
	
	boolean created = false;	
	
	String selectedTubes = "";
	
	public BrainViewer(String name, BrainData dat) {
		super(name);
		this.data = dat;
        final BrainViewer thisf = this;
		Task t = new Task("Create Tubes")
		{
			@Override
			public void task() {
				
				
				try {
					Property<PInteger> ptubewidth = new Property<PInteger>(PROPERTY_TUBE_WIDTH, new PInteger(1))
							{
								@Override
								public boolean updating(PInteger newvalue)
								{
									double width = ((PInteger)newvalue).intValue();
									if (width <= 0) return true;
									width *= TUBE_WIDTH_COEFFICIENT;
									
									int faces = ((PInteger)getProperty(PROPERTY_TUBE_FACES).getValue()).intValue();
									
									Color color = ((PColor)getProperty(PROPERTY_TUBE_COLOR).getValue()).colorValue();
									createGeometry(faces, width, color);
									thisf.requestRender();
									return true;
								}
							};
					addProperty(ptubewidth);
					
					Property<PInteger> ptubefaces = new Property<PInteger>(PROPERTY_TUBE_FACES, new PInteger(6))
							{
								@Override
								public boolean updating(PInteger newvalue)
								{
									double width = ((PInteger)getProperty(PROPERTY_TUBE_WIDTH).getValue()).intValue();
									width *= TUBE_WIDTH_COEFFICIENT;
									
									int faces = ((PInteger)newvalue).intValue();
									Color color = ((PColor)getProperty(PROPERTY_TUBE_COLOR).getValue()).colorValue();
									createGeometry(faces, width, color);
									thisf.requestRender();
									return true;
								}
							};
					addProperty(ptubefaces);
				
final String PROPERTY_TUBE_COLOR="Appearance.TubeColor";
Property<PColor> ptubecolor = new Property<PColor>(PROPERTY_TUBE_COLOR, 
                                                 new PColor(Color.LIGHT_GRAY))
   {
   @Override
   public boolean updating(PColor newvalue)
       {
         double width = 
             ((PInteger)getProperty(PROPERTY_TUBE_WIDTH).getValue()).intValue();
       
         int faces = ((PInteger)getProperty(PROPERTY_TUBE_FACES).getValue()).intValue();

         Color color = ((PColor)newvalue).colorValue();
         createGeometry(faces, width, color);
         thisf.requestRender();
         return true;
        }
     };
addProperty(ptubecolor);

                            Property<PString> pselection = new Property<PString>(BrainViewer.PROPERTY_SELECTED_TUBES, new PString("")) 
                            {
                               @Override
                                protected void receivedBroadcast(PString newvalue, PropertyManager sender) {
                                   
                                    selectedTubes = ((PString) newvalue).stringValue();

                                    String[] split = selectedTubes.split(",");
                                    getProperty("SelectedTubes").setValue(new PString(selectedTubes));
                                    Color tubeColor = ((PColor) getProperty(PROPERTY_TUBE_COLOR).getValue()).colorValue();

                                    ArrayList<Integer> selectedTubeIndices = new ArrayList<Integer>();
                                    for (String tubeIndex : split) {
                                        if (!tubeIndex.isEmpty()) {
                                            int index = Integer.parseInt(tubeIndex.trim());
                                            selectedTubeIndices.add(index);
                                        }

                                    }
                                    for (int i = 0; i < tubes.length; i++) {
                                        changeColorTube.add(i);
                                        if (selectedTubeIndices.contains(new Integer(i))) {
                                            changeColor.add(COLOR_SELECTION);
                                            tubes[i].setSelected(true);
                                        } else {
                                            changeColor.add(tubeColor);
                                            tubes[i].setSelected(false);
                                        }
                                    }
                                    thisf.requestRender();
                                }
                            };
                            pselection.setPublic(true);
                            pselection.setVisible(false);
                            addProperty(pselection);
                            
                            POptions pOption = new POptions(new String[]{"Add", "Remove"});
                            Property<POptions> pSelectionMode = new Property<POptions>(PROPERTY_SELECTION_MODE, pOption )
                    		{
                            	protected boolean updating(POptions newvalue) {
                            		thisf.requestRender();
                            		return true;
                            	};
                    		};
                            thisf.addProperty(pSelectionMode);
                            
                            Property<PBoolean> pOnlySelected = new Property<PBoolean>(PROPERTY_ONLY_SELECTED, new PBoolean(false))
                            {
                            	protected boolean updating(PBoolean newvalue) {
                            		thisf.requestRender();
                            		return true;
                            	};
                    		};
                            thisf.addProperty(pOnlySelected);
				
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				int faces = ((PInteger)getProperty(PROPERTY_TUBE_FACES).getValue()).intValue();
				
				Color color = ((PColor)getProperty(PROPERTY_TUBE_COLOR).getValue()).colorValue();
				double width = ((PInteger)getProperty(PROPERTY_TUBE_WIDTH).getValue()).intValue();
				width *= TUBE_WIDTH_COEFFICIENT;
				
				thisf.createGeometry(faces,width, color );
				
				done();
	
			}
		};
			t.blocking = true;
			t.indeterminate = true;
			this.startTask(t);
		
		System.out.println(" done creating brainviewer");
	}	
	

	
	public boolean creating = false;
	private void createGeometry(int faces, double width, Color color)
	{	
		if (creating) return;
		creating = true;
		
		final int facesf = faces;
		final double widthf = width;
		final Color colorf = color;
		
				created = false;
				System.out.println("creating geom("+faces+", "+width+")");
				
				tubes = new Tube[data.segments.length];
				for (int i=0; i<data.segments.length; i++)
					tubes[i] = new Tube(data.segments[i], facesf, widthf, colorf);
				
				created = true;
				
				vbo = null;
								
			
				
				creating = false;
				System.out.println("done creating geom");
			

	}

	private boolean isTubeSelected(int index)
	{

		return this.tubes[index].isSelected();
	}
	private void setAllTubesDeselected()
	{
		selectedTubes = "";
		getProperty("SelectedTubes").setValue(new PString(selectedTubes));
		Color tubeColor = ((PColor)getProperty("Appearance.TubeColor").getValue()).colorValue();
		for (int i=0; i<tubes.length; i++)
		{
		//	System.out.println(tubes[i].color.getRed());
			//if (tubes[i].color.getRed() == 255)
			if (tubes[i].color== COLOR_SELECTION)
			{
				changeColor.add(tubeColor);
				changeColorTube.add(i);
			}
			tubes[i].setSelected(false);
		}
	}
	private String getSelectedTubesString()
	{
		String selected="";
		for (int i=0; i<tubes.length; i++)
		{
			if (tubes[i].isSelected())
			{
				if (selected.length() == 0)
					selected += i;
				else
					selected += "," + i;
			}
		}
		return selected;
	}

	@Override
	public void render() {
		
		if (!created) return;
		
		
		
		long ttt = new Date().getTime();		
		
		for (int i=0; i<changeColor.size(); i++)
		{
			Color c = changeColor.get(i);
			this.changeTubeColor(changeColorTube.get(i), c);
			
		}
		changeColor.clear();
		changeColorTube.clear();
		
		
        GL11.glShadeModel(GL11.GL_SMOOTH);
     //   GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
     //   GL11.glClearDepth(1.0f);
        GL11.glEnable( GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc( GL11.GL_LEQUAL);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT,  GL11.GL_NICEST);
        

		GL11.glPushMatrix();
		
		float[] savedModel = model; 
		float[] savedProj = proj;
		int[] savedViewport = viewport;
		
		
		FloatBuffer pmb = BufferUtils.createFloatBuffer(proj.length);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, pmb);
		pmb.get(proj);
		
		FloatBuffer mvb = BufferUtils.createFloatBuffer(model.length);
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, mvb);
		mvb.get(model);
		
		IntBuffer vpb = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, vpb);
		vpb.get(viewport);	 
		
		 
		GL11.glMatrixMode (GL11.GL_MODELVIEW);
		

		GL11.glPushMatrix();
		
		GL11.glLoadIdentity();		
		GL11.glTranslated(0, 0, -10);
	
		 
        float SHINE_ALL_DIRECTIONS = 1;
        FloatBuffer lightPos = BufferUtils.createFloatBuffer(4);
        lightPos.put(new float[]{0, 0, -10, SHINE_ALL_DIRECTIONS}); lightPos.rewind();
        FloatBuffer lightColorAmbient = BufferUtils.createFloatBuffer(4);
        lightColorAmbient.put(new float[]{0.01f, 0.01f, 0.01f, 1f}); lightColorAmbient.rewind();
        FloatBuffer lightColorSpecular = BufferUtils.createFloatBuffer(4);
        lightColorSpecular.put(new float[]{0.99f, 0.99f, 0.99f, 1f});lightColorSpecular.rewind();
        

        // Set light parameters.
        
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, lightPos);
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, lightColorAmbient);
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, lightColorSpecular);
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, lightColorSpecular);

        // Enable lighting in GL.
        GL11.glEnable(GL11.GL_LIGHT1);
        GL11.glEnable(GL11.GL_LIGHTING);

        // Set material properties.
        FloatBuffer rgba = BufferUtils.createFloatBuffer(4);
        rgba.put(new float[]{.1f, .1f, .1f, 1f});rgba.rewind();
       // gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, rgba, 0);
        GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, rgba);
        //gl.glMateria
        GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, 1f);
        
        GL11.glPopMatrix();
        
        init();
      
        if (vbo == null) 
        	{
        	//GL11.glPopMatrix();
        	return;
        	}
        
        boolean onlySelectedMode =((PBoolean)this.getProperty(PROPERTY_ONLY_SELECTED).getValue()).boolValue();
 
        for (int i=0; i<tubes.length; i++)
        {	
        	if(onlySelectedMode && !this.isTubeSelected(i))
        	{
        		continue;
        	}
        	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo[i]);

        	GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        	GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        	GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
	      
        	GL11.glColorMaterial( GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE );
        	GL11.glEnable(GL11.GL_COLOR_MATERIAL);

        	GL11.glColorPointer(3,GL11.GL_FLOAT, vertexStride, colorPointer);
        	GL11.glVertexPointer(3,GL11.GL_FLOAT, vertexStride, vertexPointer);
        	GL11.glNormalPointer(GL11.GL_FLOAT, vertexStride, normalPointer);

        	GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, totalNumVerts[i]);

        	GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        	GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        	GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        	GL11.glDisable(GL11.GL_COLOR_MATERIAL);

        	GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }
			
		
	      GL11.glPopMatrix();
	      
	      System.out.println("brain viewer render time: " + (new Date().getTime()-ttt));
		
	}


	@Override
	public String getViewerType() {
		return "Viewer3D";
	}

	
	
	public void project()
	{
		if (!created)
			return;
					
		long ttt = new Date().getTime();
		GLU glu = new GLU();
		
	   	 FloatBuffer modelbuf = BufferUtils.createFloatBuffer(16);
    	 modelbuf.put(model); modelbuf.rewind();
    	 FloatBuffer projbuf = BufferUtils.createFloatBuffer(16);
    	 projbuf.put(proj); projbuf.rewind();
    	 IntBuffer viewportbuf = BufferUtils.createIntBuffer(viewport.length);
    	 viewportbuf.put(viewport); viewportbuf.rewind();
		
		projectedSegments = new Vector3D[tubes.length][];
		projectedRects = new Rectangle2D[tubes.length];
		for (int i=0; i<tubes.length; i++)
		{
			projectedSegments[i] = new Vector3D[tubes[i].segments.length];
			double minX = 99999999;
			double minY = 99999999;
			double maxX = -99999999;
			double maxY = -99999999;		
			
			
			for (int j=0; j<projectedSegments[i].length; j++)
			{
				FloatBuffer result = BufferUtils.createFloatBuffer(3);
				
				glu.gluProject(tubes[i].segments[j].x, tubes[i].segments[j].y, tubes[i].segments[j].z,
						modelbuf,  projbuf,  viewportbuf, 
						result);
			
				Vector3D p = new Vector3D(result.get(0), viewport[3] - result.get(1) -1 , result.get(2));		
				
				projectedSegments[i][j] = p;
				
				if (p.x < minX) minX = p.x; if (p.y < minY) minY = p.y;
				if (p.x > maxX) maxX = p.x; if (p.y > maxY) maxY = p.y;
			}
			
			projectedRects[i] = new Rectangle2D.Double(minX,minY,maxX-minX,maxY-minY);
		}
		
		System.out.println("projected in: " + (new Date().getTime()-ttt));
	}
	
	boolean drag = false;
	int startX, startY;
	int endX, endY;
	
	
	public boolean mousepressed(int x, int y, int button)
	{
		if (button == 1)
		{
			
			this.requestRender();

			drag = true;
			startX = x;
			startY = y;
			endX = x;
			endY = y;
		}
		return false;
	};
	
	public boolean mousereleased(int x, int y, int button)
	{
		if (button == 1)
		{
			drag = false;			
			
			Line2D.Double l1 = new Line2D.Double(startX, startY, x, y);
			
			changeSelection(l1);
		}
		project();
		return false;
	};
	
	ArrayList<Integer> changeColorTube = new ArrayList<Integer>();
	ArrayList<Color> changeColor = new ArrayList<Color>();
	public boolean mousedragged(int currentx, int currenty, int oldx, int oldy)
	{
		if (!created)
			return false;
		
		if (drag)
		{	
			endX = currentx;
			endY = currenty;
			this.requestRender();
			return true;
		}
		return false;
	};
	
	
	public void changeSelection(Line2D l)
	{
		Color tubeColor = ((PColor) this.getProperty(PROPERTY_TUBE_COLOR).getValue()).colorValue();
		int mode = ((POptions)this.getProperty(PROPERTY_SELECTION_MODE).getValue()).selectedIndex;
		
		long t = new Date().getTime();
		
		if (projectedSegments == null)
			project();
		
		for (int i=0; i<projectedSegments.length; i++)
		{
			if (!projectedRects[i].intersectsLine(l)) continue;
			
			for (int j=0; j<projectedSegments[i].length-1; j++)
			{
				
				Line2D.Double l2 = new Line2D.Double(projectedSegments[i][j].x, projectedSegments[i][j].y, projectedSegments[i][j+1].x, projectedSegments[i][j+1].y);
				
				if (l.intersectsLine(l2))
				{		
					changeColorTube.add(i);
					if(mode ==0)
					{
						changeColor.add(COLOR_SELECTION);						
						this.tubes[i].setSelected(true);
					}
					else
					{
						changeColor.add(tubeColor);						
						this.tubes[i].setSelected(false);
					}
					
					break;
				}
			}
		}
		this.selectedTubes = this.getSelectedTubesString();
		this.getProperty("SelectedTubes").setValue(new PString(selectedTubes));
		
		System.out.println("changed selection in: " + (new Date().getTime()-t));
		
		this.requestRender();
	}
	
	public void render2DOverlay(Graphics2D g)
	{
		if (drag)
		{
			g.setColor(Color.red);
			g.setStroke(new BasicStroke(10));
			g.drawLine(startX,  startY, endX, endY);
		}
	}
	

	
	public void changeTubeColor(int tube, Color color)
	{
		float[] c = new float[]{color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f};
		
		tubes[tube].color = color;
		
	//	System.out.println("change color tube: " + tube + " , " + c[0] + "," + c[1] + "," + c[2]);
		 
		GL15.glDeleteBuffers(vbo[tube]);
		 
		totalNumVerts[tube] = tubes[tube].indeces.length;

		// generate a VBO pointer / handle
		      IntBuffer buf = BufferUtils.createIntBuffer(1);
		      GL15.glGenBuffers(buf);
		      vbo[tube] = buf.get();

		      // interleave vertex / color data
		      FloatBuffer data = BufferUtils.createFloatBuffer(tubes[tube].indeces.length * 9);
		      
		      for (int i = 0; i < tubes[tube].indeces.length; i++) {		        	
		            data.put(c);
		            
		            Vector3D vertex = tubes[tube].vertices[tubes[tube].indeces[i]];
		            Vector3D normal = tubes[tube].normals[tubes[tube].indeces[i]];
		            
		            float[] vertexf = new float[]{vertex.x, vertex.y, vertex.z};
		            float[] normalf = new float[]{normal.x, normal.y, normal.z};
		            	            
		            data.put(vertexf);
		            data.put(normalf);
		       
		      }
		      data.rewind();

		      int bytesPerFloat = Float.SIZE / Byte.SIZE;

		      // transfer data to VBO
		      int numBytes = data.capacity() * bytesPerFloat;
		      GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, vbo[tube]);
		      GL15.glBufferData( GL15.GL_ARRAY_BUFFER, data,  GL15.GL_STATIC_DRAW);
		      GL15.glBindBuffer( GL15.GL_ARRAY_BUFFER, 0);

	}

	   
	   public void init()
	   {
		   if (vbo != null)
			   return;
		
		   
		   vbo = new int[tubes.length];
		   totalNumVerts = new int[tubes.length];
		   
		   for (int k=0; k<tubes.length; k++)
			   this.changeTubeColor(k, tubes[k].color);

		   int bytesPerFloat = Float.SIZE / Byte.SIZE;
		      vertexStride = 9 * bytesPerFloat;
		      
		      colorPointer = 0;
		      vertexPointer = 3 * bytesPerFloat;
		      normalPointer = 6* bytesPerFloat;
		   
		 
	   }

}
