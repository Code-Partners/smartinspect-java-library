//
// <!-- Copyright (C) Code Partners Pty. Ltd. All rights reserved. -->
//

package com.gurock.smartinspect;

// <summary>
//   Used by the GraphicViewerContext class to specify the desired
//   picture type.
// </summary>
// <threadsafety>
//   This class is fully threadsafe.
// </threadsafety>

public final class GraphicId extends Enum
{
	private ViewerId fVi;

	// <summary>
	//   Instructs the GraphicViewerContext class to treat the data
	//   as bitmap image.
	// </summary>

	public final static GraphicId Bitmap = new GraphicId(ViewerId.Bitmap);
	
	// <summary>
	//   Instructs the GraphicViewerContext class to treat the data
	//   as JPEG image.
	// </summary>

	public final static GraphicId Jpeg = new GraphicId(ViewerId.Jpeg);
	
	// <summary>
	//   Instructs the GraphicViewerContext class to treat the data
	//   as Windows icon.
	// </summary>

	public final static GraphicId Icon = new GraphicId(ViewerId.Icon);

	// <summary>
	//   Instructs the GraphicViewerContext class to treat the data
	//   as Windows Metafile image.
	// </summary>

	public final static GraphicId Metafile = new GraphicId(ViewerId.Metafile);

	private GraphicId(ViewerId vi)
	{
		super(vi.getIntValue());
		this.fVi = vi;
	}

	// <summary>
	//   Returns the related viewer ID for this object.
	// </summary>
	// <returns>
	//   The related viewer ID.
	// </returns>

	protected ViewerId toViewerId()
	{
		return this.fVi;
	}
}

