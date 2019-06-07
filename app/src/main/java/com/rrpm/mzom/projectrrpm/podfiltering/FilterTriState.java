package com.rrpm.mzom.projectrrpm.podfiltering;


public enum FilterTriState {

    TRUE,
    FALSE,
    ANY;

    public boolean contains(boolean b){

        switch (this){
            case TRUE:
                return b;
            case FALSE:
                return !b;
            case ANY:
                return true;
        }

        return false;
    }

}
