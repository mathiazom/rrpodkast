package com.rrpm.mzom.projectrrpm.fragments;

class FragmentAnimations {

    private int enterAnim;
    private int exitAnim;
    private int popEnterAnim;
    private int popExitAnim;


    FragmentAnimations(final int enterAnim, final int exitAnim, final int popEnterAnim, final int popExitAnim) {
        this.enterAnim = enterAnim;
        this.exitAnim = exitAnim;
        this.popEnterAnim = popEnterAnim;
        this.popExitAnim = popExitAnim;
    }

    int getEnterAnim() {
        return enterAnim;
    }

    int getExitAnim() {
        return exitAnim;
    }

    int getPopEnterAnim() {
        return popEnterAnim;
    }

    int getPopExitAnim() {
        return popExitAnim;
    }
}
