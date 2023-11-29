/**
 * Copyright (C) Code Partners Pty. Ltd. All rights reserved.
 */

package com.gurock.smartinspect;

/**
 * Used by the GraphicViewerContext class to specify the desired
 * picture type.
 * <p>
 * This class is fully threadsafe.
 */
public final class GraphicId extends Enum {
	private ViewerId fVi;

	/**
	 * Instructs the GraphicViewerContext class to treat the data
	 * as bitmap image.
	 */
	public final static GraphicId Bitmap = new GraphicId(ViewerId.Bitmap);

	/**
	 * Instructs the GraphicViewerContext class to treat the data
	 * as JPEG image.
	 */
	public final static GraphicId Jpeg = new GraphicId(ViewerId.Jpeg);

	/**
	 * Instructs the GraphicViewerContext class to treat the data
	 * as Windows icon.
	 */
	public final static GraphicId Icon = new GraphicId(ViewerId.Icon);

	/**
	 * Instructs the GraphicViewerContext class to treat the data
	 * as Windows Metafile image.
	 */
	public final static GraphicId Metafile = new GraphicId(ViewerId.Metafile);

	private GraphicId(ViewerId vi) {
		super(vi.getIntValue());
		this.fVi = vi;
	}

	/**
	 * Returns the related viewer ID for this object.
	 *
	 * @return The related viewer ID.
	 */
	public ViewerId toViewerId() {
		return this.fVi;
	}
}

