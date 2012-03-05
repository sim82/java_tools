/*
 * Copyright (C) 2010 Simon A. Berger
 * 
 *  This program is free software; you may redistribute it and/or modify its
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 */

package ml;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

class Score {
    public double s1;
    public double s2;
    public double so;
    public double score;
    public String name;

    Score(double d, String name, double s1, double s2, double so) {
	this.score = d;
	this.name = name;

	this.s1 = s1;
	this.s2 = s2;
	this.so = so;
    }

}

class Alternative {

    Map<String, Score> nts = new HashMap<String, Score>();
    Score[] scores;

    Alternative(File f) {
	CSVFile cf_wgh = CSVFile.load(f, "SDDD");

	String[] names = cf_wgh.getString(0);
	double[] s1 = cf_wgh.getDouble(1);
	double[] s2 = cf_wgh.getDouble(2);
	double[] so = cf_wgh.getDouble(3);
	scores = new Score[names.length];
	for (int i = 0; i < names.length; i++) {
	    scores[i] = new Score(s1[i] - s2[i], names[i], s1[i], s2[i], so[i]);
	    nts.put(names[i], scores[i]);

	}
	Arrays.sort(scores, new Comparator<Score>() {

	    @Override
	    public int compare(Score o1, Score o2) {

		final double d;

		if (o1.so > 0 || o2.so > 0) {
		    d = o1.so - o2.so;
		} else {
		    d = o1.score - o2.score;
		}
		if (d < 0) {
		    return -1;
		} else if (d > 0) {
		    return 1;
		} else {
		    return 0;
		}
	    }
	});

    }
}

public class EpaBinningVis {

    interface LD {
	void draw(int i, int j, int y);
    }

    public static void main(String[] args) {
	// File f = new File( args[0] );
	File f = new File("/space/rlucking/bs_nowgh.txt");
	File f_wgh = new File("/space/rlucking/bs_wgh.txt");
	File f_wgh_mp = new File("/space/rlucking/bs_wgh_mp.txt");

	Alternative ar = new Alternative(f);

	Alternative a1 = new Alternative(f_wgh);
	Alternative a2 = new Alternative(f_wgh_mp);

	int scale = 8;

	final int c0width = 16;
	// final int cwidth = 32;
	final int width = 9 * c0width;
	final int height = ar.scores.length;

	BufferedImage bi = new BufferedImage(width * scale, height * scale,
		BufferedImage.TYPE_INT_ARGB);

	final Graphics2D g = bi.createGraphics();
	// g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	// RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	g.setBackground(Color.WHITE);
	g.clearRect(0, 0, bi.getWidth(), bi.getHeight());
	AffineTransform tr = AffineTransform.getScaleInstance(scale, scale
		- scale * (10.0 / height));
	tr.concatenate(AffineTransform.getTranslateInstance(0, 10));
	g.setTransform(tr);

	LD ld = new LD() {

	    @Override
	    public void draw(int i, int j, int y) {
		int x1 = i * (c0width * 3) + j * c0width;
		g.drawLine(x1, y, x1 + c0width, y);

	    }

	};

	for (int i = 0; i < ar.scores.length; i++) {
	    Score s = ar.scores[i];

	    g.setColor(getColor(s.s1));
	    ld.draw(0, 0, i);

	    g.setColor(getColor(s.s2));
	    ld.draw(0, 1, i);

	    g.setColor(getColor(s.so));
	    ld.draw(0, 2, i);

	    Score s1 = a1.nts.get(s.name);
	    g.setColor(getColor(s1.s1));
	    ld.draw(1, 0, i);

	    g.setColor(getColor(s1.s2));
	    ld.draw(1, 1, i);

	    g.setColor(getColor(s1.so));
	    ld.draw(1, 2, i);

	    Score s2 = a2.nts.get(s.name);
	    g.setColor(getColor(s2.s1));
	    ld.draw(2, 0, i);

	    g.setColor(getColor(s2.s2));
	    ld.draw(2, 1, i);

	    g.setColor(getColor(s2.so));
	    ld.draw(2, 2, i);

	    if (false) {
		g.setColor(getColor(((double) i / ar.scores.length) * 100));
		ld.draw(3, 0, i);
	    }
	}
	g.setColor(Color.black);

	for (String font : GraphicsEnvironment.getLocalGraphicsEnvironment()
		.getAvailableFontFamilyNames()) {
	    System.out.printf("font: %s\n", font);
	}

	g.setTransform(AffineTransform.getScaleInstance(scale, scale));
	g.drawLine(c0width * 3, 0, c0width * 3, height);
	g.drawLine(c0width * 6, 0, c0width * 6, height);
	g.setFont(new Font("DejaVu Sans", Font.PLAIN, 7));
	g.setXORMode(Color.WHITE);

	g.drawString("UNW", 14, 8);
	g.drawString("WGH-ML", 3 * c0width + 10, 8);
	g.drawString("WGH-MP", 6 * c0width + 10, 8);

	for (int i = 0; i < 3; i++) {
	    int y = 16;
	    g.drawString("A", i * 3 * c0width + 5, y);
	    g.drawString("G", i * 3 * c0width + c0width + 4, y);
	    g.drawString("O", i * 3 * c0width + c0width * 2 + 3, y);
	}

	g.dispose();

	try {
	    ImageIO.write(bi, "png", new File("/tmp/test.png"));
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    static Color rgbLerp(double p) {
	final double[] splits;
	final Color[] cols;
	if (false) {
	    double[] s = { 0.0, 0.5, 1.0 };
	    Color[] c = { Color.RED, Color.YELLOW, Color.GREEN };
	    splits = s;
	    cols = c;
	} else {
	    double[] s = { 0.0, 1.0 };
	    Color[] c = { Color.WHITE, Color.BLACK };
	    splits = s;
	    cols = c;
	}

	for (int i = 0; i < splits.length - 1; i++) {
	    double sl = splits[i];
	    double sr = splits[i + 1];

	    if (p >= sl && p <= sr) {
		double w = sr - sl;

		Color cl = cols[i];
		Color cr = cols[i + 1];

		int cdr = cr.getRed() - cl.getRed();
		int cdg = cr.getGreen() - cl.getGreen();
		int cdb = cr.getBlue() - cl.getBlue();

		double f = (p - sl) / w;

		return new Color((int) (cl.getRed() + f * cdr),
			(int) (cl.getGreen() + f * cdg),
			(int) (cl.getBlue() + f * cdb));
	    }
	}

	throw new RuntimeException("bogus gradient setup");
    }

    final static Color lightGreen = new Color(0.7f, 1.0f, 0.7f);

    private static Color getColor(double score) {
	double a = Math.abs(score);

	if (false) {
	    if (a >= 100) {
		return Color.GREEN;
	    } else if (a > 70) {
		return lightGreen;
	    } else if (a > 2) {
		return Color.YELLOW;
	    } else if (a != 0) {
		return Color.RED;
	    } else {
		return Color.WHITE;

	    }
	} else {
	    if (a < 1.0) {
		return Color.WHITE;
	    } else {

		return rgbLerp(a / 100.0);
	    }

	}
    }
}
