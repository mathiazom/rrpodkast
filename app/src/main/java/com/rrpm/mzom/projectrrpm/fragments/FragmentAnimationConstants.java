package com.rrpm.mzom.projectrrpm.fragments;

import com.rrpm.mzom.projectrrpm.R;

public class FragmentAnimationConstants {


    private static final FragmentAnimations DEFAULT_SLIDE_FROM_RIGHT_ANIMATIONS = new FragmentAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
    );

    private static final FragmentAnimations DEFAULT_FADE_ANIMATIONS = new FragmentAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
    );

    private static final FragmentAnimations DEFAULT_PUSH_TO_TOP_ANIMATIONS = new FragmentAnimations(
            R.anim.enter_from_bottom,
            0,
            0,
            R.anim.exit_to_bottom
    );



    static final FragmentAnimations POD_FRAGMENT_ANIMATIONS = DEFAULT_SLIDE_FROM_RIGHT_ANIMATIONS;


    static final FragmentAnimations POD_PLAYER_FULL_FRAGMENT_ANIMATIONS = DEFAULT_PUSH_TO_TOP_ANIMATIONS;


    static final FragmentAnimations POD_LIST_FRAGMENT_ANIMATIONS = DEFAULT_FADE_ANIMATIONS;


    static final FragmentAnimations FILTER_FRAGMENT_ANIMATIONS = DEFAULT_FADE_ANIMATIONS;

    public static final FragmentAnimations FROM_POD_PLAYER_ANIMATIONS = DEFAULT_FADE_ANIMATIONS;




}
