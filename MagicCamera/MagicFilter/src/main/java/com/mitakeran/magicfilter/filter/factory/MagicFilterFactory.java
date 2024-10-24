package com.mitakeran.magicfilter.filter.factory;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.mitakeran.magicfilter.filter.advance.common.MagicAmaroFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicAntiqueFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicBeautyFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicBlackCatFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicBrannanFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicBrooklynFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicCalmFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicCoolFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicCrayonFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicEarlyBirdFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicEmeraldFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicEvergreenFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicFairytaleFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicFreudFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicHealthyFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicHefeFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicHudsonFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicInkwellFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicKevinFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicLatteFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicLomoFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicN1977Filter;
import com.mitakeran.magicfilter.filter.advance.common.MagicNashvilleFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicNostalgiaFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicPixarFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicRiseFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicRomanceFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSakuraFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSierraFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSketchFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSkinWhitenFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSunriseFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSunsetFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSutroFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicSweetsFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicTenderFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicToasterFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicValenciaFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicWaldenFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicWarmFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicWhiteCatFilter;
import com.mitakeran.magicfilter.filter.advance.common.MagicXproIIFilter;
import com.mitakeran.magicfilter.filter.advance.image.MagicImageAdjustFilter;
import com.mitakeran.magicfilter.filter.base.MagicBaseGroupFilter;
import com.mitakeran.magicfilter.filter.base.MagicBilateralFilter;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageBrightnessFilter;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageContrastFilter;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageExposureFilter;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageHueFilter;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageSaturationFilter;
import com.mitakeran.magicfilter.filter.base.gpuimage.GPUImageSharpenFilter;
import com.mitakeran.magicfilter.filter.helper.MagicFilterType;

public class MagicFilterFactory{
	
	private static int mFilterType = MagicFilterType.NONE;	
	
	public static GPUImageFilter getFilters(int type, Context mContext){
		mFilterType = type;
		switch (type) {
		case MagicFilterType.WHITECAT:
			return new MagicWhiteCatFilter(mContext);
		case MagicFilterType.BLACKCAT:
			return new MagicBlackCatFilter(mContext);
		case MagicFilterType.BEAUTY:
			return new MagicBeautyFilter(mContext);
		case MagicFilterType.SKINWHITEN:
			//List<GPUImageFilter> filters = new ArrayList<GPUImageFilter>();	
			//filters.add(new MagicBilateralFilter(mContext));
			//filters.add(new MagicSkinWhitenFilter(mContext));
			//return new MagicBaseGroupFilter(filters);
			return new MagicSkinWhitenFilter(mContext);
		case MagicFilterType.ROMANCE:
			return new MagicRomanceFilter(mContext);
		case MagicFilterType.SAKURA:
			return new MagicSakuraFilter(mContext);
		case MagicFilterType.AMARO:
			return new MagicAmaroFilter(mContext);
		case MagicFilterType.WALDEN:
			return new MagicWaldenFilter(mContext);
		case MagicFilterType.ANTIQUE:
			return new MagicAntiqueFilter(mContext);
		case MagicFilterType.CALM:
			return new MagicCalmFilter(mContext);
		case MagicFilterType.BRANNAN:
			return new MagicBrannanFilter(mContext);
		case MagicFilterType.BROOKLYN:
			return new MagicBrooklynFilter(mContext);
		case MagicFilterType.EARLYBIRD:
			return new MagicEarlyBirdFilter(mContext);
		case MagicFilterType.FREUD:
			return new MagicFreudFilter(mContext);
		case MagicFilterType.HEFE:
			return new MagicHefeFilter(mContext);
		case MagicFilterType.HUDSON:
			return new MagicHudsonFilter(mContext);
		case MagicFilterType.INKWELL:
			return new MagicInkwellFilter(mContext);
		case MagicFilterType.KEVIN:
			return new MagicKevinFilter(mContext);
		case MagicFilterType.LOMO:
			return new MagicLomoFilter(mContext);
		case MagicFilterType.N1977:
			return new MagicN1977Filter(mContext);
		case MagicFilterType.NASHVILLE:
			return new MagicNashvilleFilter(mContext);
		case MagicFilterType.PIXAR:
			return new MagicPixarFilter(mContext);
		case MagicFilterType.RISE:
			return new MagicRiseFilter(mContext);
		case MagicFilterType.SIERRA:
			return new MagicSierraFilter(mContext);
		case MagicFilterType.SUTRO:
			return new MagicSutroFilter(mContext);
		case MagicFilterType.TOASTER2:
			return new MagicToasterFilter(mContext);
		case MagicFilterType.VALENCIA:
			return new MagicValenciaFilter(mContext);
		case MagicFilterType.XPROII:
			return new MagicXproIIFilter(mContext);
		case MagicFilterType.EVERGREEN:
			return new MagicEvergreenFilter(mContext);
		case MagicFilterType.HEALTHY:
			return new MagicHealthyFilter(mContext);
		case MagicFilterType.COOL:
			return new MagicCoolFilter(mContext);
		case MagicFilterType.EMERALD:
			return new MagicEmeraldFilter(mContext);
		case MagicFilterType.LATTE:
			return new MagicLatteFilter(mContext);
		case MagicFilterType.WARM:
			return new MagicWarmFilter(mContext);
		case MagicFilterType.TENDER:
			return new MagicTenderFilter(mContext);
		case MagicFilterType.SWEETS:
			return new MagicSweetsFilter(mContext);
		case MagicFilterType.NOSTALGIA:
			return new MagicNostalgiaFilter(mContext);
		case MagicFilterType.FAIRYTALE:
			return new MagicFairytaleFilter(mContext);
		case MagicFilterType.SUNRISE:
			return new MagicSunriseFilter(mContext);
		case MagicFilterType.SUNSET:
			return new MagicSunsetFilter(mContext);
		case MagicFilterType.CRAYON:
			return new MagicCrayonFilter(mContext);
		case MagicFilterType.SKETCH:
			return new MagicSketchFilter(mContext);
			
		case MagicFilterType.BRIGHTNESS:
			return new GPUImageBrightnessFilter();
		case MagicFilterType.CONTRAST:
			return new GPUImageContrastFilter();
		case MagicFilterType.EXPOSURE:
			return new GPUImageExposureFilter();
		case MagicFilterType.HUE:
			return new GPUImageHueFilter();
		case MagicFilterType.SATURATION:
			return new GPUImageSaturationFilter();
		case MagicFilterType.SHARPEN:
			return new GPUImageSharpenFilter();
		case MagicFilterType.IMAGE_ADJUST:
			return new MagicImageAdjustFilter();
		default:
			return null;
		}
	}
	
	public int getFilterType(){
		return mFilterType;
	}
}
