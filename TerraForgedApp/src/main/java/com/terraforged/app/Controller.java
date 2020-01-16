package com.terraforged.app;

import processing.core.PApplet;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class Controller {

    private static final int BUTTON_NONE = -1;
    private static final int BUTTON_1 = 37;
    private static final int BUTTON_2 = 39;
    private static final int BUTTON_3 = 3;

    private static final float cameraSpeed = 100F;
    private static final float zoomSpeed = 0.01F;
    private static final float rotateSpeed = 0.002F;
    private static final float translateSpeed = 2F;
    private static final float moveSpeed = 10F;

    private int mouseButton = BUTTON_NONE;
    private int lastX = 0;
    private int lastY = 0;

    private float yaw = -0.2F;
    private float pitch = 0.85F;
    private float translateX = 0F;
    private float translateY = 0F;
    private float translateZ = -800;
    private float velocityX = 0F;
    private float velocityY = 0F;

    private int colorMode = 1;
    private int renderMode = 0;
    private int newSeed = 0;
    private int left = 0;
    private int right = 0;
    private int up = 0;
    private int down = 0;
    private float zoom = 16;
    private boolean filters = true;

    public void apply(PApplet applet) {
        applet.translate(translateX, translateY, translateZ);
        applet.translate(applet.width / 2, applet.height / 2, 0);
        applet.rotateX(pitch);
        applet.rotateZ(yaw);
        update();
    }

    public void update() {
        float forward = up + down;
        float strafe = left + right;
        velocityX = forward * (float) Math.sin(yaw);
        velocityY = forward * (float) Math.cos(yaw);
        velocityX += strafe * (float) Math.sin(yaw + Math.toRadians(90));
        velocityY += strafe * (float) Math.cos(yaw + Math.toRadians(90));
        if (velocityX != 0 || velocityY != 0) {
            float magnitude = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            velocityX /= magnitude;
            velocityY /= magnitude;
        }
    }

    public int getColorMode() {
        return colorMode;
    }

    public int getRenderMode() {
        return renderMode;
    }

    public float velocityX() {
        return velocityX * moveSpeed / zoom;
    }

    public float velocityY() {
        return velocityY * moveSpeed / zoom;
    }

    public float zoomLevel() {
        return zoom;
    }

    public boolean filters() {
        return filters;
    }

    public int getNewSeed() {
        if (newSeed == 1) {
            newSeed = 0;
            return 1;
        }
        if (newSeed != 0) {
            int val = newSeed;
            newSeed = 0;
            return val;
        }
        return 0;
    }

    public void keyPress(KeyEvent event) {
        switch (event.getKey()) {
            case 'w':
                up = -1;
                break;
            case 'a':
                left = -1;
                break;
            case 's':
                down = 1;
                break;
            case 'd':
                right = 1;
                break;
        }
    }

    public void keyRelease(KeyEvent event) {
        switch (event.getKey()) {
            case 'w':
                up = 0;
                return;
            case 'a':
                left = 0;
                return;
            case 's':
                down = 0;
                return;
            case 'd':
                right = 0;
                return;
            case 'r':
                renderMode = renderMode == 0 ? 1 : 0;
                return;
            case 'n':
                newSeed = 1;
                return;
            case 'm':
                newSeed = Main.seed;
                return;
            case 'f':
                filters = !filters;
                return;
            case 'c':
                StringSelection selection = new StringSelection("" + Main.seed);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                return;
            case 'v':
                try {
                    Object data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    newSeed = Integer.parseInt(data.toString());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
        }

        if (event.getKey() >= '1' && event.getKey() <= '9') {
            colorMode = event.getKey() - '0';
            return;
        }
    }

    public void mousePress(MouseEvent event) {
        if (mouseButton == BUTTON_NONE) {
            lastX = event.getX();
            lastY = event.getY();
            mouseButton = event.getButton();
        }
    }

    public void mouseRelease(MouseEvent event) {
        mouseButton = BUTTON_NONE;
    }

    public void mouseWheel(MouseEvent event) {
        translateZ -= event.getCount() * cameraSpeed;
    }

    public void mouseDrag(MouseEvent event) {
        int dx = event.getX() - lastX;
        int dy = event.getY() - lastY;
        boolean ctrl = (event.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK;

        lastX = event.getX();
        lastY = event.getY();

        if (mouseButton == BUTTON_1) {
            yaw -= dx * rotateSpeed;
            pitch -= dy * rotateSpeed;
        }

        if (mouseButton == BUTTON_2) {
            translateX += dx * translateSpeed;
            translateY += dy * translateSpeed;
        }

        if (mouseButton == BUTTON_3) {
            if (ctrl) {
                zoom += (dy - dx) * zoom * zoomSpeed;
                zoom = Math.max(1F, zoom);
            } else {
                translateZ -= (dy - dx) * cameraSpeed * 0.1F;
            }
        }
    }
}
