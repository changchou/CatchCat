package com.zhang.catchcat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Mr.Z on 2016/10/18 0018.
 */

public class Playground extends SurfaceView implements View.OnTouchListener {

    private static final int ROW = 10;
    private static final int COL = 10;
    private static final int BLOCKS = 15;//初始的路障数量
    private static int WIDTH = 40;

    private Dot matrix[][];
    private Dot cat;

    public Playground(Context context) {
        super(context);

        getHolder().addCallback(callback);

        matrix = new Dot[ROW][COL];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j] = new Dot(j, i);
            }
        }

        setOnTouchListener(this);

        initGame();
    }

    private void initGame() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat = new Dot(4, 5);
        getDot(4, 5).setStatus(Dot.STATUS_IN);
        for (int i = 0; i < BLOCKS; ) {
            int x = (int) ((Math.random() * 1000) % COL);
            int y = (int) ((Math.random() * 1000) % ROW);
            if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                i++;
                //System.out.println("Block:"+i);
            }
        }
    }

    private Dot getDot(int x, int y) {
        return matrix[y][x];
    }

    private boolean isAtEdge(Dot d) {
        return d.getX() * d.getY() == 0 ||
                d.getX() + 1 == COL ||
                d.getY() + 1 == ROW;
    }

    private Dot getNeighbour(Dot dot, int dir) {
        switch (dir) {
            case 1:
                return getDot(dot.getX() - 1, dot.getY());
            case 2:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX() - 1, dot.getY() - 1);
                } else {
                    return getDot(dot.getX(), dot.getY() - 1);
                }

            case 3:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX(), dot.getY() - 1);
                } else {
                    return getDot(dot.getX() + 1, dot.getY() - 1);
                }
            case 4:
                return getDot(dot.getX() + 1, dot.getY());
            case 5:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX(), dot.getY() + 1);
                } else {
                    return getDot(dot.getX() + 1, dot.getY() + 1);
                }
            case 6:
                if (dot.getY() % 2 == 0) {
                    return getDot(dot.getX() - 1, dot.getY() + 1);
                } else {
                    return getDot(dot.getX(), dot.getY() + 1);
                }
        }
        return null;
    }

    private int getDistance(Dot dot, int dir) {
        int distance = 0;
        if (isAtEdge(dot)) {
            return 1;
        }
        Dot ori, next;
        ori = dot;
        while (true) {
            next = getNeighbour(ori, dir);
            if (next != null && next.getStatus() == Dot.STATUS_ON) {
                return distance * -1;
            }
            if (isAtEdge(next)) {
                distance++;
                return distance;
            }
            distance++;
            ori = next;
        }
    }

    private void moveTo(Dot dot) {
        dot.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(dot.getX(), dot.getY());
    }

    private void move() {
        if (isAtEdge(cat)) {
            lose();
            return;
        }

        Vector<Dot> available = new Vector<>();
        Vector<Dot> positive = new Vector<>();
        HashMap<Dot, Integer> al = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            Dot n = getNeighbour(cat, i);
            if (n != null && n.getStatus() == Dot.STATUS_OFF) {
                available.add(n);
                al.put(n, i);
                if (getDistance(n, i) > 0) {
                    positive.add(n);
                }
            }
        }

        if (available.size() == 0) {
            win();
        } else if (available.size() == 1) {
            moveTo(available.get(0));
        } else {
            Dot best = null;
            if (positive.size() != 0) {//存在可以直接到达屏幕边缘的走向
                int min = 999;
                for (int i = 0; i < positive.size(); i++) {
                    int a = getDistance(positive.get(i), al.get(positive.get(i)));
                    if (a < min) {
                        min = a;
                        best = positive.get(i);
                    }
                }
            } else {//所有方向都存在路障
                int max = 0;
                for (int i = 0; i < available.size(); i++) {
                    int a = getDistance(available.get(i), al.get(available.get(i)));
                    if (a <= max) {
                        max = a;
                        best = available.get(i);
                    }
                }
            }
            moveTo(best);
        }


    }

    private void win() {
        Toast.makeText(getContext(), "Win", Toast.LENGTH_SHORT).show();
    }

    private void lose() {
        Toast.makeText(getContext(), "Lose", Toast.LENGTH_SHORT).show();

    }

    private void redraw() {
        Canvas c = getHolder().lockCanvas();
        c.drawColor(Color.LTGRAY);
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < ROW; i++) {
            int offset = 0;
            if (i % 2 == 0) {
                offset = WIDTH / 2;
            } else {
                offset = WIDTH;
            }
            for (int j = 0; j < COL; j++) {
                Dot one = getDot(j, i);
                switch (one.getStatus()) {
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFF0000);
                        break;
                }
                c.drawOval(new RectF(one.getX() * WIDTH + offset, one.getY() * WIDTH, (one.getX() + 1) * WIDTH + offset, (one.getY() + 1) * WIDTH), paint);
            }
        }
        getHolder().unlockCanvasAndPost(c);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
//            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            WIDTH = (int) (width / (COL + 1.5));
            redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            int x, y;
            y = (int) (event.getY() / WIDTH);
            if (y % 2 == 0) {
                x = (int) ((event.getX() - WIDTH / 2) / WIDTH);
            } else {
                x = (int) ((event.getX() - WIDTH) / WIDTH);
            }
            if (x + 1 > COL || y + 1 > ROW) {
                new AlertDialog.Builder(getContext()).setTitle("提醒").
                        setMessage("是否重新开始").
                        setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                initGame();
                                redraw();
                            }
                        }).
                        setNegativeButton("取消", null).show();
            } else if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                move();
                redraw();
            }
        }
        return true;
    }
}
