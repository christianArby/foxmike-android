package com.foxmike.android.models;

/**
 * Created by chris on 2019-03-19.
 */

public class SessionStars {
    private int five;
    private int four;
    private int three;
    private int two;
    private int one;

    public SessionStars(int five, int four, int three, int two, int one) {
        this.five = five;
        this.four = four;
        this.three = three;
        this.two = two;
        this.one = one;
    }

    public SessionStars() {
    }

    public int getFive() {
        return five;
    }

    public void setFive(int five) {
        this.five = five;
    }

    public int getFour() {
        return four;
    }

    public void setFour(int four) {
        this.four = four;
    }

    public int getThree() {
        return three;
    }

    public void setThree(int three) {
        this.three = three;
    }

    public int getTwo() {
        return two;
    }

    public void setTwo(int two) {
        this.two = two;
    }

    public int getOne() {
        return one;
    }

    public void setOne(int one) {
        this.one = one;
    }
}
