package com.mitakeran.magicfilter.filter.advance.common;

import android.content.Context;
import android.opengl.GLES20;

import com.mitakeran.magicfilter.R;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.mitakeran.magicfilter.utils.OpenGLUtils;

public class MagicToasterFilter extends GPUImageFilter{
	private int[] inputTextureHandles = {-1,-1,-1,-1,-1};
	private int[] inputTextureUniformLocations = {-1,-1,-1,-1,-1};
	private Context mContext;
	
	public MagicToasterFilter(Context context){
		super(NO_FILTER_VERTEX_SHADER,OpenGLUtils.readShaderFromRawResource(context, R.raw.toaster2_filter_shader));
		mContext = context;
	}
	
	public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteTextures(inputTextureHandles.length, inputTextureHandles, 0);
        for(int i = 0; i < inputTextureHandles.length; i++)
        	inputTextureHandles[i] = -1;
    }
	
	protected void onDrawArraysAfter(){
		for(int i = 0; i < inputTextureHandles.length
				&& inputTextureHandles[i] != OpenGLUtils.NO_TEXTURE; i++){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3));
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		}
	}
	  
	protected void onDrawArraysPre(){
		for(int i = 0; i < inputTextureHandles.length 
				&& inputTextureHandles[i] != OpenGLUtils.NO_TEXTURE; i++){
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + (i+3) );
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureHandles[i]);
			GLES20.glUniform1i(inputTextureUniformLocations[i], (i+3));
		}
	}
	
	public void onInit(){
		super.onInit();
		for(int i = 0; i < inputTextureUniformLocations.length; i++){
			inputTextureUniformLocations[i] = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture"+(2+i));
		}
	}
	
	public void onInitialized(){
		super.onInitialized();
	    runOnDraw(new Runnable(){
		    public void run(){
		    	inputTextureHandles[0] = OpenGLUtils.loadTexture(mContext, "filter/toastermetal.png");
				inputTextureHandles[1] = OpenGLUtils.loadTexture(mContext, "filter/toastersoftlight.png");
				inputTextureHandles[2] = OpenGLUtils.loadTexture(mContext, "filter/toastercurves.png");
				inputTextureHandles[3] = OpenGLUtils.loadTexture(mContext, "filter/toasteroverlaymapwarm.png");
				inputTextureHandles[4] = OpenGLUtils.loadTexture(mContext, "filter/toastercolorshift.png");
		    }
	    });
	}
}