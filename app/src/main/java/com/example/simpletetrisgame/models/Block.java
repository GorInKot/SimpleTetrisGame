package com.example.simpletetrisgame.models;

import android.graphics.Color;
import android.graphics.Point;
import com.example.simpletetrisgame.constants.FieldConstrants;
import androidx.annotation.NonNull;
import java.util.Random;

public class Block {
    private int shapeIndex;
    private int frameNumber;
    private final BlockColor color;
    private Point position;

    private Block(int shapeIndex, BlockColor blockColor) {
        this.frameNumber = 0;
        this.shapeIndex = shapeIndex;
        this.color = blockColor;
        this.position = new Point(FieldConstrants.COLUMN_COUNT.getValue() / 2, 0);
    }

    public enum BlockColor {
        PINK(Color.rgb(255, 105, 180), (byte) 2),
        GREEN(Color.rgb(0, 128, 0), (byte) 3),
        ORANGE(Color.rgb(255, 165, 0), (byte) 4),
        YELLOW(Color.rgb(255, 215, 0), (byte) 5),
        CYAN(Color.rgb(0, 255, 255), (byte) 6);

        BlockColor(int rgbValue, byte value) {
            this.rgbValue = rgbValue;
            this.byteValue = value;
        }

        private final int rgbValue;
        private final byte byteValue;
    }

    public static Block createBlock() {
        Random random = new Random();
        int shapeIndex = random.nextInt(Shape.values().length);
        BlockColor blockColor = BlockColor.values()[random.nextInt(BlockColor.values().length)];
        Block block = new Block(shapeIndex, blockColor);
        int startX = FieldConstrants.COLUMN_COUNT.getValue() / 2 - Shape.values()[shapeIndex].getStartPosition();
        block.position.x = Math.max(0, startX); // Предотвращаем отрицательные координаты
        return block;
    }

    public static int getColor(byte value) {
        for (BlockColor color : BlockColor.values()) {
            if (value == color.byteValue) {
                return color.rgbValue;
            }
        }
        return Color.BLACK; // Значение по умолчанию
    }

    public void setState(int frame, @NonNull Point position) {
        this.frameNumber = frame;
        this.position = position;
    }

    @NonNull
    public byte[][] getShape(int frameNumber) {
        return Shape.values()[shapeIndex].getFrame(frameNumber).as2dByteArray();
    }

    public Point getPosition() {
        return this.position;
    }

    public int getFrameCount() {
        return Shape.values()[shapeIndex].getFrameCount();
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public int getColor() {
        return color.rgbValue;
    }

    public byte getStaticValue() {
        return color.byteValue;
    }
}
