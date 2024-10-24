package com.mitakeran.magiccamera.common.view.edit.frame;

import android.content.Context;
import android.content.DialogInterface;

import com.mitakeran.magiccamera.common.view.edit.ImageEditFragment;
import com.mitakeran.magicfilter.display.MagicImageDisplay;

public class ImageEditFrameView extends ImageEditFragment{

	public ImageEditFrameView(Context context, MagicImageDisplay magicDisplay) {
		super(context, magicDisplay);

	}

	@Override
	protected boolean isChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onDialogButtonClick(DialogInterface dialog) {
		// TODO Auto-generated method stub
		
	}
}
