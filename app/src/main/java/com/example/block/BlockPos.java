package com.example.block;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public final class BlockPos {
    private static final ArrayList<Point> z1 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(1,0));
        add(new Point(1,1));
        add(new Point(2,1));
    }};
    private static final ArrayList<Point> z2 = new ArrayList<Point>(){{
        add(new Point(1,0));
        add(new Point(1,1));
        add(new Point(0,1));
        add(new Point(0,2));
    }};
    private static final ArrayList<Point> s1 = new ArrayList<Point>(){{
        add(new Point(0,1));
        add(new Point(1,1));
        add(new Point(1,0));
        add(new Point(2,0));
    }};
    private static final ArrayList<Point> s2 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(0,1));
        add(new Point(1,1));
        add(new Point(1,2));
    }};
    private static final ArrayList<Point> o = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(0,1));
        add(new Point(1,0));
        add(new Point(1,1));
    }};
    private static final ArrayList<Point> t1 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(0,1));
        add(new Point(0,2));
        add(new Point(1,1));
    }};
    private static final ArrayList<Point> t2 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(1,0));
        add(new Point(2,0));
        add(new Point(1,1));
    }};
    private static final ArrayList<Point> t3 = new ArrayList<Point>(){{
        add(new Point(1,0));
        add(new Point(1,1));
        add(new Point(1,2));
        add(new Point(0,1));
    }};
    private static final ArrayList<Point> t4 = new ArrayList<Point>(){{
        add(new Point(0,1));
        add(new Point(1,1));
        add(new Point(2,1));
        add(new Point(1,0));
    }};
    private static final ArrayList<Point> l1 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(0,1));
        add(new Point(0,2));
        add(new Point(1,2));
    }};
    private static final ArrayList<Point> l2 = new ArrayList<Point>(){{
        add(new Point(0,1));
        add(new Point(1,1));
        add(new Point(2,1));
        add(new Point(2,0));
    }};
    private static final ArrayList<Point> l3 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(1,0));
        add(new Point(1,1));
        add(new Point(1,2));
    }};
    private static final ArrayList<Point> l4 = new ArrayList<Point>(){{
        add(new Point(0,1));
        add(new Point(0,0));
        add(new Point(1,0));
        add(new Point(2,0));
    }};
    private static final ArrayList<Point> j1 = new ArrayList<Point>(){{
        add(new Point(1,0));
        add(new Point(1,1));
        add(new Point(1,2));
        add(new Point(0,2));
    }};
    private static final ArrayList<Point> j2 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(1,0));
        add(new Point(2,0));
        add(new Point(2,1));
    }};
    private static final ArrayList<Point> j3 = new ArrayList<Point>(){{
        add(new Point(1,0));
        add(new Point(0,0));
        add(new Point(0,1));
        add(new Point(0,2));
    }};
    private static final ArrayList<Point> j4 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(0,1));
        add(new Point(1,1));
        add(new Point(2,1));
    }};
    private static final ArrayList<Point> i1 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(0,1));
        add(new Point(0,2));
        add(new Point(0,3));
    }};
    private static final ArrayList<Point> i2 = new ArrayList<Point>(){{
        add(new Point(0,0));
        add(new Point(1,0));
        add(new Point(2,0));
        add(new Point(3,0));
    }};
    public static final ArrayList<ArrayList<Point>> L = new ArrayList<ArrayList<Point>>(){{
        add(z1); add(z2); add(z1); add(z2);
        add(s1); add(s2); add(s1); add(s2);
        add(o);  add(o);  add(o);  add(o);
        add(t1); add(t2); add(t3); add(t4);
        add(l1); add(l2); add(l3); add(l4);
        add(j1); add(j2); add(j3); add(j4);
        add(i1); add(i2); add(i1); add(i2);
    }};
}
