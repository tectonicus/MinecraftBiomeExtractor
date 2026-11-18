package org.tectonicus.biomeextractor;

/**
 * A pixel position in a 256x256 biome colormap. The fields are signed Java bytes; read them as unsigned with
 * {@code & 0xff}.
 */
public class ByteCoord
{
	/** The colormap column, derived from temperature. */
	public byte x;
	/** The colormap row, derived from temperature and moisture. */
	public byte y;

	/** Creates the coordinate (0, 0). */
	public ByteCoord()
	{
		x = 0;
		y = 0;
	}

	/**
	 * Creates a coordinate.
	 *
	 * @param xin the colormap column
	 * @param yin the colormap row
	 */
	public ByteCoord(byte xin, byte yin)
	{
		x = xin;
		y = yin;
	}
}
